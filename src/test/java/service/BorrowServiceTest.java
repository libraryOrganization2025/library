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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
        // Correctly wiring the service with mocks
        borrowService = new BorrowService(borrowRepo, itemsRepo);
    }

    @Test
    void borrowItemSuccess() {
        // Arrange
        int isbn = 5555;
        String email = "student@test.com";
        Items mockItem = new Items("Author", "Book Name", libraryType.Book, 5, String.valueOf(isbn));

        when(borrowRepo.getTotalFine(email)).thenReturn(0);
        when(itemsRepo.findByISBN(isbn)).thenReturn(Optional.of(mockItem));
        when(borrowRepo.borrowItem(any(Borrow.class))).thenReturn(true);

        // Act
        boolean result = borrowService.borrowItem(email, isbn);

        // Assert
        assertTrue(result);
        verify(borrowRepo).borrowItem(any(Borrow.class));
    }

    @Test
    void borrowItem_ItemNotFound_ThrowsException() {
        // Arrange
        int isbn = 999;
        String email = "student@test.com";
        when(borrowRepo.getTotalFine(email)).thenReturn(0);
        when(itemsRepo.findByISBN(isbn)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> borrowService.borrowItem(email, isbn));
    }
}