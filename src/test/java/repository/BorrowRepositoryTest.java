package repository;

import domain.Borrow;
import infrastructure.DatabaseConnection;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BorrowRepositoryTest {

    private BorrowRepository repo;
    private Connection conn;
    private PreparedStatement stmt;
    private ResultSet rs;
    private MockedStatic<DatabaseConnection> dbMock;

    @BeforeEach
    void setup() throws Exception {
        // Initialize Repository
        repo = new BorrowRepository();

        // Initialize Mocks
        conn = mock(Connection.class);
        stmt = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);

        // Static Mock for Database Connection
        dbMock = mockStatic(DatabaseConnection.class);
        dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);

        // Default behavior for standard statements
        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(stmt.executeUpdate()).thenReturn(1); // Default success for updates/inserts
    }

    @AfterEach
    void cleanup() {
        if (dbMock != null) {
            dbMock.close();
        }
    }

    @Test
    void borrowItem_success() throws Exception {
        Borrow b = new Borrow(1, "x@mail.com", 55, LocalDate.now(), LocalDate.now().plusDays(7), false, 0);

        // Ensure success for both the INSERT and the inventory UPDATE
        when(stmt.executeUpdate()).thenReturn(1);

        boolean result = repo.borrowItem(b);

        assertTrue(result);
        verify(conn, atLeastOnce()).commit();
    }

    @Test
    void borrowItem_outOfStock_rollback() throws Exception {
        Borrow b = new Borrow(1, "x@mail.com", 55, LocalDate.now(), LocalDate.now().plusDays(7), false, 0);

        // 1st call (insert) succeeds, 2nd call (quantity update) fails/returns 0
        when(stmt.executeUpdate()).thenReturn(1).thenReturn(0);

        boolean result = repo.borrowItem(b);

        assertFalse(result);
        verify(conn).rollback();
    }

    @Test
    void borrowItem_exceptionThrown() throws Exception {
        Borrow b = new Borrow(1, "x@mail.com", 55, LocalDate.now(), LocalDate.now().plusDays(7), false, 0);
        when(stmt.executeUpdate()).thenThrow(new SQLException("fail"));

        boolean result = repo.borrowItem(b);

        assertFalse(result);
    }

    @Test
    void getOverdueUsers_returnsList() throws Exception {
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("student_email")).thenReturn("a@mail.com");
        when(rs.getInt("item_isbn")).thenReturn(66);
        when(rs.getDate("borrow_date")).thenReturn(Date.valueOf(LocalDate.now()));
        when(rs.getDate("overdue_date")).thenReturn(Date.valueOf(LocalDate.now()));
        when(rs.getBoolean("returned")).thenReturn(false);
        when(rs.getInt("fine")).thenReturn(10);

        List<Borrow> list = repo.getOverdueUsers();

        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
    }

    @Test
    void returnItem_success() throws Exception {
        Borrow b = new Borrow(1, "a@mail.com", 12, LocalDate.now(), LocalDate.now(), false, 0);
        when(stmt.executeUpdate()).thenReturn(1);

        boolean result = repo.returnItem(b);

        assertTrue(result);
        verify(conn).commit();
    }

    @Test
    void updateFineAfterPayment_fullPayment_coversMultipleRows() throws Exception {
        PreparedStatement select = mock(PreparedStatement.class);
        PreparedStatement update = mock(PreparedStatement.class);
        ResultSet r2 = mock(ResultSet.class);

        // Specific stubs for the two different queries
        when(conn.prepareStatement(contains("SELECT"))).thenReturn(select);
        when(conn.prepareStatement(contains("UPDATE"))).thenReturn(update);

        when(select.executeQuery()).thenReturn(r2);
        when(r2.next()).thenReturn(true, true, false); // Two rows found
        when(r2.getInt("id")).thenReturn(1, 2);
        when(r2.getInt("fine")).thenReturn(30, 20);

        // Crucial: The update mock must return success for the repository to continue loop
        when(update.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> repo.updateFineAfterPayment("mail", 50));
        verify(update, times(2)).executeUpdate();
    }

    @Test
    void getTotalFine_hasValue() throws Exception {
        when(rs.next()).thenReturn(true);
        when(rs.getInt("total")).thenReturn(99);

        int total = repo.getTotalFine("mail");

        assertEquals(99, total);
    }

    @Test
    void markReturned_success() throws Exception {
        // Step 1: Mock the initial SELECT to find the borrow record
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(55);

        // Step 2: Mock the UPDATE statement specifically
        PreparedStatement updateStmt = mock(PreparedStatement.class);
        when(conn.prepareStatement(contains("UPDATE"))).thenReturn(updateStmt);
        when(updateStmt.executeUpdate()).thenReturn(1); // Fix: Must return 1

        boolean result = repo.markReturnedByStudentAndIsbn("mail", 10, 5);

        assertTrue(result);
        verify(updateStmt).executeUpdate();
    }

    @Test
    void findActiveBorrow_found() throws Exception {
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getInt("item_isbn")).thenReturn(12);
        when(rs.getDate("borrow_date")).thenReturn(Date.valueOf(LocalDate.now()));
        when(rs.getDate("overdue_date")).thenReturn(Date.valueOf(LocalDate.now()));

        Borrow result = repo.findActiveBorrow("a@mail.com", 12);

        assertNotNull(result);
        assertEquals(12, result.getIsbn());
    }

    @Test
    void getStudentsWithUnpaidFines_oneRow() throws Exception {
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("student_email")).thenReturn("a@mail.com");

        List<String> result = repo.getStudentsWithUnpaidFines();

        assertEquals(1, result.size());
        assertEquals("a@mail.com", result.get(0));
    }
}