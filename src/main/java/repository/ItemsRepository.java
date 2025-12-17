package repository;

import domain.Items;
import domain.libraryType;
import infrastructure.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link ItemsRepo}.
 *
 * <p>This repository class handles all database operations related to library items,
 * including adding new items, searching by name, author, or ISBN, and updating item quantity.
 *
 * <p>All database interactions are performed using JDBC through {@link DatabaseConnection}.
 *
 * <p>Usage example:
 * <pre>
 *     ItemsRepo repo = new ItemsRepository();
 *     Items item = new Items("Author Name", "Book Title", libraryType.BOOK, 5, "12345");
 *     repo.addItem(item);
 * </pre>
 *
 * @author Shatha , Sara
 * @version 1.0
 */
public class ItemsRepository implements ItemsRepo{

    /**
     * Adds a new library item to the database.
     *
     * @param item the item to add
     * @return true if the item was successfully saved, false otherwise
     */
    @Override
    public boolean addItem(Items item) {
        String sql = "INSERT INTO items (author, name, type, quantity) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getAuthor());
            stmt.setString(2, item.getName());
            stmt.setString(3, item.getType().name());
            stmt.setInt(4, item.getQuantity());

            stmt.executeUpdate();
            return true;

        } catch (Exception e) {
            System.out.println("Error saving item: " + e.getMessage());
            return false;
        }
    }

    /**
     * Finds items whose name contains the specified string (case-insensitive).
     *
     * @param name the string to search in item names
     * @return list of matching items
     */
    @Override
    public List<Items> findByName(String name) {
        String sql = "SELECT * FROM items WHERE name LIKE ?";
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

    /**
     * Finds items whose author contains the specified string (case-insensitive).
     *
     * @param author the string to search in authors
     * @return list of matching items
     */
    @Override
    public List<Items> findByAuthor(String author) {
        String sql = "SELECT * FROM items WHERE author LIKE ?";
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

    /**
     * Finds an item by its ISBN.
     *
     * @param isbn the ISBN to search
     * @return an Optional containing the item if found, or empty if not found
     */
    @Override
    public Optional<Items> findByISBN(int isbn) {
        String sql = "SELECT * FROM items WHERE isbn = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, isbn);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapItem(rs));
            }

        } catch (Exception e) {
            System.out.println("Error searching by ISBN: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Increases the quantity of an item by 1.
     *
     * @param isbn the ISBN of the item
     * @return true if the quantity was successfully updated, false otherwise
     */
    @Override
    public boolean increaseQuantity(int isbn) {
        String sql = "UPDATE items SET quantity = quantity + 1 WHERE isbn = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, isbn);
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Error increasing quantity: " + e.getMessage());
            return false;
        }
    }

    /**
     * Maps a JDBC ResultSet row to an {@link Items} object.
     *
     * @param rs the ResultSet pointing to a row
     * @return the corresponding Items object
     * @throws SQLException if an SQL error occurs while reading the ResultSet
     */
    private Items mapItem(ResultSet rs) throws SQLException {
        return new Items(
                rs.getString("author"),
                rs.getString("name"),
                libraryType.valueOf(rs.getString("type")),
                rs.getInt("quantity"),
                String.valueOf(rs.getInt("isbn"))
        );
    }
}
