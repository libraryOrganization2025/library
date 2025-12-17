package service;

import domain.Borrow;
import domain.Items;
import domain.libraryType;
import repository.BorrowRepo;
import repository.BorrowRepository;
import repository.ItemsRepo;
import repository.ItemsRepository;
import domain.strategyPattern.FineStrategyFactory;
import domain.strategyPattern.FineStrategy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;


/**
 * Service class responsible for handling borrowing and returning items,
 * managing fines, and providing borrowing-related queries.
 * <p>
 * Interacts with {@link BorrowRepository} and {@link ItemsRepository} to perform
 * all necessary operations related to borrowing workflow.
 * </p>
 *
 * @author Shatha
 * @version 1.0
 */
public class BorrowService {

    private BorrowRepo borrowRepo;
    private ItemsRepo itemsRepo;

    /**
     * Constructs a {@link BorrowService} with the required repositories.
     *
     * @param borrowRepo repository for borrow-related operations
     * @param itemsRepo repository for item-related operations
     */
    public BorrowService(BorrowRepo borrowRepo, ItemsRepo itemsRepo) {
        this.borrowRepo = borrowRepo;
        this.itemsRepo = itemsRepo;
    }

    /**
     * Checks if a student has any unpaid fines.
     *
     * @param email the student's email
     * @return {@code true} if the student has unpaid fines, {@code false} otherwise
     */
    public boolean hasUnpaidFine(String email) {
        return borrowRepo.getTotalFine(email) > 0;
    }

    /**
     * Retrieves the total fine amount for a student.
     *
     * @param email the student's email
     * @return the total fine amount
     */
    public int getTotalFine(String email) {
        return borrowRepo.getTotalFine(email);
    }

    /**
     * Pays a specified amount towards a student's fine.
     *
     * @param email the student's email
     * @param amount the amount to pay (must be positive)
     * @throws IllegalArgumentException if the amount is zero or negative
     */
    public void payFine(String email, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Payment must be positive.");
        borrowRepo.updateFineAfterPayment(email, amount);
    }

    /**
     * Borrows an item for a student.
     * <p>
     * A student cannot borrow if they have unpaid fines. The borrowing period depends
     * on the item type: 28 days for books, 7 days for other types.
     * </p>
     *
     * @param studentEmail the student's email
     * @param isbn the ISBN of the item to borrow
     * @return {@code true} if the borrow was successfully registered, {@code false} otherwise
     * @throws IllegalArgumentException if the student has unpaid fines or the item is not found
     */
    public boolean borrowItem(String studentEmail, int isbn) {

        if (hasUnpaidFine(studentEmail)) {
            throw new IllegalArgumentException("You have unpaid fines. Pay before borrowing.");
        }

        Items item = itemsRepo.findByISBN(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        LocalDate today = LocalDate.now();
        LocalDate overdue = item.getType() == libraryType.BOOK ? today.plusDays(28) : today.plusDays(7);

        Borrow borrow = new Borrow(studentEmail, isbn, today, overdue, false);
        return borrowRepo.borrowItem(borrow);
    }

    /**
     * Returns a borrowed item for a student.
     * <p>
     * If the item is returned after the due date, a fine is calculated based on the item type.
     * The borrow record is marked as returned and item quantity is updated.
     * </p>
     *
     * @param studentEmail the student's email
     * @param isbn the ISBN of the item to return
     * @return {@code true} if the return was successfully processed, {@code false} otherwise
     * @throws IllegalArgumentException if the student has unpaid fines, no active borrow exists, or the item is not found
     */
    public boolean returnItem(String studentEmail, int isbn) {

        if (hasUnpaidFine(studentEmail)) {
            throw new IllegalArgumentException("You have unpaid fines. Pay before returning items.");
        }

        Borrow active = borrowRepo.findActiveBorrow(studentEmail, isbn);
        if (active == null) {
            throw new IllegalArgumentException("No active borrow found for this ISBN and student.");
        }

        LocalDate today = LocalDate.now();
        long overdueDays = 0;
        if (active.getOverdueDate() != null && active.getOverdueDate().isBefore(today)) {
            overdueDays = ChronoUnit.DAYS.between(active.getOverdueDate(), today);
        }

        Items item = itemsRepo.findByISBN(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Item not found when returning"));

        FineStrategy strategy = FineStrategyFactory.getStrategy(item.getType().name());
        int fine = strategy.calculateFine((int) overdueDays);

        boolean updated = borrowRepo.markReturnedByStudentAndIsbn(studentEmail, isbn, fine);

        if (updated) {
            itemsRepo.increaseQuantity(isbn);
        }

        return updated;
    }

    /**
     * Retrieves a list of all students who have overdue items.
     *
     * @return a list of {@link Borrow} objects representing overdue items
     */
    public List<Borrow> getOverdueStudents() {
        return borrowRepo.getOverdueUsers();
    }

    /**
     * Retrieves a list of student emails who currently have unpaid fines.
     *
     * @return a list of emails of students with unpaid fines
     */
    public List<String> getStudentsWithUnpaidFines() {
        return borrowRepo.getStudentsWithUnpaidFines();
    }

}
