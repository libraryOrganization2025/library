package repository;

import domain.Borrow;
import infrastructure.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository responsible for handling all database operations related to borrowing items.
 * <p>
 * This class manages:
 * <ul>
 *     <li>Borrowing items</li>
 *     <li>Returning items</li>
 *     <li>Updating fines</li>
 *     <li>Finding overdue borrow records</li>
 *     <li>Fetching active borrow rows</li>
 *     <li>Listing users with unpaid fines</li>
 * </ul>
 *
 * All database operations are executed using JDBC via {@link DatabaseConnection}.
 *
 * @author Shatha , Sara
 * @version 1.0
 */
public class BorrowRepository {

    /**
     * Saves a new borrow record and decreases item quantity.
     * <p>
     * The method runs as a transaction:
     * <ol>
     *     <li>Insert a borrow row</li>
     *     <li>Reduce item quantity by 1</li>
     *     <li>Update user's last borrow date</li>
     *     <li>Commit if all succeed, otherwise rollback</li>
     * </ol>
     *
     * @param borrow the borrow object containing student email, ISBN, and borrow dates
     * @return true if the operation completed successfully, false if item is out of stock or an error occurred
     */
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
                    conn.rollback();
                    return false;
                }
            }

            // 3. Update user's last borrow date
            try (PreparedStatement stmt3 = conn.prepareStatement(updateUserLastBorrowSql)) {
                stmt3.setDate(1, Date.valueOf(borrow.getBorrowDate()));
                stmt3.setString(2, borrow.getStudentEmail());
                stmt3.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            System.out.println("Error borrowing item: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves a list of all users who have overdue and unreturned items.
     *
     * @return a list of {@link Borrow} objects representing overdue borrow records
     */
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

    /**
     * Marks an item as returned and increases its quantity.
     * <p>
     * The operation runs in a transaction:
     * <ul>
     *     <li>Update returned = true</li>
     *     <li>Increase item quantity by 1</li>
     * </ul>
     *
     * @param returned the borrow object containing student email and ISBN
     * @return true if completed successfully, false otherwise
     */
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

            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(updateReturnedSql)) {
                stmt.setInt(1, returned.getIsbn());
                stmt.setString(2, returned.getStudentEmail());
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt2 = conn.prepareStatement(updateQuantitySql)) {
                stmt2.setInt(1, returned.getIsbn());
                stmt2.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            System.out.println("Error returning item: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates a user's fine after they make a payment.
     * <p>
     * The payment is applied in order of the oldest fine first (ascending by record ID).
     * Partial payments are supported.
     *
     * @param email       the student's email
     * @param paidAmount  the amount the student paid
     */
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
                            update.setInt(1, 0);
                            update.setInt(2, id);
                            update.executeUpdate();
                            remaining -= rowFine;
                        } else {
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

    /**
     * Calculates the total unpaid fines for a user.
     *
     * @param email student's email
     * @return total fine amount, or 0 if none or an error occurred
     */
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

    /**
     * Marks a specific borrow record (based on student and ISBN) as returned
     * and sets a final fine amount.
     *
     * @param email      the student's email
     * @param isbn       the item ISBN
     * @param fineToSet  the computed fine to assign to this borrow record
     * @return true if a record was found and updated, false otherwise
     */
    public boolean markReturnedByStudentAndIsbn(String email, int isbn, int fineToSet) {
        String findSql = "SELECT id FROM student_borrow WHERE student_email = ? AND item_isbn = ? AND returned = false ORDER BY borrow_date DESC LIMIT 1";
        String updateSql = "UPDATE student_borrow SET returned = true, fine = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement find = conn.prepareStatement(findSql)) {

            find.setString(1, email);
            find.setInt(2, isbn);
            ResultSet rs = find.executeQuery();

            if (!rs.next()) {
                return false;
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

    /**
     * Finds the most recent active (not returned) borrow record for a student and item.
     *
     * @param email student's email
     * @param isbn  item ISBN
     * @return a {@link Borrow} object if found, otherwise null
     */
    public Borrow findActiveBorrow(String email, int isbn) {
        String sql = "SELECT id, student_email, item_isbn, borrow_date, overdue_date, returned, fine "
                + "FROM student_borrow WHERE student_email = ? AND item_isbn = ? AND returned = false "
                + "ORDER BY borrow_date DESC LIMIT 1";

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

    /**
     * Retrieves a list of all students who currently have unpaid fines.
     *
     * @return a list of email addresses
     */
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
