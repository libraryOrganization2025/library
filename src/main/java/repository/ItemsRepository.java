package repository;

import domain.Items;
import domain.libraryType;
import infrastructure.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository class responsible for performing CRUD operations on items stored
 * in the database. Supports adding items, searching by name/author/ISBN, and
 * updating quantities.
 *
 * @author Shatha , Sara
 * @version 1.0
 */
public class ItemsRepository {

    /**
     * Inserts a new item into the database.
     *
     * @param item the {@link Items} object to save
     * @return true if the insert succeeded, false otherwise
     */
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
     * Searches for items with names matching the provided string (partial match allowed).
     *
     * @param name the search keyword for item name
     * @return list of items whose names match the pattern
     */
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
     * Searches for items by author name (partial match allowed).
     *
     * @param author the author keyword to search for
     * @return list of items written by the matching author(s)
     */
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
     * Finds a specific item by its ISBN.
     *
     * @param isbn the unique ISBN of the item
     * @return an {@link Optional} containing the found item, or empty if none found
     */
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
     * Increases the quantity of a given item by 1.
     *
     * @param isbn the ISBN of the item whose quantity should increase
     * @return true if successfully updated, false if the item does not exist or error occurs
     */
    public boolean increaseQuantity(int isbn) {
        String sql = "UPDATE items SET quantity = quantity + 1 WHERE isbn = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, isbn);
            int updated = stmt.executeUpdate();
            return updated > 0;

        } catch (Exception e) {
            System.out.println("Error increasing quantity: " + e.getMessage());
            return false;
        }
    }

    /**
     * Maps a single SQL {@link ResultSet} row into an {@link Items} object.
     *
     * @param rs the result set positioned at a row
     * @return the mapped {@link Items}
     * @throws SQLException if reading the database columns fails
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
