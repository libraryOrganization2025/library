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

public class ItemsRepositoryTest {

    private ItemsRepository repo;
    private Connection conn;
    private PreparedStatement stmt;
    private ResultSet rs;
    private MockedStatic<DatabaseConnection> dbMock;

    @BeforeEach
    void setup() throws Exception {
        repo = new ItemsRepository();
        conn = mock(Connection.class);
        stmt = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);

        dbMock = mockStatic(DatabaseConnection.class);
        dbMock.when(DatabaseConnection::getConnection).thenReturn(conn);

        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
    }

    @AfterEach
    void tearDown() {
        dbMock.close();
    }

    // --- ADD ITEM ---

    @Test
    void addItem_Success() throws Exception {
        Items item = new Items("Author", "Title", libraryType.BOOK, 10, "111");
        when(stmt.executeUpdate()).thenReturn(1);

        assertTrue(repo.addItem(item));
    }

    @Test
    void addItem_Exception_ReturnsFalse() throws Exception {
        Items item = new Items("Author", "Title", libraryType.BOOK, 10, "111");
        when(stmt.executeUpdate()).thenThrow(new SQLException("Duplicate Key"));

        assertFalse(repo.addItem(item));
    }

    // --- FIND BY NAME ---

    @Test
    void findByName_ReturnsList() throws Exception {
        when(rs.next()).thenReturn(true, true, false); // Return 2 items
        mockResultSet();

        List<Items> results = repo.findByName("Java");

        assertEquals(2, results.size());
        verify(stmt).setString(1, "%Java%");
    }

    @Test
    void findByName_Exception_ReturnsEmptyList() throws Exception {
        when(stmt.executeQuery()).thenThrow(new SQLException("Syntax Error"));

        List<Items> results = repo.findByName("Java");

        assertTrue(results.isEmpty());
    }

    // --- FIND BY AUTHOR ---


    @Test
    void findByAuthor_ReturnsList() throws Exception {
        // Arrange
        when(rs.next()).thenReturn(true, false);
        mockResultSet();

        // Act
        List<Items> results = repo.findByAuthor("Shatha");

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size(), "List size should be 1");
        assertEquals("Author Name", results.get(0).getAuthor());
    }

    // --- FIND BY ISBN ---

    @Test
    void findByISBN_Found() throws Exception {
        // 1. Setup the Mock
        when(rs.next()).thenReturn(true);
        when(rs.getString("author")).thenReturn("Author Name");
        when(rs.getString("name")).thenReturn("Book Title");

        // Ensure this matches your Enum EXACTLY (case-sensitive)
        when(rs.getString("type")).thenReturn("BOOK");

        when(rs.getInt("quantity")).thenReturn(5);
        when(rs.getInt("isbn")).thenReturn(12345);

        // 2. Act
        Optional<Items> result = repo.findByISBN(12345);

        // 3. Assert
        assertTrue(result.isPresent(), "Result should be present");

        // Use String quotes around "12345"
        assertEquals("12345", result.get().getISBN(), "ISBN mismatch");
    }

    @Test
    void findByISBN_NotFound() throws Exception {
        when(rs.next()).thenReturn(false);

        Optional<Items> result = repo.findByISBN(999);

        assertFalse(result.isPresent());
    }

    // --- INCREASE QUANTITY ---

    @Test
    void increaseQuantity_Success() throws Exception {
        when(stmt.executeUpdate()).thenReturn(1);
        assertTrue(repo.increaseQuantity(123));
    }

    @Test
    void increaseQuantity_NoRowUpdated_ReturnsFalse() throws Exception {
        when(stmt.executeUpdate()).thenReturn(0);
        assertFalse(repo.increaseQuantity(123));
    }

    @Test
    void increaseQuantity_Exception_ReturnsFalse() throws Exception {
        when(stmt.executeUpdate()).thenThrow(new SQLException("Deadlock"));
        assertFalse(repo.increaseQuantity(123));
    }

    /**
     * Helper to avoid repeating ResultSet mocking code
     */
    private void mockResultSet() throws SQLException {
        when(rs.getString("author")).thenReturn("Author Name");
        when(rs.getString("name")).thenReturn("Book Title");

        // FIX 1: Change "Book" to "BOOK" (or whatever matches your Enum exactly)
        when(rs.getString("type")).thenReturn("BOOK");

        when(rs.getInt("quantity")).thenReturn(5);
        when(rs.getInt("isbn")).thenReturn(12345);
    }

}