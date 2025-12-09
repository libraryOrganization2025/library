package service;

import domain.*;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

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

    // ===================== showMainMenu (existing) =====================
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

    // ===================== showRoleBasedMenu =====================
    @Test
    void testShowRoleBasedMenuAdmin() {
        user admin = mock(user.class);
        when(admin.getRole()).thenReturn(Role.ADMIN);

        menuService menu = createMenu("");
        // Make showAdminMenu return false to exit loop immediately
        doReturn(false).when(menu).showAdminMenu();

        menu.showRoleBasedMenu(admin);

        verify(menu).showAdminMenu();
    }

    @Test
    void testShowRoleBasedMenuLibrarian() {
        user lib = mock(user.class);
        when(lib.getRole()).thenReturn(Role.LIBRARIAN);

        menuService menu = createMenu("");
        doReturn(false).when(menu).showLibrarianMenu();

        menu.showRoleBasedMenu(lib);

        verify(menu).showLibrarianMenu();
    }

    @Test
    void testShowRoleBasedMenuUnknownRole() {
        user u = mock(user.class);
        when(u.getRole()).thenReturn(null);

        menuService menu = createMenu("");

        assertThrows(NullPointerException.class, () -> {
            menu.showRoleBasedMenu(u);
        });
    }


    // ===================== showAdminMenu =====================
    @Test
    void testShowAdminMenuLogout() {
        menuService menu = createMenu("6\n");
        boolean result = menu.showAdminMenu();
        assertFalse(result);
        assertTrue(outContent.toString().contains("Logging out"));
    }

    @Test
    void testShowAdminMenuInvalidChoice() {
        menuService menu = createMenu("0\n6\n");
        menu.showAdminMenu();
        assertTrue(outContent.toString().contains("Invalid choice"));
    }

    @Test
    void testShowAdminMenuShowInactiveEmpty() {
        when(mockUserService.getInactiveUsers()).thenReturn(List.of());
        menuService menu = createMenu("1\n6\n");
        // call method once (expect it to print "No inactive users found.")
        menu.showAdminMenu();
        assertTrue(outContent.toString().contains("No inactive users found"));
    }

    @Test
    void testShowAdminMenuShowInactiveNonEmpty() {
        user u = mock(user.class);
        when(u.getEmail()).thenReturn("a@b.com");
        when(u.getRole()).thenReturn(Role.STUDENT);
        when(mockUserService.getInactiveUsers()).thenReturn(List.of(u));

        menuService menu = createMenu("1\n6\n");
        menu.showAdminMenu();

        assertTrue(outContent.toString().contains("a@b.com"));
        assertTrue(outContent.toString().contains("STUDENT"));
    }

    @Test
    void testDeleteInactiveAccountSuccessAndFail() {
        // success
        menuService menu1 = createMenu("someone@x.com\n");
        when(mockUserService.removeInactiveUser("someone@x.com")).thenReturn(true);
        menu1.deleteInactiveAccount();
        assertTrue(outContent.toString().contains("Account deleted successfully"));

        // clear output
        outContent.reset();

        // fail
        menuService menu2 = createMenu("noone@x.com\n");
        when(mockUserService.removeInactiveUser("noone@x.com")).thenReturn(false);
        menu2.deleteInactiveAccount();
        assertTrue(outContent.toString().contains("Account not found"));
    }

    @Test
    void testChangeUserRoleInvalidChoiceAndValidUpdate() {
        // invalid choice
        menuService menuInvalid = createMenu("user@x.com\n9\n");
        menuInvalid.changeUserRole();
        assertTrue(outContent.toString().contains("Invalid role choice"));

        outContent.reset();

        // valid but update fails
        menuService menuFail = createMenu("user@x.com\n1\n");
        when(mockUserService.updateUserRole("user@x.com", Role.ADMIN)).thenReturn(false);
        menuFail.changeUserRole();
        assertTrue(outContent.toString().contains("User not found"));

        outContent.reset();

        // valid and update succeeds
        menuService menuOk = createMenu("user@x.com\n2\n");
        when(mockUserService.updateUserRole("user@x.com", Role.LIBRARIAN)).thenReturn(true);
        menuOk.changeUserRole();
        assertTrue(outContent.toString().contains("Role updated successfully"));
    }

    @Test
    void testSendFineRemindersEmptyAndNonEmpty() {
        // empty
        when(mockBorrowService.getStudentsWithUnpaidFines()).thenReturn(List.of());
        menuService menu = createMenu("");
        menu.sendFineReminders();
        assertTrue(outContent.toString().contains("No users with unpaid fines"));

        outContent.reset();

        // non-empty
        when(mockBorrowService.getStudentsWithUnpaidFines()).thenReturn(List.of("a@x.com", "b@x.com"));
        menu.sendFineReminders();
        verify(mockEmailService, times(2)).sendEmail(anyString(), anyString(), anyString());
        assertTrue(outContent.toString().contains("Fine reminder emails sent to 2 students"));
    }

    // ===================== showLibrarianMenu =====================
    @Test
    void testShowLibrarianMenuEmptyOverdue() {
        when(mockBorrowService.getOverdueStudents()).thenReturn(List.of());
        menuService menu = createMenu("1\n2\n");
        menu.showLibrarianMenu();
        assertTrue(outContent.toString().contains("Overdue Users"));
    }

    @Test
    void testShowLibrarianMenuNonEmptyOverdue() {
        Borrow b = new Borrow() {
            @Override
            public String toString() {
                return "student1 111 2025-01-01 2025-01-11 10";
            }
        };
        when(mockBorrowService.getOverdueStudents()).thenReturn(List.of(b));
        menuService menu = createMenu("1\n2\n");
        menu.showLibrarianMenu();
        assertTrue(outContent.toString().contains("student1"));
    }

    // ===================== showStudentMenu (additional cases) =====================
    @Test
    void testStudentMenuReturnWithFine() {
        user mockUser = mock(user.class);
        when(mockUser.getEmail()).thenReturn("student@test.com");

        menuService menu = createMenu("4\n");
        when(mockBorrowService.hasUnpaidFine(anyString())).thenReturn(true);

        menu.showStudentMenu(mockUser);

        assertTrue(outContent.toString().contains("You have unpaid fines"));
    }

    @Test
    void testStudentMenuReturnWrongISBNFormat() {
        user mockUser = mock(user.class);
        when(mockUser.getEmail()).thenReturn("student@test.com");

        menuService menu = createMenu("4\nnotanumber\n");
        when(mockBorrowService.hasUnpaidFine(anyString())).thenReturn(false);

        menu.showStudentMenu(mockUser);

        assertTrue(outContent.toString().contains("ISBN must be a number"));
    }

    @Test
    void testStudentMenuPayFineNoFineAndInvalidAmountAndPartialPayment() {
        user mockUser = mock(user.class);
        when(mockUser.getEmail()).thenReturn("student@test.com");

        // no fine case
        menuService menuNoFine = createMenu("5\n");
        when(mockBorrowService.getTotalFine("student@test.com")).thenReturn(0);
        menuNoFine.showStudentMenu(mockUser);
        assertTrue(outContent.toString().contains("You have no fines"));

        outContent.reset();

        // invalid amount (non-number)
        menuService menuInvalid = createMenu("5\nnotanumber\n");
        when(mockBorrowService.getTotalFine("student@test.com")).thenReturn(10);
        menuInvalid.showStudentMenu(mockUser);
        assertTrue(outContent.toString().contains("Amount must be a number"));

        outContent.reset();

        // invalid amount (<=0)
        menuService menuZero = createMenu("5\n0\n");
        when(mockBorrowService.getTotalFine("student@test.com")).thenReturn(10);
        menuZero.showStudentMenu(mockUser);
        assertTrue(outContent.toString().contains("Invalid amount"));

        outContent.reset();

        // partial payment (remaining > 0)
        menuService menuPartial = createMenu("5\n5\n");
        when(mockBorrowService.getTotalFine("student@test.com")).thenReturn(10, 5); // before and after
        doNothing().when(mockBorrowService).payFine("student@test.com", 5);
        menuPartial.showStudentMenu(mockUser);
        assertTrue(outContent.toString().contains("Payment successful"));
        assertTrue(outContent.toString().contains("You still cannot borrow/return until full fine is paid"));
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

    @Test
    void testHandleAddItemIncreaseQuantitySuccessAndFail() {
        // success when type matches
        Items item = mock(Items.class);
        when(item.getType()).thenReturn(libraryType.Book);
        when(mockItemsService.searchByISBN("456")).thenReturn(item);
        when(mockItemsService.increaseQuantityByISBN("456")).thenReturn(true);

        menuService menuOk = createMenu("1\n1\n456\n");
        menuOk.handleAddItem();
        assertTrue(outContent.toString().contains("Quantity increased by 1"));

        outContent.reset();

        // fail to increase
        when(mockItemsService.increaseQuantityByISBN("456")).thenReturn(false);
        menuService menuFail = createMenu("1\n1\n456\n");
        menuFail.handleAddItem();
        assertTrue(outContent.toString().contains("Failed to increase quantity"));
    }

    @Test
    void testHandleAddItemQuantityNumberFormatException() {
        menuService menu = createMenu("1\n2\nName\nAuthor\nnotanumber\n");
        menu.handleAddItem();
        assertTrue(outContent.toString().contains("Quantity must be a number"));
    }

    @Test
    void testHandleAddItemAddNewItemFailure() {
        menuService menu = createMenu("1\n2\nName\nAuthor\n3\n");
        when(mockItemsService.addNewItem(anyString(), anyString(), anyInt(), any())).thenReturn(false);
        menu.handleAddItem();
        assertTrue(outContent.toString().contains("Failed to add item"));
    }

    // ===================== handleSearchBook / handleSearchCD =====================
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

    @Test
    void testSearchBookServiceThrows() {
        menuService menu = createMenu("1\nsearch\n");
        when(mockItemsService.searchBooksByName(anyString())).thenThrow(new IllegalArgumentException("bad query"));

        menu.handleSearchBook();
        assertTrue(outContent.toString().contains("bad query"));
    }

    @Test
    void testSearchCDByISBNWrongType() {
        menuService menu = createMenu("3\n123\n");
        Items item = mock(Items.class);
        when(item.getType()).thenReturn(libraryType.Book);
        when(mockItemsService.searchByISBN("123")).thenReturn(item);

        menu.handleSearchCD();

        assertTrue(outContent.toString().contains("belongs to a Book, not a CD"));
    }

    @Test
    void testSearchCDServiceThrows() {
        menuService menu = createMenu("1\nsearch\n");
        when(mockItemsService.searchCDsByName(anyString())).thenThrow(new IllegalArgumentException("bad cd query"));

        menu.handleSearchCD();
        assertTrue(outContent.toString().contains("bad cd query"));
    }

    @Test
    void testPrintItemsSkipsWrongTypeAndPrintsRightType() {
        Items book = mock(Items.class);
        when(book.getType()).thenReturn(libraryType.Book);
        when(book.getISBN()).thenReturn("111");
        when(book.getName()).thenReturn("BookName");
        when(book.getAuthor()).thenReturn("Auth");
        when(book.getQuantity()).thenReturn(2);

        Items cd = mock(Items.class);
        when(cd.getType()).thenReturn(libraryType.CD);
        when(cd.getISBN()).thenReturn("222");

        when(mockItemsService.searchBooksByName(anyString())).thenReturn(List.of(book, cd));

        menuService menu = createMenu("1\nname\n");
        menu.handleSearchBook();

        String out = outContent.toString();
        assertTrue(out.contains("ISBN:     111"));
        assertFalse(out.contains("ISBN:     222")); // CD should be skipped
    }
}
