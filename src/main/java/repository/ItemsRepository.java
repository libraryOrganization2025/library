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
 */
public class ItemsRepository implements ItemsRepo{

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
