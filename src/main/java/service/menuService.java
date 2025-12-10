package service;

import domain.*;

import java.io.Console;
import java.util.List;
import java.util.Scanner;

/**
 * Service class responsible for handling the console-based menu interactions
 * of the library system.
 * <p>
 * Provides the main menu, role-based menus (Admin, Librarian, Student), and
 * handles all user actions such as sign-up, login, borrowing, returning items,
 * searching items, sending fine reminders, and managing users.
 * </p>
 *
 * <p>This class interacts with {@link userService}, {@link ItemsService},
 * {@link BorrowService}, and {@link EmailService} for business logic and
 * database operations.</p>
 *
 * @author Shatha , Sara
 * @version 1.0
 */
public class menuService {

    private final Scanner scanner;
    private final userService userService;
    private final ItemsService itemsService;
    private final BorrowService borrowService;
    private final EmailService emailService;

    /**
     * Constructs a {@link menuService} with the specified scanner and services.
     *
     * @param scanner      the Scanner for reading user input
     * @param userService  the service for user management
     * @param itemsService the service for item management
     * @param borrowService the service for borrowing/returning items
     * @param emailService the service for sending emails
     */
    public menuService(Scanner scanner, userService userService, ItemsService itemsService,
                       BorrowService borrowService, EmailService emailService) {
        this.scanner = scanner;
        this.userService = userService;
        this.itemsService = itemsService;
        this.borrowService = borrowService;
        this.emailService = emailService;
    }

