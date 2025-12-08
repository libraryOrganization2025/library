package service;

import domain.*;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import service.menuService;

import java.io.*;
import java.util.List;
import java.util.Scanner;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class MenuServiceTest {

    private userService mockUserService;
    private ItemsService mockItemsService;
    private BorrowService mockBorrowService;
    private EmailService mockEmailService;

    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        mockUserService = mock(userService.class);
        mockItemsService = mock(ItemsService.class);
        mockBorrowService = mock(BorrowService.class);
        mockEmailService = mock(EmailService.class);

        outContent = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // Helper to create menuService with input
    private menuService createMenu(String input) {
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        return spy(new menuService(scanner, mockUserService, mockItemsService, mockBorrowService, mockEmailService));
    }

    // ===================== showMainMenu =====================
    @Test
    void testMainMenuSignUp() throws Exception {
        menuService menu = createMenu("1\n3\n");
        doNothing().when(menu).handleSignUp();

        menu.showMainMenu();

        verify(menu).handleSignUp();
        assertTrue(outContent.toString().contains("Welcome to the Library System"));
        assertTrue(outContent.toString().contains("Exiting"));
    }

    @Test
    void testMainMenuLogin() throws Exception {
        menuService menu = createMenu("2\n3\n");
        doNothing().when(menu).handleLogin();

        menu.showMainMenu();

        verify(menu).handleLogin();
    }

    @Test
    void testMainMenuExit() {
        menuService menu = createMenu("3\n");
        menu.showMainMenu();

        verify(menu, never()).handleLogin();
        verify(menu, never()).handleSignUp();
        assertTrue(outContent.toString().contains("Exiting"));
    }

    @Test
    void testMainMenuInvalidOption() {
        menuService menu = createMenu("invalid\n3\n");
        menu.showMainMenu();

        assertTrue(outContent.toString().contains("Invalid option"));
    }

    // ===================== handleSignUp =====================
    @Test
    void testHandleSignUpSuccess() {
        menuService menu = createMenu("email@test.com\npassword\npassword\n");
        doReturn("password").when(menu).readPasswordHidden(anyString());
        when(mockUserService.registerUser(anyString(), anyString(), anyString())).thenReturn(true);

        menu.handleSignUp();

        assertTrue(outContent.toString().contains("User registered successfully"));
    }

    @Test
    void testHandleSignUpException() {
        menuService menu = createMenu("email@test.com\npassword\npassword\n");
        doReturn("password").when(menu).readPasswordHidden(anyString());
        when(mockUserService.registerUser(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid email"));

        menu.handleSignUp();

        assertTrue(outContent.toString().contains("Invalid email"));
    }

    // ===================== handleLogin =====================
    @Test
    void testHandleLoginSuccess() {
        user mockUser = mock(user.class);
        when(mockUser.getRole()).thenReturn(Role.STUDENT);

        menuService menu = createMenu("email@test.com\npassword\n");
        doReturn("password").when(menu).readPasswordHidden(anyString());
        when(mockUserService.authenticate(anyString(), anyString())).thenReturn(mockUser);
        doNothing().when(menu).showRoleBasedMenu(mockUser);

        menu.handleLogin();

        verify(menu).showRoleBasedMenu(mockUser);
        assertTrue(outContent.toString().contains("Login successful"));
    }

    @Test
    void testHandleLoginException() {
        menuService menu = createMenu("email@test.com\npassword\n");
        doReturn("password").when(menu).readPasswordHidden(anyString());
        when(mockUserService.authenticate(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Wrong password"));

        menu.handleLogin();

        assertTrue(outContent.toString().contains("Wrong password"));
    }

    // ===================== showAdminMenu =====================
    @Test
    void testShowAdminMenuValidChoices() {
        menuService menu = createMenu("6\n");
        // Only test logout branch
        boolean result = menu.showAdminMenu();
        assertFalse(result);
    }

    @Test
    void testShowAdminMenuInvalidChoice() {
        menuService menu = createMenu("0\n6\n");
        menu.showAdminMenu();
        assertTrue(outContent.toString().contains("Invalid choice"));
    }

    // ===================== showStudentMenu =====================
    @Test
    void testStudentMenuBorrowWithNoFineSuccess() throws Exception {
        user mockUser = mock(user.class);
        when(mockUser.getEmail()).thenReturn("student@test.com");

        menuService menu = createMenu("3\n123\n6\n");
        when(mockBorrowService.hasUnpaidFine(anyString())).thenReturn(false);
        when(mockBorrowService.borrowItem(anyString(), anyInt())).thenReturn(true);

        menu.showStudentMenu(mockUser);

        assertTrue(outContent.toString().contains("Borrow successful"));
    }

    @Test
    void testStudentMenuBorrowWithFine() {
        user mockUser = mock(user.class);
        when(mockUser.getEmail()).thenReturn("student@test.com");

        menuService menu = createMenu("3\n");
        when(mockBorrowService.hasUnpaidFine(anyString())).thenReturn(true);

        menu.showStudentMenu(mockUser);

        assertTrue(outContent.toString().contains("You have unpaid fines"));
    }

    @Test
    void testStudentMenuBorrowWrongFormatISBN() {
        user mockUser = mock(user.class);
        when(mockUser.getEmail()).thenReturn("student@test.com");

        menuService menu = createMenu("3\nnotanumber\n");
        when(mockBorrowService.hasUnpaidFine(anyString())).thenReturn(false);

        menu.showStudentMenu(mockUser);

        assertTrue(outContent.toString().contains("ISBN must be a number"));
    }

    // ===================== handleAddItem =====================
    @Test
    void testHandleAddItemNewItemSuccess() {
        menuService menu = createMenu("1\n2\nBook Name\nAuthor\n5\n");
        when(mockItemsService.addNewItem(anyString(), anyString(), anyInt(), any())).thenReturn(true);

        menu.handleAddItem();

        assertTrue(outContent.toString().contains("Item added successfully"));
    }

    @Test
    void testHandleAddItemIncreaseQuantityMismatchType() {
        menuService menu = createMenu("1\n1\n123\n");
        Items item = mock(Items.class);
        when(mockItemsService.searchByISBN("123")).thenReturn(item);
        when(item.getType()).thenReturn(libraryType.CD); // mismatch

        menu.handleAddItem();

        assertTrue(outContent.toString().contains("belongs to a CD not a Book"));
    }

    // ===================== handleSearchBook =====================
    @Test
    void testSearchBookByNameNoResults() {
        menuService menu = createMenu("1\nsearch\n");
        when(mockItemsService.searchBooksByName(anyString())).thenReturn(List.of());

        menu.handleSearchBook();

        assertTrue(outContent.toString().contains("No items found"));
    }

    @Test
    void testSearchBookByISBNWrongType() {
        menuService menu = createMenu("3\n123\n");
        Items item = mock(Items.class);
        when(item.getType()).thenReturn(libraryType.CD);
        when(mockItemsService.searchByISBN("123")).thenReturn(item);

        menu.handleSearchBook();

        assertTrue(outContent.toString().contains("belongs to a CD, not a Book"));
    }

    // ===================== handleSearchCD =====================
    @Test
    void testSearchCDByISBNWrongType() {
        menuService menu = createMenu("3\n123\n");
        Items item = mock(Items.class);
        when(item.getType()).thenReturn(libraryType.Book);
        when(mockItemsService.searchByISBN("123")).thenReturn(item);

        menu.handleSearchCD();

        assertTrue(outContent.toString().contains("belongs to a Book, not a CD"));
    }
}
