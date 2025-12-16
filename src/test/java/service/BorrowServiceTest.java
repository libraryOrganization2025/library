package service;

import domain.Borrow;
import domain.Items;
import domain.libraryType;
import domain.strategyPattern.FineStrategy;
import domain.strategyPattern.FineStrategyFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import repository.BorrowRepository;
import repository.ItemsRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {
    private BorrowRepository borrowRepo;
    private ItemsRepository itemsRepo;
    private BorrowService borrowService;

    @BeforeEach
    void setUp() {
        borrowRepo = mock(BorrowRepository.class);
        itemsRepo = mock(ItemsRepository.class);
        borrowService = new BorrowService(borrowRepo, itemsRepo);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void hasUnpaidFine() {
        String email = "sara@gmail.com";
        when(borrowRepo.getTotalFine(email)).thenReturn(50);
        boolean result = borrowService.hasUnpaidFine(email);
        assertTrue(result);
        verify(borrowRepo, times(1)).getTotalFine(email);
    }

    @Test
    void hasUnpaidFineWithZeroFines() {
        String email = "sara@gmail.com";
        when(borrowRepo.getTotalFine(email)).thenReturn(0);
        boolean result = borrowService.hasUnpaidFine(email);
        assertFalse(result);
        verify(borrowRepo, times(1)).getTotalFine(email);
    }

    @Test
    void getTotalFine() {
        String email = "sara@gmail.com";
        when(borrowRepo.getTotalFine(email)).thenReturn(200);
        int totalFine = borrowService.getTotalFine(email);
        assertEquals(200, totalFine);
        verify(borrowRepo, times(1)).getTotalFine(email);
    }

    @Test
    void payFine() {
        String email = "sara@gmail.com";
        int amount = 100;
        borrowService.payFine(email, amount);
        verify(borrowRepo, times(1)).updateFineAfterPayment(email, amount);
    }

    @Test
    void payFineWithException() {
        String email = "sara@gmail.com";
        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> borrowService.payFine(email, 0)
        );
        assertEquals("Payment must be positive.", ex.getMessage());
    }

    @Test
    void borrowItemWithUnPaidFineException() {
        String email = "sara@gmail.com";
        int isbn = 100;
        when(borrowRepo.getTotalFine(email)).thenReturn(20);
        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> borrowService.borrowItem(email, isbn)
        );
        assertEquals("You have unpaid fines. Pay before borrowing.", ex.getMessage());
        verify(borrowRepo, times(1)).getTotalFine(email);
        verifyNoInteractions(itemsRepo);
        verify(borrowRepo, never()).borrowItem(any());
    }

    @Test
    void borrowItemWithNonFoundItemException() {
        String email = "sara@gmail.com";
        int isbn = 1000;
        when(borrowRepo.getTotalFine(email)).thenReturn(0);
        when(itemsRepo.findByISBN(isbn)).thenReturn(Optional.empty());
        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> borrowService.borrowItem(email, isbn)
        );

        assertEquals("Item not found", ex.getMessage());
        verify(borrowRepo, times(1)).getTotalFine(email);
        verify(itemsRepo, times(1)).findByISBN(isbn);
        verify(borrowRepo, never()).borrowItem(any());
    }

    @Test
    void borroItemSuccess() {
        String email = "sara@gmail.com";
        int isbn = 100;
        Items book = new Items(
                "Author",
                "Nice Book",
                libraryType.Book,
                5,
                String.valueOf(isbn)
        );
        when(borrowRepo.getTotalFine(email)).thenReturn(0);
        when(itemsRepo.findByISBN(isbn)).thenReturn(Optional.of(book));
        when(borrowRepo.borrowItem(any(Borrow.class))).thenReturn(true);
        boolean result = borrowService.borrowItem(email, isbn);
        assertTrue(result);
        verify(borrowRepo, times(1)).getTotalFine(email);
        verify(itemsRepo, times(1)).findByISBN(isbn);
        verify(borrowRepo, times(1)).borrowItem(any(Borrow.class));
    }

    @Test
    void returnItemWithUnPaidFineException() {
        String email = "sara@gmail.com";
        int isbn = 1;
        when(borrowRepo.getTotalFine(email)).thenReturn(20);
        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> borrowService.returnItem(email, isbn)
        );

        assertEquals("You have unpaid fines. Pay before returning items.", ex.getMessage());
        verify(borrowRepo, times(1)).getTotalFine(email);
        verify(borrowRepo, never()).findActiveBorrow(anyString(), anyInt());
        verifyNoInteractions(itemsRepo);
    }

    @Test
    void returnItemWithNonFoundIsbnException() {
        String email = "sara@gmail.com";
        int isbn = 1000;
        when(borrowRepo.getTotalFine(email)).thenReturn(0);
        when(itemsRepo.findByISBN(isbn)).thenReturn(null);
        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> borrowService.returnItem(email, isbn)
        );
        assertEquals("No active borrow found for this ISBN and student.", ex.getMessage());
        verify(borrowRepo, times(1)).getTotalFine(email);
        verify(borrowRepo, times(1)).findActiveBorrow(email, isbn);
        verifyNoInteractions(itemsRepo);
        verify(borrowRepo, never()).markReturnedByStudentAndIsbn(anyString(), anyInt(), anyInt());
    }

    @Test
    void returnItemSuccess() {
        String email = "sara@gmail.com";
        int isbn = 100;
        when(borrowRepo.getTotalFine(email)).thenReturn(0);
        LocalDate today = LocalDate.now();
        LocalDate borrowDate = today.minusDays(10);
        LocalDate overdueDate = today.minusDays(3);
        Borrow active = new Borrow(email, isbn, borrowDate, overdueDate, false);
        when(borrowRepo.findActiveBorrow(email, isbn)).thenReturn(active);
        Items book = new Items("Sara", "Sara's test", libraryType.Book, 5, String.valueOf(isbn));
        when(itemsRepo.findByISBN(isbn)).thenReturn(Optional.of(book));
        when(borrowRepo.markReturnedByStudentAndIsbn(eq(email), eq(isbn), anyInt()))
                .thenReturn(true);
        boolean result = borrowService.returnItem(email, isbn);
        assertTrue(result);
        verify(borrowRepo, times(1)).getTotalFine(email);
        verify(borrowRepo, times(1)).findActiveBorrow(email, isbn);
        verify(itemsRepo, times(1)).findByISBN(isbn);
        verify(borrowRepo, times(1))
                .markReturnedByStudentAndIsbn(eq(email), eq(isbn), anyInt());
        verify(itemsRepo, times(1)).increaseQuantity(isbn);

    }

    @Test
    void returnItemWithNonExistingException(){
        String email = "sara@gmail.com";
        int isbn = 1000;
        when(borrowRepo.getTotalFine(email)).thenReturn(0);
        LocalDate today = LocalDate.now();
        Borrow active = new Borrow(
                email,
                isbn,
                today.minusDays(10),
                today.minusDays(3),
                false
        );
        when(borrowRepo.findActiveBorrow(email, isbn)).thenReturn(active);
        when(itemsRepo.findByISBN(isbn)).thenReturn(Optional.empty());
        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> borrowService.returnItem(email, isbn)
        );
        assertEquals("Item not found when returning", ex.getMessage());
        verify(borrowRepo, times(1)).getTotalFine(email);
        verify(borrowRepo, times(1)).findActiveBorrow(email, isbn);
        verify(itemsRepo, times(1)).findByISBN(isbn);
        verify(borrowRepo, never()).markReturnedByStudentAndIsbn(anyString(), anyInt(), anyInt());
        verify(itemsRepo, never()).increaseQuantity(anyInt());
    }

    @Test
    void getOverdueStudents() {
        String email = "sara@gmail.com";
        int isbn = 11;
        Borrow b1 = new Borrow(
                email,
                isbn,
                LocalDate.now().minusDays(10),
                LocalDate.now().minusDays(3),
                false
        );
        List<Borrow> mockList = List.of(b1);
        when(borrowRepo.getOverdueUsers()).thenReturn(mockList);
        List<Borrow> result = borrowService.getOverdueStudents();
        assertEquals(1, result.size());
        assertEquals(mockList, result);
        verify(borrowRepo, times(1)).getOverdueUsers();
        verifyNoMoreInteractions(borrowRepo);
    }

    @Test
    void getStudentsWithUnpaidFines() {
        List<String> mockEmails = List.of(
                "sara@gmail.com",
                "lana@gmail.com"
        );
        when(borrowRepo.getStudentsWithUnpaidFines()).thenReturn(mockEmails);
        List<String> result = borrowService.getStudentsWithUnpaidFines();
        assertEquals(2, result.size());
        assertEquals(mockEmails, result);
        verify(borrowRepo, times(1)).getStudentsWithUnpaidFines();
        verifyNoMoreInteractions(borrowRepo);
    }
}