    /**
     * Displays the main menu and handles user choice for sign-up, login, or exit.
     */
    public void showMainMenu() {
        System.out.println("===== Welcome to the Library System =====");

        while (true) {
            System.out.println("\nChoose an option:");
            System.out.println("1. Sign Up");
            System.out.println("2. Log In");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> handleSignUp();
                case "2" -> handleLogin();
                case "3" -> {
                    System.out.println("👋 Exiting... Goodbye!");
                    return;
                }
                default -> System.out.println("❌ Invalid option. Please try again.");
            }
        }
    }

    /**
     * Handles user sign-up workflow including password confirmation
     * and registration via {@link userService}.
     */
    public void handleSignUp() {
        System.out.println("\n--- Sign Up ---");
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();

        String password = readPasswordHidden("Enter password (min 8 chars): ");
        String confirm = readPasswordHidden("Confirm password: ");

        try {
            boolean success = userService.registerUser(email, password, confirm);
            if (success) System.out.println("✅ User registered successfully!");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    /**
     * Handles user login workflow, authenticates via {@link userService},
     * and routes the user to the appropriate role-based menu.
     */
    public void handleLogin() {
        System.out.println("\n--- Log In ---");
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();

        String password = readPasswordHidden("Enter password: ");

        try {
            user user = userService.authenticate(email, password);
            System.out.println("\n✅ Login successful! Welcome ");
            showRoleBasedMenu(user);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    /**
     * Displays a menu based on the user's role and handles role-specific options.
     *
     * @param user the authenticated user
     */
    public void showRoleBasedMenu(user user) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n===== " + user.getRole() + " Interface =====");
            switch (user.getRole()) {
                case ADMIN -> loggedIn = showAdminMenu();
                case LIBRARIAN -> loggedIn = showLibrarianMenu();
                case STUDENT -> loggedIn = showStudentMenu(user);
                default -> {
                    System.out.println("❌ Unknown role.");
                    loggedIn = false;
                }
            }
        }
    }

    /**
     * Shows the admin menu and handles admin-specific actions.
     *
     * @return false if the admin chooses to logout, true otherwise
     */
    public boolean showAdminMenu() {
        System.out.println("\n--- Admin Menu ---");
        System.out.println("1. See Inactive Accounts");
        System.out.println("2. Delete Inactive Account");
        System.out.println("3. Change User's Role");
        System.out.println("4. Add Book / CD");
        System.out.println("5. Send Fine Reminder Emails");
        System.out.println("6. Logout");
        System.out.print("Choose option: ");

        String choice = scanner.nextLine();
        switch (choice) {
            case "1" -> showInactiveAccounts();
            case "2" -> deleteInactiveAccount();
            case "3" -> changeUserRole();
            case "4" -> handleAddItem();
            case "5" -> sendFineReminders();
            case "6" -> {
                System.out.println("🚪 Logging out...");
                return false;
            }
            default -> System.out.println("❌ Invalid choice.");
        }
        return true;
    }

    /**
     * Displays a list of inactive user accounts.
     */
    private void showInactiveAccounts() {
        System.out.println("\n📋 Inactive Accounts:");
        List<user> inactiveUsers = userService.getInactiveUsers();
        if (inactiveUsers.isEmpty()) {
            System.out.println("✅ No inactive users found.");
        } else {
            inactiveUsers.forEach(u ->
                    System.out.println("• " + u.getEmail() + " | Role: " + u.getRole())
            );
        }
    }

    /**
     * Deletes an inactive user account by email via {@link userService}.
     */
    void deleteInactiveAccount() {
        System.out.print("\nEnter email of inactive account to delete: ");
        String email = scanner.nextLine().trim();

        boolean success = userService.removeInactiveUser(email);
        if (success)
            System.out.println("🗑️ Account deleted successfully.");
        else
            System.out.println("❌ Account not found or not inactive.");
    }

    /**
     * Changes a user's role by email via {@link userService}.
     */
    void changeUserRole() {
        System.out.print("\nEnter user email: ");
        String email = scanner.nextLine().trim();

        System.out.println("Choose new role:");
        System.out.println("1. ADMIN");
        System.out.println("2. LIBRARIAN");
        System.out.println("3. STUDENT");
        System.out.print("Enter choice: ");
        String choice = scanner.nextLine();

        Role newRole = switch (choice) {
            case "1" -> Role.ADMIN;
            case "2" -> Role.LIBRARIAN;
            case "3" -> Role.STUDENT;
            default -> null;
        };

        if (newRole == null) {
            System.out.println("❌ Invalid role choice.");
            return;
        }

        boolean updated = userService.updateUserRole(email, newRole);
        if (updated)
            System.out.println("✅ Role updated successfully.");
        else
            System.out.println("❌ User not found.");
    }

    /**
     * Sends fine reminder emails to users with unpaid fines via {@link BorrowService} and {@link EmailService}.
     */
    void sendFineReminders() {
        System.out.println("📧 Sending fine reminder emails...");

        List<String> unpaidEmails = borrowService.getStudentsWithUnpaidFines();

        if (unpaidEmails.isEmpty()) {
            System.out.println("✔ No users with unpaid fines.");
            return;
        }

        for (String email : unpaidEmails) {
            String subject = "Library Fine Reminder";
            String body =
                    "Dear Student,\n\n" +
                            "You have unpaid library fines in your account. " +
                            "Please settle them as soon as possible to avoid borrowing restrictions.\n\n" +
                            "Best regards,\nLibrary Admin";

            emailService.sendEmail(email, subject, body);
        }

        System.out.println("✔ Fine reminder emails sent to " + unpaidEmails.size() + " students.");
    }

    /**
     * Shows the librarian menu and handles librarian-specific actions.
     *
     * @return false if the librarian chooses to logout, true otherwise
     */
    boolean showLibrarianMenu() {
        System.out.println("\n--- Librarian Menu ---");
        System.out.println("1. See Overdue users");
        System.out.println("2. Logout");
        System.out.print("Choose option: ");

        String choice = scanner.nextLine();
        switch (choice) {
            case "1" -> {
                System.out.println("📨 [Librarian] Overdue Users:");
                List<Borrow> overdueList = borrowService.getOverdueStudents();

                if (overdueList.isEmpty()) {
                    System.out.println("No overdue users found.");
                } else {
                    System.out.println("student_email      ISBN  borrow_date  overdue_date fine");
                    overdueList.forEach(System.out::println);
                }
            }
            case "2" -> {
                System.out.println("🚪 Logging out...");
                return false;
            }
            default -> System.out.println("❌ Invalid choice.");
        }
        return true;
    }

    /**
     * Shows the student menu and handles student-specific actions such as
     * searching items, borrowing, returning, and paying fines.
     *
     * @param user the logged-in student
     * @return false if the student chooses to logout, true otherwise
     */
    public boolean showStudentMenu(user user) {
        // Implementation omitted for brevity
        return true; // Placeholder
    }

    /**
     * Handles adding a new item or increasing quantity for an existing item via {@link ItemsService}.
     */
    public void handleAddItem() {
        // Implementation omitted for brevity
    }

    /**
     * Handles searching books by name, author, or ISBN via {@link ItemsService}.
     */
    public void handleSearchBook() {
        // Implementation omitted for brevity
    }

    /**
     * Handles searching CDs by name, author, or ISBN via {@link ItemsService}.
     */
    public void handleSearchCD() {
        // Implementation omitted for brevity
    }

    /**
     * Reads a password from the console without displaying it, or via scanner if console is unavailable.
     *
     * @param prompt the prompt message
     * @return the entered password as a String
     */
    public String readPasswordHidden(String prompt) {
        Console console = System.console();
        if (console != null) {
            char[] passArray = console.readPassword(prompt);
            return new String(passArray);
        } else {
            System.out.print(prompt + " ");
            return scanner.nextLine();
        }
    }

    /**
     * Prints a list of items with a specific type.
     *
     * @param items the list of items
     * @param expectedType the expected type of items to print
     */
    private void printItems(List<Items> items, libraryType expectedType) {
        if (items.isEmpty()) {
            System.out.println("⚠️ No items found.");
            return;
        }

        System.out.println("\nResults:");
        for (Items item : items) {
            if (item.getType() != expectedType) continue;
            printSingleItem(item);
        }
    }

    /**
     * Prints details of a single item.
     *
     * @param item the item to print
     */
    private void printSingleItem(Items item) {
        System.out.println("------------------------------------");
        System.out.println("ISBN:     " + item.getISBN());
        System.out.println("Name:     " + item.getName());
        System.out.println("Author:   " + item.getAuthor());
        System.out.println("Type:     " + item.getType());
        System.out.println("Quantity: " + item.getQuantity());
    }
}
