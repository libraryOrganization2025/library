package repository;

import domain.Borrow;

import java.util.List;

/**
 * Repository interface for borrowing-related operations in the library system.
 *
 * <p>This interface defines the contract for managing borrow records, including:
 * borrowing items, returning items, updating fines, and querying overdue or unpaid records.
 * Implementations of this interface are responsible for interacting with the database.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 *     BorrowRepo repo = new BorrowRepoImpl();
 *     boolean success = repo.borrowItem(borrow);
 * </pre>
 *
 * @author Shatha
 * @version 1.0
 */
public interface BorrowRepo {

    /**
     * Records a new borrowing of an item.
     *
     * @param borrow the borrow record to save
     * @return true if the borrow was successfully recorded, false otherwise
     */
    boolean borrowItem(Borrow borrow);

    /**
     * Marks an item as returned.
     *
     * @param returned the borrow record representing the returned item
     * @return true if the return was successfully processed, false otherwise
     */
    boolean returnItem(Borrow returned);

    /**
     * Updates the fine amount for a student after payment.
     *
     * @param email the student's email
     * @param paidAmount the amount paid
     */
    void updateFineAfterPayment(String email, int paidAmount);

    /**
     * Retrieves the total fine amount for a student.
     *
     * @param email the student's email
     * @return the total unpaid fine
     */
    int getTotalFine(String email);

    /**
     * Marks a borrowed item as returned by student and sets the fine if any.
     *
     * @param email the student's email
     * @param isbn the ISBN of the borrowed item
     * @param fineToSet the fine to set after return
     * @return true if operation was successful, false otherwise
     */
    boolean markReturnedByStudentAndIsbn(String email, int isbn, int fineToSet);

    /**
     * Finds an active borrow record for a given student and item.
     *
     * @param email the student's email
     * @param isbn the ISBN of the borrowed item
     * @return the active borrow record, or null if none exists
     */
    Borrow findActiveBorrow(String email, int isbn);

    /**
     * Returns a list of borrow records for users with overdue items.
     *
     * @return list of overdue borrow records
     */
    List<Borrow> getOverdueUsers();

    /**
     * Returns a list of student emails who have unpaid fines.
     *
     * @return list of student emails with unpaid fines
     */
    List<String> getStudentsWithUnpaidFines();
}
