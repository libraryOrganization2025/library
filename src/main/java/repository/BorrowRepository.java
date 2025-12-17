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

    @Override
    public void updateFineAfterPayment(String email, int paidAmount) {
        // (same code you already wrote)
    }

    @Override
    public int getTotalFine(String email) {
        // (same code you already wrote)
        return 0;
    }

    @Override
    public boolean markReturnedByStudentAndIsbn(String email, int isbn, int fineToSet) {
        // (same code you already wrote)
        return false;
    }

    @Override
    public Borrow findActiveBorrow(String email, int isbn) {
        // (same code you already wrote)
        return null;
    }

    @Override
    public List<String> getStudentsWithUnpaidFines() {
        // (same code you already wrote)
        return new ArrayList<>();
    }

    @Override
    public boolean returnItem(Borrow returned) {
        // (same code you already wrote)
        return false;
    }
}
