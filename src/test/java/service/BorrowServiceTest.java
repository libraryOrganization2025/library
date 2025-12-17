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

    @Test
    void getStudentsWithUnpaidFines_CallsRepo() {
        when(borrowRepo.getStudentsWithUnpaidFines())
                .thenReturn(List.of("a@test.com", "b@test.com"));

        List<String> result = borrowService.getStudentsWithUnpaidFines();

        assertEquals(2, result.size());
        verify(borrowRepo).getStudentsWithUnpaidFines();
    }

    @Test
    void getTotalFine_ReturnsValue() {
        when(borrowRepo.getTotalFine("x@test.com")).thenReturn(42);

        assertEquals(42, borrowService.getTotalFine("x@test.com"));
    }

    @Test
    void hasUnpaidFine_True() {
        when(borrowRepo.getTotalFine("x@test.com")).thenReturn(10);

        assertTrue(borrowService.hasUnpaidFine("x@test.com"));
    }

    @Test
    void returnItem_UpdateFails_NoQuantityIncrease() {
        String email = "student@test.com";
        int isbn = 333;

        Borrow borrow = new Borrow(
                email,
                isbn,
                LocalDate.now().minusDays(10),
                LocalDate.now().minusDays(5),
                false
        );

        Items item = new Items("Auth", "Book", libraryType.BOOK, 1, String.valueOf(isbn));

        when(borrowRepo.getTotalFine(email)).thenReturn(0);
        when(borrowRepo.findActiveBorrow(email, isbn)).thenReturn(borrow);
        when(itemsRepo.findByISBN(isbn)).thenReturn(Optional.of(item));
        when(borrowRepo.markReturnedByStudentAndIsbn(any(), anyInt(), anyInt()))
                .thenReturn(false);

        boolean result = borrowService.returnItem(email, isbn);

        assertFalse(result);
        verify(itemsRepo, never()).increaseQuantity(anyInt());
    }

    @Test
    void returnItem_NotOverdue_NoFine() {
        String email = "student@test.com";
        int isbn = 222;

        Borrow borrow = new Borrow(
                email,
                isbn,
                LocalDate.now().minusDays(2),
                LocalDate.now().plusDays(5), // not overdue
                false
        );

        Items item = new Items("Auth", "Book", libraryType.BOOK, 2, String.valueOf(isbn));

        when(borrowRepo.getTotalFine(email)).thenReturn(0);
        when(borrowRepo.findActiveBorrow(email, isbn)).thenReturn(borrow);
        when(itemsRepo.findByISBN(isbn)).thenReturn(Optional.of(item));
        when(borrowRepo.markReturnedByStudentAndIsbn(email, isbn, 0)).thenReturn(true);

        boolean result = borrowService.returnItem(email, isbn);

        assertTrue(result);
        verify(itemsRepo).increaseQuantity(isbn);
    }

    @Test
    void returnItem_ItemNotFound_ThrowsException() {
        String email = "student@test.com";
        int isbn = 555;

        Borrow borrow = new Borrow(email, isbn,
                LocalDate.now().minusDays(3),
                LocalDate.now().plusDays(3),
                false);

        when(borrowRepo.getTotalFine(email)).thenReturn(0);
        when(borrowRepo.findActiveBorrow(email, isbn)).thenReturn(borrow);
        when(itemsRepo.findByISBN(isbn)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> borrowService.returnItem(email, isbn)
        );
    }

    @Test
    void returnItem_UnpaidFine_ThrowsException() {
        when(borrowRepo.getTotalFine("debtor@test.com")).thenReturn(20);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> borrowService.returnItem("debtor@test.com", 123)
        );

        assertEquals(
                "You have unpaid fines. Pay before returning items.",
                ex.getMessage()
        );
    }

    @Test
    void borrowItem_Success_NonBook() {
        int isbn = 777;
        String email = "student@test.com";

        // Use ANY non-BOOK type that exists in your enum
        Items item = new Items(
                "Auth",
                "Some Item",
                libraryType.CD,   // ✅ NOT BOOK
                3,
                String.valueOf(isbn)
        );

        when(borrowRepo.getTotalFine(email)).thenReturn(0);
        when(itemsRepo.findByISBN(isbn)).thenReturn(Optional.of(item));
        when(borrowRepo.borrowItem(any(Borrow.class))).thenReturn(true);

        boolean result = borrowService.borrowItem(email, isbn);

        assertTrue(result);

        // 7-day logic branch
        verify(borrowRepo).borrowItem(argThat(b ->
                b.getOverdueDate().equals(b.getBorrowDate().plusDays(7))
        ));
    }

}