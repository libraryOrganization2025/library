package service;

import domain.Items;
import domain.libraryType;
import repository.ItemsRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemsServiceTest {

    private ItemsRepository itemsRepository;
    private ItemsService itemsService;

    // --------------------------------------------------------
    //  Setup / Teardown
    // --------------------------------------------------------

    @BeforeEach
    void setUp() {
        itemsRepository = mock(ItemsRepository.class);
        itemsService = new ItemsService(itemsRepository);
    }

    @AfterEach
    void tearDown() {
    }

    // --------------------------------------------------------
    //  addNewItem() Tests
    // --------------------------------------------------------

    @Test
    void addNewItem_Success() {
        when(itemsRepository.addItem(any(Items.class))).thenReturn(true);

        boolean result = itemsService.addNewItem("Sara's Test", "Sara Taha", 10, libraryType.Book);

        assertTrue(result);
        verify(itemsRepository).addItem(any(Items.class));
    }

    @Test
    void addNewItem_BlankName_ThrowsException() {
        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> itemsService.addNewItem("   ", "Author", 5, libraryType.Book)
        );
        assertEquals("Item name cannot be empty.", ex.getMessage());
        verifyNoInteractions(itemsRepository);
    }

    @Test
    void addNewItem_BlankAuthor_ThrowsException() {
        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> itemsService.addNewItem("Title", " ", 5, libraryType.Book)
        );
        assertEquals("Author cannot be empty.", ex.getMessage());
        verifyNoInteractions(itemsRepository);
    }

    @Test
    void addNewItem_ZeroQuantity_ThrowsException() {
        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> itemsService.addNewItem("Title", "Author", 0, libraryType.Book)
        );
        assertEquals("Quantity must be greater than 0.", ex.getMessage());
        verifyNoInteractions(itemsRepository);
    }

    @Test
    void addNewItem_NegativeQuantity_ThrowsException() {
        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> itemsService.addNewItem("Title", "Author", -5, libraryType.Book)
        );
        assertEquals("Quantity must be greater than 0.", ex.getMessage());
        verifyNoInteractions(itemsRepository);
    }

    @Test
    void addNewItem_NullType_ThrowsException() {
        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> itemsService.addNewItem("Title", "Author", 5, null)
        );
        assertEquals("Type (BOOK / CD) is required.", ex.getMessage());
        verifyNoInteractions(itemsRepository);
    }

    @Test
    void addNewItem_NullName_ThrowsException() {
        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> itemsService.addNewItem(null, "Author", 5, libraryType.Book)
        );
        assertEquals("Item name cannot be empty.", ex.getMessage());
        verifyNoInteractions(itemsRepository);
    }

    @Test
    void addNewItem_NullAuthor_ThrowsException() {
        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> itemsService.addNewItem("Title", null, 5, libraryType.Book)
        );
        assertEquals("Author cannot be empty.", ex.getMessage());
        verifyNoInteractions(itemsRepository);
    }

    // --------------------------------------------------------
    //  increaseQuantityByISBN()
    // --------------------------------------------------------

    @Test
    void increaseQuantityByISBN_Success() {
        int isbn = 123;
        Items existingItem = new Items("Author", "Title", libraryType.Book, 5, "");

        when(itemsRepository.findByISBN(isbn)).thenReturn(Optional.of(existingItem));
        when(itemsRepository.increaseQuantity(isbn)).thenReturn(true);

        boolean result = itemsService.increaseQuantityByISBN("123");

        assertTrue(result);
        verify(itemsRepository).findByISBN(isbn);
        verify(itemsRepository).increaseQuantity(isbn);
    }

    @Test
    void increaseQuantityByISBN_Null_ThrowsException() {
        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> itemsService.increaseQuantityByISBN(null)
        );
        assertEquals("ISBN cannot be empty.", ex.getMessage());
        verifyNoInteractions(itemsRepository);
    }

    @Test
    void increaseQuantityByISBN_Blank_ThrowsException() {
        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> itemsService.increaseQuantityByISBN("   ")
        );
        assertEquals("ISBN cannot be empty.", ex.getMessage());
        verifyNoInteractions(itemsRepository);
    }

    @Test
    void increaseQuantityByISBN_NotNumber_ThrowsException() {
        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> itemsService.increaseQuantityByISBN("abc")
        );
        assertEquals("ISBN must be a number.", ex.getMessage());
        verifyNoInteractions(itemsRepository);
    }

    @Test
    void increaseQuantityByISBN_NotFound_ThrowsException() {
        when(itemsRepository.findByISBN(5000)).thenReturn(Optional.empty());

        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> itemsService.increaseQuantityByISBN("5000")
        );

        assertEquals("No item found with this ISBN.", ex.getMessage());
        verify(itemsRepository).findByISBN(5000);
        verify(itemsRepository, never()).increaseQuantity(anyInt());
    }

    // --------------------------------------------------------
    //  searchByISBN()
    // --------------------------------------------------------

    @Test
    void searchByISBN_Success() {
        Items mockItem = new Items("Author", "Title", libraryType.Book, 10, "123");
        when(itemsRepository.findByISBN(123)).thenReturn(Optional.of(mockItem));

        Items result = itemsService.searchByISBN("123");

        assertEquals(mockItem, result);
        verify(itemsRepository).findByISBN(123);
    }

    @Test
    void searchByISBN_Null_ThrowsException() {
        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> itemsService.searchByISBN(null)
        );
        assertEquals("ISBN cannot be empty.", ex.getMessage());
        verifyNoInteractions(itemsRepository);
    }

    @Test
    void searchByISBN_Blank_ThrowsException() {
        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> itemsService.searchByISBN("   ")
        );
        assertEquals("ISBN cannot be empty.", ex.getMessage());
        verifyNoInteractions(itemsRepository);
    }

    @Test
    void searchByISBN_NotNumber_ThrowsException() {
        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> itemsService.searchByISBN("abc")
        );
        assertEquals("ISBN must be a number.", ex.getMessage());
        verifyNoInteractions(itemsRepository);
    }

    @Test
    void searchByISBN_NotFound_ThrowsException() {
        when(itemsRepository.findByISBN(5000)).thenReturn(Optional.empty());

        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> itemsService.searchByISBN("5000")
        );

        assertEquals("No item found with this ISBN.", ex.getMessage());
        verify(itemsRepository).findByISBN(5000);
    }

    // --------------------------------------------------------
    //  searchByName() — General
    // --------------------------------------------------------

    @Test
    void searchByName_AllTypesReturned_WhenTypeIsNull() {
        String name = "Sara";
        List<Items> mockList = List.of(
                new Items("A1", "Sara", libraryType.Book, 3, "1"),
                new Items("A2", "Sara", libraryType.CD, 2, "2")
        );

        when(itemsRepository.findByName(name)).thenReturn(mockList);

        List<Items> result = itemsService.searchByName(name, null);

        assertEquals(2, result.size());
        verify(itemsRepository).findByName(name);
    }

    @Test
    void searchByName_FiltersWrongType() {
        List<Items> list = List.of(
                new Items("A1", "Sara", libraryType.Book, 3, "1"),
                new Items("A2", "Sara", libraryType.CD, 2, "2")
        );

        when(itemsRepository.findByName("Sara")).thenReturn(list);

        List<Items> result = itemsService.searchByName("Sara", libraryType.Book);

        assertEquals(1, result.size());
        assertEquals(libraryType.Book, result.get(0).getType());
    }

    @Test
    void searchByName_EmptyResults() {
        when(itemsRepository.findByName("Nothing")).thenReturn(List.of());

        List<Items> result = itemsService.searchByName("Nothing", null);

        assertTrue(result.isEmpty());
    }

    @Test
    void searchByName_FilterReturnsEmpty() {
        List<Items> list = List.of(
                new Items("A1", "Sara", libraryType.CD, 3, "1")
        );

        when(itemsRepository.findByName("Sara")).thenReturn(list);

        List<Items> result = itemsService.searchByName("Sara", libraryType.Book);

        assertTrue(result.isEmpty());
    }

    // --------------------------------------------------------
    //  searchByAuthor() — General
    // --------------------------------------------------------

    @Test
    void searchByAuthor_AllTypesReturned_WhenTypeNull() {
        String author = "Sara";
        List<Items> list = List.of(
                new Items("Sara", "Book1", libraryType.Book, 3, "1"),
                new Items("Sara", "CD1", libraryType.CD, 5, "2")
        );

        when(itemsRepository.findByAuthor(author)).thenReturn(list);

        List<Items> result = itemsService.searchByAuthor(author, null);

        assertEquals(2, result.size());
    }

    @Test
    void searchByAuthor_FilterCorrectly() {
        String author = "Sara";
        List<Items> list = List.of(
                new Items("Sara", "Book1", libraryType.Book, 3, "1"),
                new Items("Sara", "CD1", libraryType.CD, 5, "2")
        );

        when(itemsRepository.findByAuthor(author)).thenReturn(list);

        List<Items> result = itemsService.searchByAuthor(author, libraryType.CD);

        assertEquals(1, result.size());
        assertEquals(libraryType.CD, result.get(0).getType());
    }

    @Test
    void searchByAuthor_NoResults() {
        when(itemsRepository.findByAuthor("Nobody")).thenReturn(List.of());

        List<Items> result = itemsService.searchByAuthor("Nobody", null);

        assertTrue(result.isEmpty());
    }

    @Test
    void searchByAuthor_FilterReturnsEmpty() {
        List<Items> list = List.of(
                new Items("Sara", "Title", libraryType.Book, 3, "1")
        );

        when(itemsRepository.findByAuthor("Sara")).thenReturn(list);

        List<Items> result = itemsService.searchByAuthor("Sara", libraryType.CD);

        assertTrue(result.isEmpty());
    }

    // --------------------------------------------------------
    //  searchBooks & searchCDs — By Name
    // --------------------------------------------------------

    @Test
    void searchBooksByName_Success() {
        List<Items> list = List.of(
                new Items("A1", "Sara", libraryType.Book, 3, "1")
        );
        when(itemsRepository.findByName("Sara")).thenReturn(list);

        List<Items> result = itemsService.searchBooksByName("Sara");

        assertEquals(1, result.size());
        assertEquals(libraryType.Book, result.get(0).getType());
    }

    @Test
    void searchBooksByName_Empty() {
        when(itemsRepository.findByName("Sara")).thenReturn(List.of());

        List<Items> result = itemsService.searchBooksByName("Sara");

        assertTrue(result.isEmpty());
    }

    @Test
    void searchCDsByName_Success() {
        List<Items> list = List.of(
                new Items("A1", "Sara", libraryType.CD, 3, "1")
        );
        when(itemsRepository.findByName("Sara")).thenReturn(list);

        List<Items> result = itemsService.searchCDsByName("Sara");

        assertEquals(1, result.size());
        assertEquals(libraryType.CD, result.get(0).getType());
    }

    @Test
    void searchCDsByName_Empty() {
        when(itemsRepository.findByName("Sara")).thenReturn(List.of());

        List<Items> result = itemsService.searchCDsByName("Sara");

        assertTrue(result.isEmpty());
    }

    // --------------------------------------------------------
    //  searchBooks & searchCDs — By Author
    // --------------------------------------------------------

    @Test
    void searchBooksByAuthor_Success() {
        List<Items> list = List.of(
                new Items("Sara", "Book1", libraryType.Book, 3, "1")
        );
        when(itemsRepository.findByAuthor("Sara")).thenReturn(list);

        List<Items> result = itemsService.searchBooksByAuthor("Sara");

        assertEquals(1, result.size());
        assertEquals(libraryType.Book, result.get(0).getType());
    }

    @Test
    void searchBooksByAuthor_Empty() {
        when(itemsRepository.findByAuthor("Sara")).thenReturn(List.of());

        List<Items> result = itemsService.searchBooksByAuthor("Sara");

        assertTrue(result.isEmpty());
    }

    @Test
    void searchCDsByAuthor_Success() {
        List<Items> list = List.of(
                new Items("Sara", "CD1", libraryType.CD, 5, "2")
        );
        when(itemsRepository.findByAuthor("Sara")).thenReturn(list);

        List<Items> result = itemsService.searchCDsByAuthor("Sara");

        assertEquals(1, result.size());
        assertEquals(libraryType.CD, result.get(0).getType());
    }

    @Test
    void searchCDsByAuthor_Empty() {
        when(itemsRepository.findByAuthor("Sara")).thenReturn(List.of());

        List<Items> result = itemsService.searchCDsByAuthor("Sara");

        assertTrue(result.isEmpty());
    }
}
