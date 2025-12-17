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
public class BorrowRepository implements BorrowRepo {

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
    @Override
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

            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, borrow.getStudentEmail());
                stmt.setInt(2, borrow.getIsbn());
                stmt.setDate(3, Date.valueOf(borrow.getBorrowDate()));
                stmt.setDate(4, Date.valueOf(borrow.getOverdueDate()));
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt2 = conn.prepareStatement(updateQuantitySql)) {
                stmt2.setInt(1, borrow.getIsbn());
                if (stmt2.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

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
     * Retrieves a list of borrow records for users with overdue items.
     *
     * @return list of overdue borrow records
     */
    @Override
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
     * Updates the fine amount for a student after a payment.
     *
     * @param email the student's email
     * @param paidAmount the amount paid
     */
    @Override
    public void updateFineAfterPayment(String email, int paidAmount) {
        // (same code you already wrote)
    }

    /**
     * Retrieves the total fine amount for a student.
     *
     * @param email the student's email
     * @return the total unpaid fine
     */
    @Override
    public int getTotalFine(String email) {
        // (same code you already wrote)
        return 0;
    }

    /**
     * Marks a borrowed item as returned by a student and sets the fine if applicable.
     *
     * @param email the student's email
     * @param isbn the ISBN of the borrowed item
     * @param fineToSet the fine to set after return
     * @return true if the operation was successful, false otherwise
     */
    @Override
    public boolean markReturnedByStudentAndIsbn(String email, int isbn, int fineToSet) {
        // (same code you already wrote)
        return false;
    }

    /**
     * Finds an active borrow record for a given student and ISBN.
     *
     * @param email the student's email
     * @param isbn the ISBN of the borrowed item
     * @return the active borrow record, or null if none exists
     */
    @Override
    public Borrow findActiveBorrow(String email, int isbn) {
        // (same code you already wrote)
        return null;
    }

    /**
     * Returns a list of student emails who have unpaid fines.
     *
     * @return list of student emails with unpaid fines
     */
    @Override
    public List<String> getStudentsWithUnpaidFines() {
        // (same code you already wrote)
        return new ArrayList<>();
    }

    /**
     * Marks a borrowed item as returned.
     *
     * @param returned the borrow record representing the returned item
     * @return true if the return was successfully processed, false otherwise
     */
    @Override
    public boolean returnItem(Borrow returned) {
        // (same code you already wrote)
        return false;
    }
}
