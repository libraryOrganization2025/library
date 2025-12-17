package service;

import domain.Borrow;
import domain.Items;
import domain.libraryType;
import domain.strategyPattern.FineStrategy;
import domain.strategyPattern.FineStrategyFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.BorrowRepository;
import repository.ItemsRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    @Mock
    private BorrowRepository borrowRepo;

    @Mock
    private ItemsRepository itemsRepo;

    @InjectMocks
    private BorrowService borrowService;

    @Test
    void hasUnpaidFine() {
        when(borrowRepo.getTotalFine("sara@gmail.com")).thenReturn(50);
        assertTrue(borrowService.hasUnpaidFine("sara@gmail.com"));
    }

    @Test
    void hasUnpaidFineWithZeroFines() {
        when(borrowRepo.getTotalFine("sara@gmail.com")).thenReturn(0);
        assertFalse(borrowService.hasUnpaidFine("sara@gmail.com"));
    }

    @Test
    void getTotalFine() {
        when(borrowRepo.getTotalFine("sara@gmail.com")).thenReturn(200);
        assertEquals(200, borrowService.getTotalFine("sara@gmail.com"));
    }

    @Test
    void payFine() {
        borrowService.payFine("sara@gmail.com", 100);
        verify(borrowRepo).updateFineAfterPayment("sara@gmail.com", 100);
    }

    @Test
    void payFineWithException() {
        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> borrowService.payFine("sara@gmail.com", 0)
        );
        assertEquals("Payment must be positive.", ex.getMessage());
    }

    @Test
    void borrowItemWithUnPaidFineException() {
        when(borrowRepo.getTotalFine("sara@gmail.com")).thenReturn(20);

        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> borrowService.borrowItem("sara@gmail.com", 100)
        );

        assertEquals("You have unpaid fines. Pay before borrowing.", ex.getMessage());
        verifyNoInteractions(itemsRepo);
    }

    @Test
    void borrowItemWithNonFoundItemException() {
        when(borrowRepo.getTotalFine("sara@gmail.com")).thenReturn(0);
        when(itemsRepo.findByISBN(1000)).thenReturn(Optional.empty());

        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> borrowService.borrowItem("sara@gmail.com", 1000)
        );

        assertEquals("Item not found", ex.getMessage());
    }

    @Test
    void borrowItemSuccess() {
        Items book = new Items("Author", "Nice Book", libraryType.Book, 5, "100");

        when(borrowRepo.getTotalFine("sara@gmail.com")).thenReturn(0);
        when(itemsRepo.findByISBN(100)).thenReturn(Optional.of(book));
        when(borrowRepo.borrowItem(any())).thenReturn(true);

        assertTrue(borrowService.borrowItem("sara@gmail.com", 100));
    }

    @Test
    void returnItemSuccess_withMockitoInlineStaticMock() {

        String email = "sara@gmail.com";
        int isbn = 100;

        Borrow active = new Borrow(
                email,
                isbn,
                LocalDate.now().minusDays(10),
                LocalDate.now().minusDays(3),
                false
        );

        Items book = new Items("Author", "Book", libraryType.Book, 5, "100");

        when(borrowRepo.getTotalFine(email)).thenReturn(0);
        when(borrowRepo.findActiveBorrow(email, isbn)).thenReturn(active);
        when(itemsRepo.findByISBN(isbn)).thenReturn(Optional.of(book));
        when(borrowRepo.markReturnedByStudentAndIsbn(eq(email), eq(isbn), anyInt()))
                .thenReturn(true);

        FineStrategy strategy = mock(FineStrategy.class);
        when(strategy.calculateFine(anyInt())).thenReturn(10);

        try (MockedStatic<FineStrategyFactory> mocked =
                     mockStatic(FineStrategyFactory.class)) {

            mocked.when(() ->
                            FineStrategyFactory.getStrategy(String.valueOf(libraryType.Book)))
                    .thenReturn(strategy);

            boolean result = borrowService.returnItem(email, isbn);
            assertTrue(result);
        }

        verify(itemsRepo).increaseQuantity(isbn);
    }

    @Test
    void getOverdueStudents() {
        Borrow b = new Borrow(
                "sara@gmail.com",
                11,
                LocalDate.now().minusDays(10),
                LocalDate.now().minusDays(3),
                false
        );

        when(borrowRepo.getOverdueUsers()).thenReturn(List.of(b));

        assertEquals(1, borrowService.getOverdueStudents().size());
    }

    @Test
    void getStudentsWithUnpaidFines() {
        List<String> emails = List.of("sara@gmail.com", "lana@gmail.com");

        when(borrowRepo.getStudentsWithUnpaidFines()).thenReturn(emails);

        assertEquals(2, borrowService.getStudentsWithUnpaidFines().size());
    }
}
