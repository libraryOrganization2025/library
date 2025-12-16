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


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BorrowRepositoryTest {

    private BorrowRepository repo;

    // Common mocks
    private Connection conn;
    private PreparedStatement stmt;
    private ResultSet rs;

    private MockedStatic<DatabaseConnection> dbMock;

    @BeforeEach
    void setup() throws Exception {
        repo = new BorrowRepository();

        conn = mock(Connection.class);
        stmt = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);

        dbMock = mockStatic(DatabaseConnection.class);
        dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);

        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(stmt.executeUpdate()).thenReturn(1);
    }

    @AfterEach
    void cleanup() {
        dbMock.close();
    }


    @Test
    void borrowItem_success() throws Exception {
        Borrow b = new Borrow(
                1, "x@mail.com", 55,
                LocalDate.now(), LocalDate.now().plusDays(7),
                false, 0
        );

        // quantity -1 is successful (rowsUpdated=1)
        when(stmt.executeUpdate()).thenReturn(1);

        boolean result = repo.borrowItem(b);

        assertTrue(result);
        verify(conn).commit();
    }

    @Test
    void borrowItem_outOfStock_rollback() throws Exception {
        Borrow b = new Borrow(
                1, "x@mail.com", 55,
                LocalDate.now(), LocalDate.now().plusDays(7),
                false, 0
        );

        // The first insert returns 1
        when(stmt.executeUpdate())
                .thenReturn(1)   // insert success
                .thenReturn(0);  // update quantity returns 0 → out of stock branch

        boolean result = repo.borrowItem(b);

        assertFalse(result);
        verify(conn).rollback();
    }

    @Test
    void borrowItem_exceptionThrown() throws Exception {
        Borrow b = new Borrow(
                1, "x@mail.com", 55,
                LocalDate.now(), LocalDate.now().plusDays(7),
                false, 0
        );

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
        when(rs.getDate("borrow_date")).thenReturn(Date.valueOf("2024-01-01"));
        when(rs.getDate("overdue_date")).thenReturn(Date.valueOf("2024-01-05"));
        when(rs.getBoolean("returned")).thenReturn(false);
        when(rs.getInt("fine")).thenReturn(10);

        List<Borrow> list = repo.getOverdueUsers();

        assertEquals(1, list.size());
    }

    @Test
    void getOverdueUsers_exception() throws Exception {
        when(conn.prepareStatement(anyString())).thenThrow(new SQLException());

        List<Borrow> list = repo.getOverdueUsers();
        assertTrue(list.isEmpty());
    }


    @Test
    void returnItem_success() throws Exception {
        Borrow b = new Borrow(
                1, "a@mail.com", 12,
                LocalDate.now(), LocalDate.now(), false, 0
        );

        when(stmt.executeUpdate()).thenReturn(1);

        boolean result = repo.returnItem(b);

        assertTrue(result);
        verify(conn).commit();
    }

    @Test
    void returnItem_exception() throws Exception {
        Borrow b = new Borrow(
                1, "mail", 10,
                LocalDate.now(), LocalDate.now(), false, 0
        );

        when(stmt.executeUpdate()).thenThrow(new SQLException());

        boolean result = repo.returnItem(b);

        assertFalse(result);
    }


    @Test
    void updateFineAfterPayment_fullPayment_coversMultipleRows() throws Exception {
        PreparedStatement select = mock(PreparedStatement.class);
        PreparedStatement update = mock(PreparedStatement.class);
        ResultSet r2 = mock(ResultSet.class);

        when(conn.prepareStatement("SELECT id, fine FROM student_borrow WHERE student_email = ? AND fine > 0 ORDER BY id ASC"))
                .thenReturn(select);
        when(conn.prepareStatement("UPDATE student_borrow SET fine = ? WHERE id = ?"))
                .thenReturn(update);

        when(select.executeQuery()).thenReturn(r2);

        when(r2.next()).thenReturn(true, true, false);
        when(r2.getInt("id")).thenReturn(1, 2);
        when(r2.getInt("fine")).thenReturn(30, 20);

        repo.updateFineAfterPayment("mail", 50);

        verify(update, times(2)).executeUpdate();
    }

    @Test
    void updateFineAfterPayment_exception() throws Exception {
        when(conn.prepareStatement(anyString())).thenThrow(new SQLException());

        assertDoesNotThrow(() -> repo.updateFineAfterPayment("mail", 30));
    }


    @Test
    void getTotalFine_hasValue() throws Exception {
        when(rs.next()).thenReturn(true);
        when(rs.getInt("total")).thenReturn(99);

        int total = repo.getTotalFine("mail");

        assertEquals(99, total);
    }

    @Test
    void getTotalFine_noValue() throws Exception {
        when(rs.next()).thenReturn(false);

        int total = repo.getTotalFine("mail");

        assertEquals(0, total);
    }

    @Test
    void getTotalFine_exception() throws Exception {
        when(conn.prepareStatement(anyString())).thenThrow(new SQLException());

        int total = repo.getTotalFine("mail");

        assertEquals(0, total);
    }


    @Test
    void markReturned_success() throws Exception {
        // 1 → row found
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(55);

        PreparedStatement updateStmt = mock(PreparedStatement.class);
        when(conn.prepareStatement("UPDATE student_borrow SET returned = true, fine = ? WHERE id = ?"))
                .thenReturn(updateStmt);

        boolean result = repo.markReturnedByStudentAndIsbn("mail", 10, 5);

        assertTrue(result);
        verify(updateStmt).executeUpdate();
    }

    @Test
    void markReturned_notFound() throws Exception {
        when(rs.next()).thenReturn(false);

        boolean result = repo.markReturnedByStudentAndIsbn("mail", 10, 5);

        assertFalse(result);
    }

    @Test
    void markReturned_exception() throws Exception {
        when(conn.prepareStatement(anyString())).thenThrow(new SQLException());

        boolean result = repo.markReturnedByStudentAndIsbn("mail", 10, 2);

        assertFalse(result);
    }


    @Test
    void findActiveBorrow_found() throws Exception {
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("student_email")).thenReturn("a@mail.com");
        when(rs.getInt("item_isbn")).thenReturn(12);
        when(rs.getDate("borrow_date")).thenReturn(Date.valueOf("2024-01-01"));
        when(rs.getDate("overdue_date")).thenReturn(Date.valueOf("2024-01-10"));
        when(rs.getBoolean("returned")).thenReturn(false);
        when(rs.getInt("fine")).thenReturn(5);

        Borrow result = repo.findActiveBorrow("a@mail.com", 12);

        assertNotNull(result);
        assertEquals(12, result.getIsbn());
    }

    @Test
    void findActiveBorrow_notFound() throws Exception {
        when(rs.next()).thenReturn(false);

        Borrow result = repo.findActiveBorrow("a@mail.com", 12);

        assertNull(result);
    }

    @Test
    void findActiveBorrow_exception() throws Exception {
        when(conn.prepareStatement(anyString())).thenThrow(new SQLException());

        Borrow result = repo.findActiveBorrow("a@mail.com", 12);

        assertNull(result);
    }


    @Test
    void getStudentsWithUnpaidFines_oneRow() throws Exception {
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("student_email")).thenReturn("a@mail.com");

        List<String> result = repo.getStudentsWithUnpaidFines();

        assertEquals(1, result.size());
    }

    @Test
    void getStudentsWithUnpaidFines_exception() throws Exception {
        when(conn.prepareStatement(anyString())).thenThrow(new SQLException());

        List<String> result = repo.getStudentsWithUnpaidFines();

        assertTrue(result.isEmpty());
    }
}
