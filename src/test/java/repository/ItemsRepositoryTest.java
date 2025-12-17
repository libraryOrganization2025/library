package repository;

import domain.Items;
import domain.libraryType;
import infrastructure.DatabaseConnection;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.sql.*;
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

    @Test
    void findByISBN_Success() throws Exception {
        // Arrange
        when(rs.next()).thenReturn(true);
        // Matching your mapItem() method logic
        when(rs.getString("author")).thenReturn("Author Name");
        when(rs.getString("name")).thenReturn("Book Title");
        when(rs.getString("type")).thenReturn("BOOK");
        when(rs.getInt("quantity")).thenReturn(5);
        when(rs.getInt("isbn")).thenReturn(12345);

        // Act
        Optional<Items> result = repo.findByISBN(12345);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Book Title", result.get().getName());
        assertEquals("12345", result.get().getISBN());
    }

    @Test
    void addItem_Success() throws Exception {
        // Arrange
        Items item = new Items("Author", "Title", libraryType.BOOK, 10, "111");
        when(stmt.executeUpdate()).thenReturn(1);

        // Act
        boolean result = repo.addItem(item);

        // Assert
        assertTrue(result);
        verify(stmt).executeUpdate();
    }

    @Test
    void increaseQuantity_Success() throws Exception {
        // Arrange
        when(stmt.executeUpdate()).thenReturn(1);

        // Act
        boolean result = repo.increaseQuantity(123);

        // Assert
        assertTrue(result);
    }
}