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

        String updateUserLastBorrowSql = """
        UPDATE users
        SET lastdateborrowed = ?
        WHERE email = ?
    """;

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Start transaction
            conn.setAutoCommit(false);

            // 1. Insert borrow record
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, borrow.getStudentEmail());
                stmt.setInt(2, borrow.getIsbn());
                stmt.setDate(3, Date.valueOf(borrow.getBorrowDate()));
                stmt.setDate(4, Date.valueOf(borrow.getOverdueDate()));
                stmt.executeUpdate();
            }

            // 2. Decrease quantity
            try (PreparedStatement stmt2 = conn.prepareStatement(updateQuantitySql)) {
                stmt2.setInt(1, borrow.getIsbn());
                int rowsUpdated = stmt2.executeUpdate();

                if (rowsUpdated == 0) {
                    // Quantity was already 0 → rollback
                    conn.rollback();
                    System.out.println("Item out of stock");
                    return false;
                }
            }

            try (PreparedStatement stmt3 = conn.prepareStatement(updateUserLastBorrowSql)) {
                stmt3.setDate(1, Date.valueOf(borrow.getBorrowDate())); // أو Date.valueOf(LocalDate.now())
                stmt3.setString(2, borrow.getStudentEmail());
                stmt3.executeUpdate();
            }

            // Commit both operations
            conn.commit();
            return true;

        } catch (Exception e) {
            System.out.println("Error borrowing item: " + e.getMessage());
            return false;
        }
    }

    public List<Borrow> getOverdueUsers() {
        String sql = "SELECT * FROM student_borrow WHERE overdue_date < CURRENT_DATE AND returned = false";
        List<Borrow> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();

             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new Borrow(
                        rs.getInt("id"),
                        rs.getString("student_email"),
                        rs.getInt("item_isbn"),
                        rs.getDate("borrow_date").toLocalDate(),
                        rs.getDate("overdue_date").toLocalDate(),
                        rs.getBoolean("returned"),
                        rs.getInt("fine")
                ));
            }

        } catch (Exception e) {
            System.out.println("Error fetching overdue users: " + e.getMessage());
        }
        return list;
    }

    public boolean returnItem(Borrow returned) {

        String updateReturnedSql = """
        UPDATE student_borrow
        SET returned = True
        WHERE item_isbn = ? AND student_email = ?
    """;

        String updateQuantitySql = """
        UPDATE items
        SET quantity = quantity + 1
        WHERE isbn = ? AND quantity > 0
    """;

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Start transaction
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(updateReturnedSql)) {
                stmt.setInt(1, returned.getIsbn());
                stmt.setString(2, returned.getStudentEmail());
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt2 = conn.prepareStatement(updateQuantitySql)) {
                stmt2.setInt(1, returned.getIsbn());
                int rowsUpdated = stmt2.executeUpdate();

            }

            // Commit both operations
            conn.commit();
            return true;

        } catch (Exception e) {
            System.out.println("Error returning item: " + e.getMessage());
            return false;
        }
    }

    public void updateFineAfterPayment(String email, int paidAmount) {
        String selectSql = "SELECT id, fine FROM student_borrow WHERE student_email = ? AND fine > 0 ORDER BY id ASC";
        String updateSql = "UPDATE student_borrow SET fine = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement select = conn.prepareStatement(selectSql)) {
                select.setString(1, email);
                ResultSet rs = select.executeQuery();

                try (PreparedStatement update = conn.prepareStatement(updateSql)) {
                    int remaining = paidAmount;

                    while (rs.next() && remaining > 0) {
                        int id = rs.getInt("id");
                        int rowFine = rs.getInt("fine");

                        if (remaining >= rowFine) {
                            // pay full rowFine
                            update.setInt(1, 0);
                            update.setInt(2, id);
                            update.executeUpdate();
                            remaining -= rowFine;
                        } else {
                            // partial payment for this row
                            update.setInt(1, rowFine - remaining);
                            update.setInt(2, id);
                            update.executeUpdate();
                            remaining = 0;
                        }
                    }
                }
            }

            conn.commit();
        } catch (Exception e) {
            System.out.println("Error paying fine: " + e.getMessage());
        }
    }

    public int getTotalFine(String email) {
        String sql = "SELECT SUM(fine) AS total FROM student_borrow WHERE student_email = ? AND fine > 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("total");
        } catch (Exception e) {
            System.out.println("Error checking fine: " + e.getMessage());
        }
        return 0;
    }

    public boolean markReturnedByStudentAndIsbn(String email, int isbn, int fineToSet) {
        String findSql = "SELECT id FROM student_borrow WHERE student_email = ? AND item_isbn = ? AND returned = false ORDER BY borrow_date DESC LIMIT 1";
        String updateSql = "UPDATE student_borrow SET returned = true, fine = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement find = conn.prepareStatement(findSql)) {

            find.setString(1, email);
            find.setInt(2, isbn);
            ResultSet rs = find.executeQuery();

            if (!rs.next()) {
                return false; // no active borrow found
            }

            int id = rs.getInt("id");

            try (PreparedStatement update = conn.prepareStatement(updateSql)) {
                update.setInt(1, fineToSet);
                update.setInt(2, id);
                update.executeUpdate();
                return true;
            }

        } catch (Exception e) {
            System.out.println("Error returning item: " + e.getMessage());
            return false;
        }
    }

    // Get the most recent active borrow row for a student/isbn (to compute overdueDays etc.)
    public Borrow findActiveBorrow(String email, int isbn) {
        String sql = "SELECT id, student_email, item_isbn, borrow_date, overdue_date, returned, fine " +
                "FROM student_borrow WHERE student_email = ? AND item_isbn = ? AND returned = false ORDER BY borrow_date DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setInt(2, isbn);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Borrow(
                        rs.getInt("id"),
                        rs.getString("student_email"),
                        rs.getInt("item_isbn"),
                        rs.getDate("borrow_date").toLocalDate(),
                        rs.getDate("overdue_date").toLocalDate(),
                        rs.getBoolean("returned"),
                        rs.getInt("fine")
                );
            }

        } catch (Exception e) {
            System.out.println("Error finding active borrow: " + e.getMessage());
        }
        return null;
    }

    public List<String> getStudentsWithUnpaidFines() {
        String sql = "SELECT DISTINCT student_email FROM student_borrow WHERE fine > 0";
        List<String> emails = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                emails.add(rs.getString("student_email"));
            }

        } catch (Exception e) {
            System.out.println("Error fetching students with unpaid fines: " + e.getMessage());
        }

        return emails;
    }

}
