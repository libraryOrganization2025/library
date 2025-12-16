package repository;

import domain.Items;
import domain.libraryType;
import infrastructure.DatabaseConnection;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.sql.*;
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
public class ItemsRepositoryTest {

    private ItemsRepository repo;

    private Connection conn;
    private PreparedStatement stmt;
    private ResultSet rs;

    private MockedStatic<DatabaseConnection> dbMock;

//    @BeforeEach
//    void setup() throws Exception {
//        repo = new ItemsRepository();
//
//        conn = mock(Connection.class);
//        stmt = mock(PreparedStatement.class);
//        rs = mock(ResultSet.class);
//
//        dbMock = mockStatic(DatabaseConnection.class);
//        dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);
//
//        when(conn.prepareStatement(anyString())).thenReturn(stmt);
//        when(stmt.executeQuery()).thenReturn(rs);
//        when(stmt.executeUpdate()).thenReturn(1); // default success
//    }
//
//    @AfterEach
//    void cleanup() {
//        dbMock.close();
//    }


    @Test
    void addItem_success() throws Exception {
        Items item = new Items("Author", "Name", libraryType.Book, 10, "123");

        when(stmt.executeUpdate()).thenReturn(1);

        boolean result = repo.addItem(item);

        assertTrue(result);
    }

    @Test
    void addItem_exception() throws Exception {
        Items item = new Items("A", "B", libraryType.Book, 5, "10");

        when(stmt.executeUpdate()).thenThrow(new SQLException("x"));

        boolean result = repo.addItem(item);

        assertFalse(result);
    }


    @Test
    void findByName_foundRows() throws Exception {
        when(rs.next()).thenReturn(true, false);

        mockFullItemRow();

        List<Items> list = repo.findByName("test");

        assertEquals(1, list.size());
        assertEquals("AuthorX", list.get(0).getAuthor());
    }

    @Test
    void findByName_noRows() throws Exception {
        when(rs.next()).thenReturn(false);

        List<Items> list = repo.findByName("xxx");

        assertTrue(list.isEmpty());
    }

    @Test
    void findByName_exception() throws Exception {
        when(conn.prepareStatement(anyString())).thenThrow(new SQLException());

        List<Items> list = repo.findByName("fail");

        assertTrue(list.isEmpty());
    }


    @Test
    void findByAuthor_foundRows() throws Exception {
        when(rs.next()).thenReturn(true, false);
        mockFullItemRow();

        List<Items> list = repo.findByAuthor("A");

        assertEquals(1, list.size());
    }

    @Test
    void findByAuthor_noRows() throws Exception {
        when(rs.next()).thenReturn(false);

        List<Items> list = repo.findByAuthor("none");

        assertTrue(list.isEmpty());
    }

    @Test
    void findByAuthor_exception() throws Exception {
        when(conn.prepareStatement(anyString())).thenThrow(new SQLException());

        List<Items> list = repo.findByAuthor("fail");

        assertTrue(list.isEmpty());
    }


    @Test
    void findByISBN_found() throws Exception {
        when(rs.next()).thenReturn(true);

        mockFullItemRow();

        Optional<Items> item = repo.findByISBN(500);

        assertTrue(item.isPresent());
        assertEquals("AuthorX", item.get().getAuthor());
    }

    @Test
    void findByISBN_notFound() throws Exception {
        when(rs.next()).thenReturn(false);

        Optional<Items> item = repo.findByISBN(100);

        assertTrue(item.isEmpty());
    }

    @Test
    void findByISBN_exception() throws Exception {
        when(conn.prepareStatement(anyString())).thenThrow(new SQLException());

        Optional<Items> item = repo.findByISBN(100);

        assertTrue(item.isEmpty());
    }


    @Test
    void increaseQuantity_success() throws Exception {
        when(stmt.executeUpdate()).thenReturn(1);

        boolean result = repo.increaseQuantity(10);

        assertTrue(result);
    }

    @Test
    void increaseQuantity_noRowsUpdated() throws Exception {
        when(stmt.executeUpdate()).thenReturn(0);

        boolean result = repo.increaseQuantity(10);

        assertFalse(result);
    }

    @Test
    void increaseQuantity_exception() throws Exception {
        when(stmt.executeUpdate()).thenThrow(new SQLException());

        boolean result = repo.increaseQuantity(10);

        assertFalse(result);
    }


    private void mockFullItemRow() throws Exception {
        when(rs.getString("author")).thenReturn("AuthorX");
        when(rs.getString("name")).thenReturn("NameX");
        when(rs.getString("type")).thenReturn("Book");
        when(rs.getInt("quantity")).thenReturn(99);
        when(rs.getInt("isbn")).thenReturn(500);
    }
}
