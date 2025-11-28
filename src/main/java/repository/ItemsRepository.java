package repository;

import domain.Items;
import domain.libraryType;
import infrastructure.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemsRepository {

    // ---------------------- SAVE ITEM ---------------------- //
    public boolean addItem(Items item) {
        String sql = "INSERT INTO items (author, name, type, quantity, isbn) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getAuthor());
            stmt.setString(2, item.getName());
            stmt.setString(3, item.getType().name());
            stmt.setInt(4, item.getQuantity());
            stmt.setString(5, item.getISBN());

            stmt.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("Error saving item: " + e.getMessage());
            return false;
        }
    }


    // ---------------------- SEARCH BY NAME ---------------------- //
    public List<Items> findByName(String name) {
        String sql = "SELECT * FROM items WHERE name LIKE ? AND deletedOn IS NULL";
        List<Items> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + name + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(mapItem(rs));
            }

        } catch (Exception e) {
            System.out.println("Error searching by name: " + e.getMessage());
        }
        return list;
    }


    // ---------------------- SEARCH BY AUTHOR ---------------------- //
    public List<Items> findByAuthor(String author) {
        String sql = "SELECT * FROM items WHERE author LIKE ? AND deletedOn IS NULL";
        List<Items> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + author + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(mapItem(rs));
            }

        } catch (Exception e) {
            System.out.println("Error searching by author: " + e.getMessage());
        }
        return list;
    }


    // ---------------------- SEARCH BY ISBN ---------------------- //
    public Optional<Items> findByISBN(String isbn) {
        String sql = "SELECT * FROM items WHERE isbn = ? AND deletedOn IS NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapItem(rs));
            }

        } catch (Exception e) {
            System.out.println("Error searching by ISBN: " + e.getMessage());
        }
        return Optional.empty();
    }


    // ---------------------- UPDATE ITEM ---------------------- //
    public boolean updateItem(String isbn, Items updated) {
        String sql = """
            UPDATE items
            SET author = ?, name = ?, type = ?, quantity = ?
            WHERE isbn = ? AND deletedOn IS NULL
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, updated.getAuthor());
            stmt.setString(2, updated.getName());
            stmt.setString(3, updated.getType().name());
            stmt.setInt(4, updated.getQuantity());
            stmt.setString(5, isbn);

            int changed = stmt.executeUpdate();
            return changed > 0;

        } catch (Exception e) {
            System.out.println("Error updating item: " + e.getMessage());
        }
        return false;
    }


    // ---------------------- SOFT DELETE ITEM ---------------------- //
    public boolean deleteItem(String isbn) {
        String sql = "UPDATE items SET deletedOn = CURDATE() WHERE isbn = ? AND deletedOn IS NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);

            int deleted = stmt.executeUpdate();
            return deleted > 0;

        } catch (Exception e) {
            System.out.println("Error deleting item: " + e.getMessage());
        }
        return false;
    }


    // ---------------------- MAP RESULTSET TO ITEM ---------------------- //
    private Items mapItem(ResultSet rs) throws SQLException {
        return new Items(
                rs.getString("author"),
                rs.getString("name"),
                libraryType.valueOf(rs.getString("type")),
                rs.getInt("quantity"),
                rs.getString("isbn")
        );
    }
}
