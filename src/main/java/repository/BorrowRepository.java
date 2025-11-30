package repository;

import domain.Borrow;
import infrastructure.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowRepository {

    public boolean borrowItem(Borrow borrow) {

        String insertSql = """
        INSERT INTO student_borrow (student_email, item_isbn, borrow_date, overdue_date, returned)
        VALUES (?, ?, ?, ?, false)
    """;

        String updateQuantitySql = """
        UPDATE items
        SET quantity = quantity - 1
        WHERE isbn = ? AND quantity > 0
    """;

        try (Connection conn = DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, borrow.getStudentEmail());
                stmt.setInt(2, borrow.getIsbn());
                stmt.setDate(3, Date.valueOf(borrow.getBorrowDate()));
                stmt.setDate(4, Date.valueOf(borrow.getOverdueDate()));
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt2 = conn.prepareStatement(updateQuantitySql)) {
                stmt2.setInt(1, borrow.getIsbn());
                int rowsUpdated = stmt2.executeUpdate();

                if (rowsUpdated == 0) {
                    conn.rollback();
                    System.out.println("Item out of stock");
                    return false;
                }
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            System.out.println("Error borrowing item: " + e.getMessage());
            return false;
        }
    }

    public List<Borrow> getOverdueUsers() {
        String sql = "SELECT * FROM student_borrow WHERE overdue_date < CURDATE() AND returned = false";
        List<Borrow> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new Borrow(
                        rs.getString("student_email"),
                        rs.getInt("item_isbn"),
                        rs.getDate("borrow_date").toLocalDate(),
                        rs.getDate("overdue_date").toLocalDate(),
                        rs.getBoolean("returned")
                ));
            }

        } catch (Exception e) {
            System.out.println("Error fetching overdue users: " + e.getMessage());
        }
        return list;
    }

    public List<Borrow> getActiveBorrowsByStudent(String email) {
        String sql = "SELECT * FROM student_borrow WHERE student_email = ? AND returned = false";
        List<Borrow> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new Borrow(
                        rs.getString("student_email"),
                        rs.getInt("item_isbn"),
                        rs.getDate("borrow_date").toLocalDate(),
                        rs.getDate("overdue_date").toLocalDate(),
                        rs.getBoolean("returned")
                ));
            }

        } catch (Exception e) {
            System.out.println("Error fetching active borrows: " + e.getMessage());
        }
        return list;
    }

    public boolean returnBorrow(Borrow borrow) {
        String updateBorrowSql = """
        UPDATE student_borrow
        SET returned = true
        WHERE student_email = ? AND item_isbn = ? AND borrow_date = ? AND returned = false
    """;

        String updateQuantitySql = """
        UPDATE items
        SET quantity = quantity + 1
        WHERE isbn = ?
    """;

        try (Connection conn = DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            int rowsBorrow;
            try (PreparedStatement stmt = conn.prepareStatement(updateBorrowSql)) {
                stmt.setString(1, borrow.getStudentEmail());
                stmt.setInt(2, borrow.getIsbn());
                stmt.setDate(3, Date.valueOf(borrow.getBorrowDate()));
                rowsBorrow = stmt.executeUpdate();
            }

            if (rowsBorrow == 0) {
                conn.rollback();
                return false;
            }

            try (PreparedStatement stmt2 = conn.prepareStatement(updateQuantitySql)) {
                stmt2.setInt(1, borrow.getIsbn());
                stmt2.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            System.out.println("Error returning item: " + e.getMessage());
            return false;
        }
    }
}
