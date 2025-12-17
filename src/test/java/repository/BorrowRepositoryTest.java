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

public class BorrowRepositoryTest {

    private BorrowRepository repo;
    private Connection conn;
    private PreparedStatement stmt;
    private ResultSet rs;
    private MockedStatic<DatabaseConnection> dbMock;

    @BeforeEach
    void setup() throws Exception {
        conn = mock(Connection.class);
        stmt = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);
        repo = new BorrowRepository();

        dbMock = mockStatic(DatabaseConnection.class);
        dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);

        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
    }

    @AfterEach
    void tearDown() {
        dbMock.close();
    }

    // --- BORROW ITEM CASES ---

    @Test
    void borrowItem_Success() throws Exception {
        Borrow borrow = new Borrow("test@mail.com", 123, LocalDate.now(), LocalDate.now().plusDays(7), false);
        when(stmt.executeUpdate()).thenReturn(1); // All 3 updates succeed

        boolean result = repo.borrowItem(borrow);

        assertTrue(result);
        verify(conn).commit();
    }

    @Test
    void borrowItem_OutOfStock_Rollback() throws Exception {
        Borrow borrow = new Borrow("test@mail.com", 123, LocalDate.now(), LocalDate.now().plusDays(7), false);
        // 1st update (Insert) = 1, 2nd update (Quantity) = 0 (Failure)
        when(stmt.executeUpdate()).thenReturn(1).thenReturn(0);

        boolean result = repo.borrowItem(borrow);

        assertFalse(result);
        verify(conn).rollback();
    }

    @Test
    void borrowItem_DatabaseError_ReturnsFalse() throws Exception {
        Borrow borrow = new Borrow("error@test.com", 123, LocalDate.now(), LocalDate.now().plusDays(7), false);
        when(conn.prepareStatement(anyString())).thenThrow(new SQLException("DB Connection lost"));

        boolean result = repo.borrowItem(borrow);

        assertFalse(result);
    }

    // --- OVERDUE USERS CASES ---

    @Test
    void getOverdueUsers_Success() throws Exception {
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("student_email")).thenReturn("overdue@test.com");
        when(rs.getInt("item_isbn")).thenReturn(123);
        when(rs.getDate("borrow_date")).thenReturn(Date.valueOf(LocalDate.now()));
        when(rs.getDate("overdue_date")).thenReturn(Date.valueOf(LocalDate.now()));
        when(rs.getBoolean("returned")).thenReturn(false);
        when(rs.getInt("fine")).thenReturn(50);

        List<Borrow> list = repo.getOverdueUsers();

        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
        assertEquals("overdue@test.com", list.get(0).getStudentEmail());
    }

    @Test
    void getOverdueUsers_EmptyResult() throws Exception {
        when(rs.next()).thenReturn(false);

        List<Borrow> list = repo.getOverdueUsers();

        assertTrue(list.isEmpty());
    }

    @Test
    void getOverdueUsers_SQLException_ReturnsEmptyList() throws Exception {
        when(stmt.executeQuery()).thenThrow(new SQLException("Query failed"));

        List<Borrow> list = repo.getOverdueUsers();

        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    // --- PLACEHOLDERS FOR REMAINING METHODS ---
    // Note: Since the logic for these wasn't fully provided in your snippet,
    // these are standard JDBC coverage patterns for those types of methods.

    @Test
    void getTotalFine_ReturnsZeroByDefault() {
        // Based on your snippet returning 0
        assertEquals(0, repo.getTotalFine("test@test.com"));
    }

    @Test
    void findActiveBorrow_ReturnsNullByDefault() {
        // Based on your snippet returning null
        assertNull(repo.findActiveBorrow("test@test.com", 123));
    }

    @Test
    void getStudentsWithUnpaidFines_ReturnsEmptyByDefault() {
        // Based on your snippet returning new ArrayList<>()
        assertTrue(repo.getStudentsWithUnpaidFines().isEmpty());
    }
}