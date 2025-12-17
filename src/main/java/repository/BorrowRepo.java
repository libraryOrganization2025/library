package repository;

import domain.Borrow;

import java.util.List;

/**
 * Repository interface for borrowing-related database operations.
 */
public interface BorrowRepo {

    boolean borrowItem(Borrow borrow);

    boolean returnItem(Borrow returned);

    void updateFineAfterPayment(String email, int paidAmount);

    int getTotalFine(String email);

    boolean markReturnedByStudentAndIsbn(String email, int isbn, int fineToSet);

    Borrow findActiveBorrow(String email, int isbn);

    List<Borrow> getOverdueUsers();

    List<String> getStudentsWithUnpaidFines();
}
