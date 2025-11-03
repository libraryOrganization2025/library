package repository;

import domain.Role;
import domain.user;
import infrastructure.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class userRepository {
    public boolean save(user user) {
        String sql = "INSERT INTO users (email, role, password_hash) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getEmail());
            stmt.setInt(2, user.getRole().getLevel());
            stmt.setString(3, user.getPasswordHash());
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Error inserting user: " + e.getMessage());
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<user> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                user foundUser = new user(
                        rs.getString("email"),
                        Role.fromLevel(rs.getInt("role")),
                        rs.getString("password_hash")
                );
                return Optional.of(foundUser);
            }
        } catch (Exception e) {
            System.out.println("Error finding user: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<user> findInactiveUsersSince(LocalDate oneYearAgo) {
        List<user> users = new ArrayList<>();

        String sql = """
                SELECT * FROM users
                WHERE deletedOn IS NULL
                  AND (
                       (lastdateborrowed IS NULL AND createdOn < ?)
                       OR (lastdateborrowed IS NOT NULL AND lastdateborrowed < ?)
                  )
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(oneYearAgo));
            stmt.setDate(2, Date.valueOf(oneYearAgo));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(new user(
                        rs.getString("email"),
                        Role.fromLevel(rs.getInt("role")),
                        rs.getString("password_hash"),
                        rs.getDate("lastdateborrowed") != null ? rs.getDate("lastdateborrowed").toLocalDate() : null,
                        rs.getDate("createdOn").toLocalDate().atStartOfDay(),
                        rs.getDate("deletedOn") != null ? rs.getDate("deletedOn").toLocalDate().atStartOfDay() : null
                ));
            }

        } catch (Exception e) {
            System.out.println("Error fetching inactive users: " + e.getMessage());
        }
        return users;
    }

    public boolean softDeleteInactiveUser(String email, LocalDate oneYearAgo) {
        String sql = """
                UPDATE users
                SET deletedOn = ?
                WHERE email = ?
                  AND deletedOn IS NULL
                  AND (
                      (lastdateborrowed IS NULL AND createdOn < ?)
                      OR (lastdateborrowed IS NOT NULL AND lastdateborrowed < ?)
                  )
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            stmt.setString(2, email);
            stmt.setDate(3, Date.valueOf(oneYearAgo));
            stmt.setDate(4, Date.valueOf(oneYearAgo));

            int affected = stmt.executeUpdate();
            return affected > 0;

        } catch (Exception e) {
            System.out.println("Error deleting inactive user: " + e.getMessage());
        }
        return false;
    }

    public boolean updateRole(String email, Role newRole) {
        String sql = "UPDATE users SET role = ? WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, String.valueOf(newRole.getLevel()));
            stmt.setString(2, email);

            int updated = stmt.executeUpdate();
            return updated > 0;

        } catch (Exception e) {
            System.out.println("Error updating user role: " + e.getMessage());
        }
        return false;
    }
}
