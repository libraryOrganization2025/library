package repository;

import domain.Borrow;
import infrastructure.DatabaseConnection;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BorrowRepositoryTest {

    private BorrowRepository repo;
    private Connection conn;
    private PreparedStatement stmt;
    private MockedStatic<DatabaseConnection> dbMock;

    @BeforeEach
    void setup() throws Exception {
        conn = mock(Connection.class);
        stmt = mock(PreparedStatement.class);
        repo = new BorrowRepository();

        dbMock = mockStatic(DatabaseConnection.class);
        dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);

        // Default: any prepareStatement returns our mock stmt
        when(conn.prepareStatement(anyString())).thenReturn(stmt);
    }

    @AfterEach
    void tearDown() {
        dbMock.close();
    }

    @Test
    void borrowItem_Success() throws Exception {
        // Arrange
        Borrow borrow = new Borrow("test@mail.com", 123, LocalDate.now(), LocalDate.now().plusDays(7), false);

        // Mocking the three statements in the transaction
        when(stmt.executeUpdate()).thenReturn(1); // For INSERT, quantity UPDATE, and user UPDATE

        // Act
        boolean result = repo.borrowItem(borrow);

        // Assert
        assertTrue(result);
        verify(conn).commit();
        verify(conn, never()).rollback();
    }

    @Test
    void borrowItem_OutOfStock_Rollback() throws Exception {
        // Arrange
        Borrow borrow = new Borrow("test@mail.com", 123, LocalDate.now(), LocalDate.now().plusDays(7), false);

        // 1st executeUpdate (Insert) returns 1
        // 2nd executeUpdate (Quantity Update) returns 0 (Out of stock)
        when(stmt.executeUpdate()).thenReturn(1).thenReturn(0);

        // Act
        boolean result = repo.borrowItem(borrow);

        // Assert
        assertFalse(result);
        verify(conn).rollback();
    }

    @Test
    void getOverdueUsers_ReturnsList() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false); // One record found

        // Match the column names in your BorrowRepository.getOverdueUsers()
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("student_email")).thenReturn("overdue@test.com");
        when(rs.getInt("item_isbn")).thenReturn(123);
        when(rs.getDate("borrow_date")).thenReturn(Date.valueOf(LocalDate.now()));
        when(rs.getDate("overdue_date")).thenReturn(Date.valueOf(LocalDate.now()));
        when(rs.getBoolean("returned")).thenReturn(false);
        when(rs.getInt("fine")).thenReturn(10);

        var list = repo.getOverdueUsers();

        assertEquals(1, list.size());
        assertEquals("overdue@test.com", list.get(0).getStudentEmail());
    }
}