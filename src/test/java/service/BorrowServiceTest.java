package service;

import domain.Borrow;
import domain.Items;
import domain.libraryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.BorrowRepo;
import repository.ItemsRepo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BorrowServiceTest {

    @Mock
    private BorrowRepo borrowRepo;
    @Mock
    private ItemsRepo itemsRepo;

    private BorrowService borrowService;

    @BeforeEach
    void setUp() {
        borrowService = new BorrowService(borrowRepo, itemsRepo);
    }

    // --- BORROW ITEM TESTS ---

    @Test
    void borrowItem_Success_Book() {
        int isbn = 5555;
        String email = "student@test.com";
        // Using BOOK to test the 28-day logic branch
        Items mockItem = new Items("Author", "Book Name", libraryType.BOOK, 5, String.valueOf(isbn));

        when(borrowRepo.getTotalFine(email)).thenReturn(0);
        when(itemsRepo.findByISBN(isbn)).thenReturn(Optional.of(mockItem));
        when(borrowRepo.borrowItem(any(Borrow.class))).thenReturn(true);

        boolean result = borrowService.borrowItem(email, isbn);

        assertTrue(result);
        verify(borrowRepo).borrowItem(argThat(b ->
                b.getOverdueDate().equals(b.getBorrowDate().plusDays(28))
        ));
    }

    @Test
    void borrowItem_Fails_WhenUnpaidFineExists() {
        String email = "debtor@test.com";
        when(borrowRepo.getTotalFine(email)).thenReturn(50);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> borrowService.borrowItem(email, 123));

        assertEquals("You have unpaid fines. Pay before borrowing.", ex.getMessage());
        verify(itemsRepo, never()).findByISBN(anyInt());
    }

    @Test
    void borrowItem_ItemNotFound_ThrowsException() {
        int isbn = 999;
        String email = "student@test.com";
        when(borrowRepo.getTotalFine(email)).thenReturn(0);
        when(itemsRepo.findByISBN(isbn)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> borrowService.borrowItem(email, isbn));
    }

    // --- RETURN ITEM TESTS ---

    @Test
    void returnItem_Success_WithFineCalculation() {
        String email = "student@test.com";
        int isbn = 123;

        // Mocking an active borrow that is 5 days overdue
        LocalDate overdueDate = LocalDate.now().minusDays(5);
        Borrow mockBorrow = new Borrow(email, isbn, LocalDate.now().minusDays(10), overdueDate, false);

        Items mockItem = new Items("Author", "Item", libraryType.BOOK, 5, String.valueOf(isbn));

        when(borrowRepo.getTotalFine(email)).thenReturn(0);
        when(borrowRepo.findActiveBorrow(email, isbn)).thenReturn(mockBorrow);
        when(itemsRepo.findByISBN(isbn)).thenReturn(Optional.of(mockItem));
        when(borrowRepo.markReturnedByStudentAndIsbn(eq(email), eq(isbn), anyInt())).thenReturn(true);

        boolean result = borrowService.returnItem(email, isbn);

        assertTrue(result);
        verify(itemsRepo).increaseQuantity(isbn);
        // Verify that markReturned was called (Strategy pattern check)
        verify(borrowRepo).markReturnedByStudentAndIsbn(eq(email), eq(isbn), anyInt());
    }

    @Test
    void returnItem_ThrowsException_NoActiveBorrow() {
        String email = "student@test.com";
        when(borrowRepo.getTotalFine(email)).thenReturn(0);
        when(borrowRepo.findActiveBorrow(email, 123)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> borrowService.returnItem(email, 123));
    }

    // --- FINE TESTS ---

    @Test
    void payFine_Success() {
        borrowService.payFine("test@test.com", 100);
        verify(borrowRepo).updateFineAfterPayment("test@test.com", 100);
    }

    @Test
    void payFine_InvalidAmount_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> borrowService.payFine("test@test.com", -5));
    }

    @Test
    void getOverdueStudents_CallsRepo() {
        borrowService.getOverdueStudents();
        verify(borrowRepo).getOverdueUsers();
    }
}