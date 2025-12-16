package repository;

import domain.Role;
import domain.user;
import infrastructure.DatabaseConnection;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class userRepositoryTest {

    private userRepository repo;

    private Connection conn;
    private PreparedStatement stmt;
    private ResultSet rs;

    private MockedStatic<DatabaseConnection> dbMock;

    @BeforeEach
    void setup() throws Exception {
        repo = new userRepository();

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
    void save_success() throws Exception {
        user u = new user("a@test.com", Role.STUDENT, "pass");

        when(stmt.executeUpdate()).thenReturn(1);

        boolean result = repo.save(u);

        assertTrue(result);
    }

    @Test
    void save_sqlException() throws Exception {
        user u = new user("b@test.com", Role.ADMIN, "hash");

        when(stmt.executeUpdate()).thenThrow(new SQLException("fail"));

        boolean result = repo.save(u);

        assertFalse(result);
    }

    @Test
    void save_genericException() throws Exception {
        user u = new user("z@test.com", Role.STUDENT, "hash");

        when(stmt.executeUpdate()).thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> repo.save(u));
    }


    @Test
    void findByEmail_found() throws Exception {
        when(rs.next()).thenReturn(true);

        when(rs.getString("email")).thenReturn("x@test.com");
        when(rs.getInt("role")).thenReturn(Role.ADMIN.getLevel());
        when(rs.getString("password_hash")).thenReturn("HASH");

        Optional<user> result = repo.findByEmail("x@test.com");

        assertTrue(result.isPresent());
        assertEquals("x@test.com", result.get().getEmail());
    }

    @Test
    void findByEmail_notFound() throws Exception {
        when(rs.next()).thenReturn(false);

        Optional<user> result = repo.findByEmail("missing@test.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByEmail_exception() throws Exception {
        when(conn.prepareStatement(anyString())).thenThrow(new SQLException());

        Optional<user> result = repo.findByEmail("error@test.com");

        assertTrue(result.isEmpty());
    }


    @Test
    void findInactiveUsers_foundRows() throws Exception {
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);

        when(rs.next()).thenReturn(true, false);

        when(rs.getString("email")).thenReturn("inactive@test.com");
        when(rs.getInt("role")).thenReturn(Role.STUDENT.getLevel());
        when(rs.getString("password_hash")).thenReturn("hashA");

        when(rs.getDate("lastdateborrowed")).thenReturn(null);
        when(rs.getDate("createdOn")).thenReturn(Date.valueOf(oneYearAgo.minusDays(10)));
        when(rs.getDate("deletedOn")).thenReturn(null);

        List<user> result = repo.findInactiveUsersSince(oneYearAgo);

        assertEquals(1, result.size());
    }

    @Test
    void findInactiveUsers_noRows() throws Exception {
        when(rs.next()).thenReturn(false);

        List<user> result = repo.findInactiveUsersSince(LocalDate.now().minusYears(1));

        assertTrue(result.isEmpty());
    }

    @Test
    void findInactiveUsers_exception() throws Exception {
        when(conn.prepareStatement(anyString())).thenThrow(new SQLException());

        List<user> result = repo.findInactiveUsersSince(LocalDate.now().minusYears(1));

        assertTrue(result.isEmpty());
    }


    @Test
    void softDeleteInactiveUser_success() throws Exception {
        when(stmt.executeUpdate()).thenReturn(1);

        boolean result = repo.softDeleteInactiveUser("x@test.com", LocalDate.now().minusYears(1));

        assertTrue(result);
    }

    @Test
    void softDeleteInactiveUser_noRowUpdated() throws Exception {
        when(stmt.executeUpdate()).thenReturn(0);

        boolean result = repo.softDeleteInactiveUser("y@test.com", LocalDate.now().minusYears(1));

        assertFalse(result);
    }

    @Test
    void softDeleteInactiveUser_exception() throws Exception {
        when(stmt.executeUpdate()).thenThrow(new SQLException());

        boolean result = repo.softDeleteInactiveUser("err@test.com", LocalDate.now().minusYears(1));

        assertFalse(result);
    }


    @Test
    void updateRole_success() throws Exception {
        when(stmt.executeUpdate()).thenReturn(1);

        boolean result = repo.updateRole("user@test.com", Role.ADMIN);

        assertTrue(result);
    }

    @Test
    void updateRole_noUpdate() throws Exception {
        when(stmt.executeUpdate()).thenReturn(0);

        boolean result = repo.updateRole("no@test.com", Role.ADMIN);

        assertFalse(result);
    }

    @Test
    void updateRole_exception() throws Exception {
        when(stmt.executeUpdate()).thenThrow(new SQLException());

        boolean result = repo.updateRole("wrong@test.com", Role.STUDENT);

        assertFalse(result);
    }
}
