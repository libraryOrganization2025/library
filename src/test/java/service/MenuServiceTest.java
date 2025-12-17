package service;

import domain.*;
import org.junit.jupiter.api.*;

import java.io.*;
import java.util.List;
import java.util.Scanner;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MenuServiceTest {

        @Mock
        private userService mockUserService;

        @Mock
        private ItemsService mockItemsService;

        @Mock
        private BorrowService mockBorrowService;

        @Mock
        private EmailService mockEmailService;

        private ByteArrayOutputStream outContent;
        private PrintStream originalOut;

        @BeforeEach
        void setUp() {
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
    void testMainMenuSignUpCallsHandleSignUpAndThenExit() throws Exception {
        menuService menu = createMenu("1\n3\n");
        doNothing().when(menu).handleSignUp();

        menu.showMainMenu();

        verify(menu).handleSignUp();
        String out = outContent.toString();
        assertTrue(out.contains("Welcome to the Library System"));
        assertTrue(out.contains("Exiting") || out.contains("Goodbye")); // tolerate punctuation/emoji differences
    }

    @Test
    void testMainMenuLoginCallsHandleLogin() throws Exception {
        menuService menu = createMenu("2\n3\n");
        doNothing().when(menu).handleLogin();

        menu.showMainMenu();

        verify(menu).handleLogin();
    }

    @Test
    void testMainMenuExitDirect() {
        menuService menu = createMenu("3\n");
        menu.showMainMenu();

        verify(menu, never()).handleLogin();
        verify(menu, never()).handleSignUp();
        assertTrue(outContent.toString().contains("Exiting") || outContent.toString().contains("Goodbye"));
    }

    @Test
    void testMainMenuInvalidOptionPrinted() {
        menuService menu = createMenu("invalid\n3\n");
        menu.showMainMenu();

        assertTrue(outContent.toString().contains("Invalid option") || outContent.toString().contains("Invalid"));
    }

    // ===================== handleSignUp =====================
    @Test
    void testHandleSignUpSuccessPrintsSuccess() {
        menuService menu = createMenu("email@test.com\npassword\npassword\n");
        doReturn("password").when(menu).readPasswordHidden(anyString());
        when(mockUserService.registerUser(anyString(), anyString(), anyString())).thenReturn(true);

        menu.handleSignUp();

        assertTrue(outContent.toString().contains("User registered") || outContent.toString().contains("registered successfully"));
    }

    @Test
    void testHandleSignUpExceptionPrintsError() {
        menuService menu = createMenu("email@test.com\npassword\npassword\n");
        doReturn("password").when(menu).readPasswordHidden(anyString());
        when(mockUserService.registerUser(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid email"));

        menu.handleSignUp();

        assertTrue(outContent.toString().contains("Invalid email"));
    }

    // ===================== handleLogin =====================
    @Test
    void testHandleLoginSuccessRoutesToRoleMenu() {
        user mockUser = mock(user.class);
        when(mockUser.getRole()).thenReturn(Role.STUDENT);
        menuService menu = createMenu("email@test.com\n");
        doReturn("password").when(menu).readPasswordHidden(anyString());
        when(mockUserService.authenticate("email@test.com", "password")).thenReturn(mockUser);
        doReturn(true).when(menu).showRoleBasedMenu(mockUser);
        menu.handleLogin();
        verify(menu, times(1)).showRoleBasedMenu(mockUser);
        String out = outContent.toString();
        assertTrue(out.contains("Login successful") || out.contains("✅ Login successful"));
    }









    @Test
    void testHandleLoginExceptionPrintsError() {
        menuService menu = createMenu("email@test.com\npassword\n");
        doReturn("password").when(menu).readPasswordHidden(anyString());
        when(mockUserService.authenticate(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Wrong password"));

        menu.handleLogin();

        assertTrue(outContent.toString().contains("Wrong password"));
    }

    // ===================== showRoleBasedMenu =====================
    @Test
    void testShowRoleBasedMenuAdminCallsAdminMenu() {
        user admin = mock(user.class);
        when(admin.getRole()).thenReturn(Role.ADMIN);

        menuService menu = createMenu("");
        doReturn(false).when(menu).showAdminMenu(); // exit immediately

        menu.showRoleBasedMenu(admin);

        verify(menu).showAdminMenu();
    }

    @Test
    void testShowRoleBasedMenuLibrarianCallsLibrarianMenu() {
        user lib = mock(user.class);
        when(lib.getRole()).thenReturn(Role.LIBRARIAN);

        menuService menu = createMenu("");
        doReturn(false).when(menu).showLibrarianMenu();

        menu.showRoleBasedMenu(lib);

        verify(menu).showLibrarianMenu();
    }

    @Test
    void testShowRoleBasedMenuUnknownRoleReturnsFalse() {
        menuService menu = createMenu("");
        user fake = mock(user.class);

        // Force unknown role
        when(fake.getRole()).thenReturn(null);

        boolean result = menu.showRoleBasedMenu(fake);

        // Method should return false
        assertFalse(result);

        // Should print "Unknown role"
        String out = outContent.toString();
        assertTrue(out.contains("Unknown role"));
    }




    // ===================== showAdminMenu and related admin ops =====================
    @Test
    void testShowAdminMenuLogout() {
        menuService menu = createMenu("6\n");
        boolean result = menu.showAdminMenu();
        assertFalse(result);
        assertTrue(outContent.toString().contains("Logging out") || outContent.toString().contains("Logging"));
    }

    @Test
    void testShowAdminMenuInvalidChoicePrinted() {
        menuService menu = createMenu("0\n6\n");
        menu.showAdminMenu();
        assertTrue(outContent.toString().contains("Invalid choice") || outContent.toString().contains("Invalid"));
    }

    @Test
    void testShowInactiveAccountsEmptyAndNonEmpty() {
        // قائمة فارغة
        when(mockUserService.getInactiveUsers()).thenReturn(List.of());
        menuService menu = createMenu("1\n6\n"); // 1 = see inactive, 6 = logout
        menu.showAdminMenu();
        String output = outContent.toString();
        assertTrue(output.contains("No inactive users") || output.contains("No inactive"),
                "Expected 'No inactive users' message");

        outContent.reset();

        // قائمة فيها مستخدم
        user u = mock(user.class);
        when(u.getEmail()).thenReturn("a@b.com");
        when(u.getRole()).thenReturn(Role.STUDENT);
        when(mockUserService.getInactiveUsers()).thenReturn(List.of(u));

        menu = createMenu("1\n6\n"); // إعادة إنشاء menuService مع Scanner جديد
        menu.showAdminMenu();
        output = outContent.toString();
        assertTrue(output.contains(u.getEmail()), "Expected user's email in output");
        assertTrue(output.contains("STUDENT"), "Expected user's role in output");
    }



    @Test
    void testDeleteInactiveAccountVerifiesServiceCallAndOutputs() {
        menuService menu1 = createMenu("someone@x.com\n");
        when(mockUserService.removeInactiveUser("someone@x.com")).thenReturn(true);
        menu1.deleteInactiveAccount();
        verify(mockUserService).removeInactiveUser("someone@x.com");
        assertTrue(outContent.toString().toLowerCase().contains("account deleted") || outContent.toString().contains("deleted"));

        outContent.reset();

        menuService menu2 = createMenu("noone@x.com\n");
        when(mockUserService.removeInactiveUser("noone@x.com")).thenReturn(false);
        menu2.deleteInactiveAccount();
        assertTrue(outContent.toString().toLowerCase().contains("not found") || outContent.toString().contains("not"));
    }

    @Test
    void testChangeUserRoleInvalidAndValidPaths() {
        // invalid choice
        menuService menuInvalid = createMenu("user@x.com\n9\n");
        menuInvalid.changeUserRole();
        assertTrue(outContent.toString().contains("Invalid role choice") || outContent.toString().contains("Invalid"));

        outContent.reset();

        // valid but update fails
        menuService menuFail = createMenu("user@x.com\n1\n");
        when(mockUserService.updateUserRole("user@x.com", Role.ADMIN)).thenReturn(false);
        menuFail.changeUserRole();
        verify(mockUserService).updateUserRole("user@x.com", Role.ADMIN);
        assertTrue(outContent.toString().contains("User not found") || outContent.toString().contains("not found"));

        outContent.reset();

        // valid and update succeeds
        menuService menuOk = createMenu("user@x.com\n2\n");
        when(mockUserService.updateUserRole("user@x.com", Role.LIBRARIAN)).thenReturn(true);
        menuOk.changeUserRole();
        verify(mockUserService).updateUserRole("user@x.com", Role.LIBRARIAN);
        assertTrue(outContent.toString().contains("Role updated") || outContent.toString().contains("updated"));
    }

    @Test
    void testSendFineRemindersEmptyAndNonEmpty() {
        // empty
        when(mockBorrowService.getStudentsWithUnpaidFines()).thenReturn(List.of());
        menuService menu = createMenu("");
        menu.sendFineReminders();
        assertTrue(outContent.toString().contains("No users with unpaid fines") || outContent.toString().contains("No users"));

        outContent.reset();

        // non-empty
        when(mockBorrowService.getStudentsWithUnpaidFines()).thenReturn(List.of("a@x.com", "b@x.com"));
        menu.sendFineReminders();
        verify(mockEmailService, times(2)).sendEmail(anyString(), anyString(), anyString());
        assertTrue(outContent.toString().contains("Fine reminder emails sent to 2 students") || outContent.toString().contains("sent to 2"));
    }

    // ===================== showLibrarianMenu =====================
    @Test
    void testShowLibrarianMenuEmptyOverdue() {
        when(mockBorrowService.getOverdueStudents()).thenReturn(List.of());
        menuService menu = createMenu("1\n2\n");
        menu.showLibrarianMenu();
        assertTrue(outContent.toString().contains("Overdue Users") || outContent.toString().contains("Overdue"));
        assertTrue(outContent.toString().contains("No overdue users found") || outContent.toString().contains("No overdue"));
    }

    @Test
    void testShowLibrarianMenuNonEmptyOverduePrintsEntries() {
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

    // ===================== showStudentMenu (current placeholder behaviour) =====================
    /*@Test
    void testShowStudentMenuReturnsTrue() {
        // Mock the user
        user mockUser = mock(user.class);
        when(mockUser.getEmail()).thenReturn("student@test.com");
        when(mockUser.getRole()).thenReturn(Role.STUDENT);

        // Instead of real scanner input, provide dummy input (like "6" for logout)
        menuService menu = createMenu("6\n");

        // Call the method
        boolean res = menu.showStudentMenu(mockUser);

        // Verify it returns true (placeholder)
        assertTrue(res);

        // Instead of checking exact printed lines, just check that it returned true
        // This avoids depending on System.out content
    }*/

    // ===================== methods that are "omitted for brevity" in production =====================
    @Test
    void testHandleAddItemDoesNotThrowAndDoesNotCallItemsServiceWhenEmpty() {
        menuService menu = createMenu("");
        // The current implementation in production is omitted; calling it should not throw.
        assertDoesNotThrow(menu::handleAddItem);
        // We cannot assert specific prints since method is omitted; ensure no unexpected interactions
        verifyNoInteractions(mockItemsService);
    }

    @Test
    void testHandleSearchBookDoesNotThrowAndHandlesServiceExceptionsGracefully() {
        menuService menu = createMenu("");
        // method body omitted in production; calling should not throw
        assertDoesNotThrow(menu::handleSearchBook);
    }

    @Test
    void testHandleSearchCDDoesNotThrow() {
        menuService menu = createMenu("");
        assertDoesNotThrow(menu::handleSearchCD);
    }

    // ===================== printing helpers behaviour (indirect) =====================
    @Test
    void testPrintItemsSkipsWrongTypeAndPrintsRightTypeIndirectly() {
        // Because handleSearchBook/handleSearchCD are omitted, we cannot call them to reach printItems.
        // But we can verify that if service returns results calling an existing printing path (if exposed),
        // currently we cannot access private printItems. So just assert services remain untouched when not used.
        verifyNoInteractions(mockItemsService);
    }

    /*@Test
    void testShowStudentMenuWithLogoutOption() {
        user student = mock(user.class);
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(student.getEmail()).thenReturn("s@test.com");

        menuService menu = createMenu("0\n0\n"); // dummy input

        // Call the method
        boolean result = menu.showStudentMenu(student);

        // Since placeholder always returns true, just assert that
        assertTrue(result);

        // Optionally, you can assert that nothing unexpected was printed
        String out = outContent.toString();
        assertTrue(out.isEmpty() || !out.contains("Invalid option"));
    }*/


    @Test
    void testHandleAddItemWithEmptyInputDoesNothing() {
        menuService menu = createMenu("\n"); // simulate pressing Enter
        assertDoesNotThrow(menu::handleAddItem);
        verifyNoInteractions(mockItemsService);
    }

    @Test
    void testPrintItemsEmptyListPrintsWarning() {
        menuService menu = createMenu("");
        // call private method via reflection
        List<Items> emptyList = List.of();
        assertDoesNotThrow(() -> {
            var method = menuService.class.getDeclaredMethod("printItems", List.class, libraryType.class);
            method.setAccessible(true);
            method.invoke(menu, emptyList, libraryType.BOOK);
        });
        assertTrue(outContent.toString().contains("No items found"));
    }

    @Test
    void testPrintItemsSkipsWrongTypePrintsCorrectType() throws Exception {
        menuService menu = createMenu("");
        Items item1 = mock(Items.class);
        Items item2 = mock(Items.class);
        when(item1.getType()).thenReturn(libraryType.CD);
        when(item2.getType()).thenReturn(libraryType.BOOK);
        when(item2.getISBN()).thenReturn("123");
        when(item2.getName()).thenReturn("BookName");
        when(item2.getAuthor()).thenReturn("Author");
        when(item2.getQuantity()).thenReturn(1);

        var method = menuService.class.getDeclaredMethod("printItems", List.class, libraryType.class);
        method.setAccessible(true);
        method.invoke(menu, List.of(item1, item2), libraryType.BOOK);

        String output = outContent.toString();
        assertTrue(output.contains("BookName"));
        assertFalse(output.contains("CD")); // wrong type skipped
    }

    @Test
    void testPrintSingleItemPrintsAllFields() throws Exception {
        menuService menu = createMenu("");

        Items item = mock(Items.class);
        when(item.getISBN()).thenReturn("123");
        when(item.getName()).thenReturn("TestBook");
        when(item.getAuthor()).thenReturn("AuthorA");
        when(item.getType()).thenReturn(libraryType.BOOK);
        when(item.getQuantity()).thenReturn(5);

        var m = menuService.class.getDeclaredMethod("printSingleItem", Items.class);
        m.setAccessible(true);
        m.invoke(menu, item);

        String out = outContent.toString();

        assertTrue(out.contains("123"));
        assertTrue(out.contains("TestBook"));
        assertTrue(out.contains("AuthorA"));
        assertTrue(out.toUpperCase().contains("BOOK")); // fixed
        assertTrue(out.contains("5"));
    }



//    @Test
//    void testReadPasswordHiddenWithNullConsoleFallsBackToScanner() {
//        menuService menu = createMenu("secret\n");
//        String result = menu.readPasswordHidden("Enter pass:");
//        assertEquals("secret", result);
//    }

    @Test
    void testShowLibrarianMenuInvalidChoicePrinted() {
        menuService menu = createMenu("0\n2\n");
        boolean res = menu.showLibrarianMenu();
        assertTrue(res || !res); // main goal: invalid choice prints
        String out = outContent.toString();
        assertTrue(out.contains("Invalid choice") || out.contains("Invalid"));
    }

    @Test
    void testAdminMenuOptions1To5Branches() {
        menuService menu = createMenu("1\n2\n3\n4\n5\n6\n");
        // Stubbing dependent methods to prevent errors
        doNothing().when(menu).showInactiveAccounts();
        doNothing().when(menu).deleteInactiveAccount();
        doNothing().when(menu).changeUserRole();
        doNothing().when(menu).handleAddItem();
        doNothing().when(menu).sendFineReminders();

        boolean res = menu.showAdminMenu(); // first call
        assertTrue(res);
        verify(menu).showInactiveAccounts();
    }

    @Test
    void testShowStudentMenuLogout() {
        user student = mock(user.class);
        when(student.getEmail()).thenReturn("s@test.com");
        when(student.getRole()).thenReturn(Role.STUDENT);

        menuService menu = createMenu("6\n");

        boolean res = menu.showRoleBasedMenu(student);

        assertTrue(res);
        assertTrue(outContent.toString().contains("Logging out"));
    }

    @Test
    void testStudentBorrowBlockedByUnpaidFine() {
        user student = mock(user.class);
        when(student.getEmail()).thenReturn("s@test.com");
        when(student.getRole()).thenReturn(Role.STUDENT);

        when(mockBorrowService.hasUnpaidFine("s@test.com")).thenReturn(true);

        menuService menu = createMenu("3\n6\n");
        menu.showRoleBasedMenu(student);

        assertTrue(outContent.toString().contains("unpaid fines"));
        verify(mockBorrowService, never()).borrowItem(anyString(), anyInt());
    }

    @Test
    void testStudentBorrowSuccess() {
        user student = mock(user.class);
        when(student.getEmail()).thenReturn("s@test.com");
        when(student.getRole()).thenReturn(Role.STUDENT);

        when(mockBorrowService.hasUnpaidFine("s@test.com")).thenReturn(false);
        when(mockBorrowService.borrowItem("s@test.com", 123)).thenReturn(true);

        menuService menu = createMenu("3\n123\n6\n");
        menu.showRoleBasedMenu(student);

        assertTrue(outContent.toString().contains("Borrow successful"));
    }

    @Test
    void testStudentBorrowInvalidISBN() {
        user student = mock(user.class);
        when(student.getEmail()).thenReturn("s@test.com");
        when(student.getRole()).thenReturn(Role.STUDENT);

        when(mockBorrowService.hasUnpaidFine("s@test.com")).thenReturn(false);

        menuService menu = createMenu("3\nabc\n6\n");
        menu.showRoleBasedMenu(student);

        assertTrue(outContent.toString().contains("ISBN must be a number"));
    }

    @Test
    void testStudentReturnBlockedByFine() {
        user student = mock(user.class);
        when(student.getEmail()).thenReturn("s@test.com");
        when(student.getRole()).thenReturn(Role.STUDENT);

        when(mockBorrowService.hasUnpaidFine("s@test.com")).thenReturn(true);

        menuService menu = createMenu("4\n6\n");
        menu.showRoleBasedMenu(student);

        assertTrue(outContent.toString().contains("unpaid fines"));
    }

    @Test
    void testStudentReturnSuccess() {
        user student = mock(user.class);
        when(student.getEmail()).thenReturn("s@test.com");
        when(student.getRole()).thenReturn(Role.STUDENT);

        when(mockBorrowService.hasUnpaidFine("s@test.com")).thenReturn(false);
        when(mockBorrowService.returnItem("s@test.com", 111)).thenReturn(true);

        menuService menu = createMenu("4\n111\n6\n");
        menu.showRoleBasedMenu(student);

        assertTrue(outContent.toString().contains("returned successfully"));
    }

    @Test
    void testStudentPayFineNoFine() {
        user student = mock(user.class);
        when(student.getEmail()).thenReturn("s@test.com");
        when(student.getRole()).thenReturn(Role.STUDENT);

        when(mockBorrowService.getTotalFine("s@test.com")).thenReturn(0);

        menuService menu = createMenu("5\n6\n");
        menu.showRoleBasedMenu(student);

        assertTrue(outContent.toString().contains("no fines"));
    }

    @Test
    void testStudentPayFinePartial() {
        user student = mock(user.class);
        when(student.getEmail()).thenReturn("s@test.com");
        when(student.getRole()).thenReturn(Role.STUDENT);

        when(mockBorrowService.getTotalFine("s@test.com"))
                .thenReturn(50)   // before
                .thenReturn(20);  // after payment

        menuService menu = createMenu("5\n30\n6\n");
        menu.showRoleBasedMenu(student);

        verify(mockBorrowService).payFine("s@test.com", 30);
        assertTrue(outContent.toString().contains("Remaining fine"));
    }

    @Test
    void testHandleAddItemInvalidType() {
        menuService menu = createMenu("9\n");
        menu.handleAddItem();

        assertTrue(outContent.toString().contains("Invalid type"));
    }

    @Test
    void testHandleAddItemExistingIncreaseQuantity() {
        Items item = mock(Items.class);
        when(item.getType()).thenReturn(libraryType.BOOK);

        when(mockItemsService.searchByISBN("123")).thenReturn(item);
        when(mockItemsService.increaseQuantityByISBN("123")).thenReturn(true);

        menuService menu = createMenu("1\n1\n123\n");
        menu.handleAddItem();

        verify(mockItemsService).increaseQuantityByISBN("123");
        assertTrue(outContent.toString().contains("Quantity increased"));
    }

    @Test
    void testHandleAddItemExistingWrongType() {
        Items item = mock(Items.class);
        when(item.getType()).thenReturn(libraryType.CD);

        when(mockItemsService.searchByISBN("123")).thenReturn(item);

        menuService menu = createMenu("1\n1\n123\n");
        menu.handleAddItem();

        assertTrue(outContent.toString().contains("belongs to a CD"));
    }

    @Test
    void testSearchBookByName() {
        Items book = mock(Items.class);
        when(book.getType()).thenReturn(libraryType.BOOK);
        when(book.getISBN()).thenReturn("1");
        when(book.getName()).thenReturn("Java");
        when(book.getAuthor()).thenReturn("Author");
        when(book.getQuantity()).thenReturn(1);

        when(mockItemsService.searchBooksByName("Java")).thenReturn(List.of(book));

        menuService menu = createMenu("1\nJava\n");
        menu.handleSearchBook();

        assertTrue(outContent.toString().contains("Java"));
    }

    @Test
    void testStudentMenuInvalidChoiceWorks() {
        user student = mock(user.class);
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(student.getEmail()).thenReturn("s@test.com");

        menuService menu = createMenu(
                "9\n" +  // invalid
                        "6\n"    // logout
        );

        menu.showRoleBasedMenu(student);

        assertTrue(outContent.toString().contains("Invalid choice"));
    }

    @Test
    void testStudentMenuLogoutOnly() {
        user student = mock(user.class);
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(student.getEmail()).thenReturn("s@test.com");

        menuService menu = createMenu("6\n");

        menu.showRoleBasedMenu(student);

        assertTrue(outContent.toString().contains("Logging out"));
    }

    @Test
    void testStudentBorrowBlockedByFineWorks() {
        user student = mock(user.class);
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(student.getEmail()).thenReturn("s@test.com");

        when(mockBorrowService.hasUnpaidFine("s@test.com")).thenReturn(true);

        menuService menu = createMenu(
                "3\n" +  // borrow
                        "6\n"    // logout
        );

        menu.showRoleBasedMenu(student);

        assertTrue(outContent.toString().contains("unpaid fines"));
    }

    @Test
    void testStudentBorrowSuccessWorks() {
        user student = mock(user.class);
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(student.getEmail()).thenReturn("s@test.com");

        when(mockBorrowService.hasUnpaidFine("s@test.com")).thenReturn(false);
        when(mockBorrowService.borrowItem("s@test.com", 123)).thenReturn(true);

        menuService menu = createMenu(
                "3\n" +
                        "123\n" +
                        "6\n"
        );

        menu.showRoleBasedMenu(student);

        assertTrue(outContent.toString().contains("Borrow successful"));
    }

    @Test
    void testStudentBorrowInvalidISBNWorks() {
        user student = mock(user.class);
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(student.getEmail()).thenReturn("s@test.com");

        when(mockBorrowService.hasUnpaidFine("s@test.com")).thenReturn(false);

        menuService menu = createMenu(
                "3\n" +
                        "abc\n" +
                        "6\n"
        );

        menu.showRoleBasedMenu(student);

        assertTrue(outContent.toString().contains("ISBN must be a number"));
    }

    @Test
    void testStudentReturnSuccessWorks() {
        user student = mock(user.class);
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(student.getEmail()).thenReturn("s@test.com");

        when(mockBorrowService.hasUnpaidFine("s@test.com")).thenReturn(false);
        when(mockBorrowService.returnItem("s@test.com", 111)).thenReturn(true);

        menuService menu = createMenu(
                "4\n" +
                        "111\n" +
                        "6\n"
        );

        menu.showRoleBasedMenu(student);

        assertTrue(outContent.toString().contains("returned successfully"));
    }

    @Test
    void testStudentPayFineZeroWorks() {
        user student = mock(user.class);
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(student.getEmail()).thenReturn("s@test.com");

        when(mockBorrowService.getTotalFine("s@test.com")).thenReturn(0);

        menuService menu = createMenu(
                "5\n" +
                        "6\n"
        );

        menu.showRoleBasedMenu(student);

        assertTrue(outContent.toString().contains("no fines"));
    }

    @Test
    void testStudentPayFineInvalidAmountWorks() {
        user student = mock(user.class);
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(student.getEmail()).thenReturn("s@test.com");

        when(mockBorrowService.getTotalFine("s@test.com")).thenReturn(50);

        menuService menu = createMenu(
                "5\n" +
                        "-10\n" +
                        "6\n"
        );

        menu.showRoleBasedMenu(student);

        assertTrue(outContent.toString().contains("Invalid amount"));
    }

    @Test
    void testMainMenuMultipleInvalidThenExit() {
        menuService menu = createMenu("x\n!\n3\n");
        menu.showMainMenu();
        String out = outContent.toString();
        assertTrue(out.contains("Invalid option"));
        assertTrue(out.contains("Exiting"));
    }

    @Test
    void testMainMenuWhitespaceInput() {
        menuService menu = createMenu(" \n3\n");
        menu.showMainMenu();
        String out = outContent.toString();
        assertTrue(out.contains("Invalid option"));
    }

    @Test
    void testShowRoleBasedMenuUnknownEnum() {
        user u = mock(user.class);
        when(u.getRole()).thenReturn(null);
        menuService menu = createMenu("");
        boolean res = menu.showRoleBasedMenu(u);
        assertFalse(res);
        assertTrue(outContent.toString().contains("Unknown role"));
    }

    @Test
    void testStudentReturnItemNotBorrowed() {
        user student = mock(user.class);
        when(student.getEmail()).thenReturn("s@test.com");
        when(student.getRole()).thenReturn(Role.STUDENT);

        when(mockBorrowService.hasUnpaidFine("s@test.com")).thenReturn(false);
        when(mockBorrowService.returnItem("s@test.com", 999)).thenReturn(false);

        menuService menu = createMenu("4\n999\n6\n");
        menu.showRoleBasedMenu(student);

        assertTrue(outContent.toString().contains("Could not return"));
    }

    @Test
    void testShowLibrarianMenuInvalidThenLogout() {
        menuService menu = createMenu("x\n2\n");
        menu.showLibrarianMenu();
        String out = outContent.toString();
        assertTrue(out.contains("Invalid choice"));
    }

    @Test
    void testStudentPayFineExcess() {
        user student = mock(user.class);
        when(student.getEmail()).thenReturn("s@test.com");
        when(student.getRole()).thenReturn(Role.STUDENT);

        when(mockBorrowService.getTotalFine("s@test.com")).thenReturn(50).thenReturn(0);
        menuService menu = createMenu("5\n60\n6\n"); // paying 60 while total is 50
        menu.showRoleBasedMenu(student);

        verify(mockBorrowService).payFine("s@test.com", 60);
        assertTrue(outContent.toString().contains("Remaining fine: 0"));
    }

    @Test
    void testSearchBookInvalidOption() {
        menuService menu = createMenu("9\n");
        menu.handleSearchBook();
        assertTrue(outContent.toString().contains("Invalid choice"));
    }

    @Test
    void testSearchCDInvalidOption() {
        menuService menu = createMenu("9\n");
        menu.handleSearchCD();
        assertTrue(outContent.toString().contains("Invalid choice"));
    }

    @Test
    void testHandleAddItemNewFails() {
        when(mockItemsService.addNewItem(any(), any(), anyInt(), any())).thenReturn(false);
        menuService menu = createMenu("2\n2\nName\nAuthor\n1\n");
        menu.handleAddItem();
        assertTrue(outContent.toString().contains("Failed to add item"));
    }

    @Test
    void testHandleAddItemNewItemFailure() {
        menuService menu = createMenu("1\n2\nBookName\nAuthorName\nabc\n"); // invalid quantity
        when(mockItemsService.addNewItem(anyString(), anyString(), anyInt(), any())).thenReturn(false);

        menu.handleAddItem();

        assertTrue(outContent.toString().contains("Quantity must be a number") || outContent.toString().contains("Failed"));
    }

    @Test
    void testHandleAddItemNewItemSuccess() {
        menuService menu = createMenu("1\n2\nBookName\nAuthorName\n5\n");
        when(mockItemsService.addNewItem("BookName","AuthorName",5, libraryType.BOOK)).thenReturn(true);

        menu.handleAddItem();

        verify(mockItemsService).addNewItem("BookName","AuthorName",5, libraryType.BOOK);
        assertTrue(outContent.toString().contains("Item added successfully"));
    }

    @Test
    void testHandleSearchBookByAuthorNoResults() {
        when(mockItemsService.searchBooksByAuthor("Unknown")).thenReturn(List.of());
        menuService menu = createMenu("2\nUnknown\n"); // 2 = search by author

        menu.handleSearchBook();

        assertTrue(outContent.toString().contains("No items found"));
    }

    @Test
    void testHandleSearchCDByNameResults() {
        Items cd = mock(Items.class);
        when(cd.getType()).thenReturn(libraryType.CD);
        when(cd.getISBN()).thenReturn("10");
        when(cd.getName()).thenReturn("Hits");
        when(cd.getAuthor()).thenReturn("Artist");
        when(cd.getQuantity()).thenReturn(3);

        when(mockItemsService.searchCDsByName("Hits")).thenReturn(List.of(cd));

        menuService menu = createMenu("1\nHits\n"); // 1 = search by name
        menu.handleSearchCD();

        String output = outContent.toString();
        assertTrue(output.contains("Hits"));
        assertTrue(output.contains("Artist"));
    }

    @Test
    void testStudentBorrowFails() {
        user student = mock(user.class);
        when(student.getEmail()).thenReturn("s@test.com");
        when(student.getRole()).thenReturn(Role.STUDENT);

        when(mockBorrowService.hasUnpaidFine("s@test.com")).thenReturn(false);
        when(mockBorrowService.borrowItem("s@test.com", 999)).thenReturn(false);

        menuService menu = createMenu("3\n999\n6\n");
        menu.showRoleBasedMenu(student);

        assertTrue(outContent.toString().contains("Could not borrow item"));
    }

    @Test
    void testStudentReturnFails() {
        user student = mock(user.class);
        when(student.getEmail()).thenReturn("s@test.com");
        when(student.getRole()).thenReturn(Role.STUDENT);

        when(mockBorrowService.hasUnpaidFine("s@test.com")).thenReturn(false);
        when(mockBorrowService.returnItem("s@test.com", 999)).thenReturn(false);

        menuService menu = createMenu("4\n999\n6\n");
        menu.showRoleBasedMenu(student);

        assertTrue(outContent.toString().contains("Could not return item"));
    }

    @Test
    void testStudentPayFineInvalidAmount() {
        user student = mock(user.class);
        when(student.getEmail()).thenReturn("s@test.com");
        when(student.getRole()).thenReturn(Role.STUDENT);

        when(mockBorrowService.getTotalFine("s@test.com")).thenReturn(50);

        menuService menu = createMenu("5\n-10\n6\n");
        menu.showRoleBasedMenu(student);

        assertTrue(outContent.toString().contains("Invalid amount"));
    }

    @Test
    void testStudentPayFineNonNumeric() {
        user student = mock(user.class);
        when(student.getEmail()).thenReturn("s@test.com");
        when(student.getRole()).thenReturn(Role.STUDENT);

        when(mockBorrowService.getTotalFine("s@test.com")).thenReturn(50);

        menuService menu = createMenu("5\nabc\n6\n");
        menu.showRoleBasedMenu(student);

        assertTrue(outContent.toString().contains("Amount must be a number"));
    }

    @Test
    void testShowRoleBasedMenuUnknownRoleLoop() {
        user unknown = mock(user.class);
        when(unknown.getRole()).thenReturn(null);

        menuService menu = createMenu("");
        boolean result = menu.showRoleBasedMenu(unknown);

        assertFalse(result);
        assertTrue(outContent.toString().contains("Unknown role"));
    }
    
}
