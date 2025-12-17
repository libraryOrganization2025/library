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
     * @return
     */
    public boolean showRoleBasedMenu(user user) {
        if (user == null || user.getRole() == null) {
            // Handle unknown or null role gracefully
            System.out.println("❌ Unknown role.");
            return false;
        }

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

        return true;
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
    void showInactiveAccounts() {
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
    private boolean showStudentMenu(user user) {
        System.out.println("\n--- Student Menu ---");
        System.out.println("1. Search Book");
        System.out.println("2. Search CD");
        System.out.println("3. Borrow Item");
        System.out.println("4. Return Item");
        System.out.println("5. Pay Fine (Partial or Total)");
        System.out.println("6. Logout");
        System.out.print("Choose option: ");

        String choice = scanner.nextLine();
        switch (choice) {
            case "1" -> handleSearchBook();
            case "2" -> handleSearchCD();
            case "3" -> {


                try {
                    if (borrowService.hasUnpaidFine(user.getEmail())) {
                        System.out.println("❌ You have unpaid fines. Pay before borrowing.");
                        break;
                    }

                    System.out.print("Enter ISBN to borrow: ");
                    int isbn = Integer.parseInt(scanner.nextLine().trim());
                    boolean ok = borrowService.borrowItem(user.getEmail(), isbn);

                    if (ok) System.out.println("📘 Borrow successful!");
                    else System.out.println("❌ Could not borrow item.");
                } catch (NumberFormatException e) {
                    System.out.println("❌ ISBN must be a number.");
                } catch (Exception e) {
                    System.out.println("❌ " + e.getMessage());
                }
            }
            case "4" -> {
                try {
                    if (borrowService.hasUnpaidFine(user.getEmail())) {
                        System.out.println("❌ You have unpaid fines. Pay before returning items.");
                        break;
                    }

                    System.out.print("Enter ISBN to return: ");
                    int isbn = Integer.parseInt(scanner.nextLine().trim());

                    boolean ok = borrowService.returnItem(user.getEmail(), isbn);
                    if (ok) System.out.println("✅ Item returned successfully.");
                    else System.out.println("❌ Could not return item (maybe not borrowed).");
                } catch (NumberFormatException e) {
                    System.out.println("❌ ISBN must be a number.");
                } catch (Exception e) {
                    System.out.println("❌ " + e.getMessage());
                }
            }
            case "5" ->{
                try {
                    int totalFine = borrowService.getTotalFine(user.getEmail());
                    if (totalFine == 0) {
                        System.out.println("🎉 You have no fines.");
                        break;
                    }
                    System.out.println("Your total fine: " + totalFine + " NIS");
                    System.out.print("Enter amount to pay: ");
                    int pay = Integer.parseInt(scanner.nextLine().trim());

                    if (pay <= 0) {
                        System.out.println("❌ Invalid amount.");
                        break;
                    }

                    borrowService.payFine(user.getEmail(), pay);

                    int remaining = borrowService.getTotalFine(user.getEmail());
                    System.out.println("💰 Payment successful. Remaining fine: " + remaining + " NIS");

                    if (remaining > 0)
                        System.out.println("⚠️ You still cannot borrow/return until full fine is paid.");
                } catch (NumberFormatException e) {
                    System.out.println("❌ Amount must be a number.");
                } catch (Exception e) {
                    System.out.println("❌ " + e.getMessage());
                }

            }
            case "6" -> {
                System.out.println("🚪 Logging out...");
                return false;
            }
            default -> System.out.println("❌ Invalid choice.");
        }
        return true;
    }

    /**
     * Handles adding a new item or increasing quantity for an existing item via {@link ItemsService}.
     */
    void handleAddItem() {
        if (!scanner.hasNextLine()) {
            return;
        }
        System.out.println("\n--- Add Item ---");

        System.out.println("Choose type:");
        System.out.println("1. BOOK");
        System.out.println("2. CD");
        System.out.print("Enter choice: ");
        String t = scanner.nextLine().trim();

        libraryType type = switch (t) {
            case "1" -> libraryType.BOOK;
            case "2" -> libraryType.CD;
            default -> null;
        };

        if (type == null) {
            System.out.println("❌ Invalid type.");
            return;
        }

        System.out.println("\nIs this item:");
        System.out.println("1. Existing (increase quantity)");
        System.out.println("2. New item");
        System.out.print("Enter choice: ");
        String choice = scanner.nextLine().trim();

        try {
            if ("1".equals(choice)) {
                System.out.print("Enter ISBN of existing item: ");
                String isbn = scanner.nextLine().trim();

                Items item = itemsService.searchByISBN(isbn);

                if (item.getType() != type) {
                    System.out.println("❌ This ISBN belongs to a " + item.getType() + " not a " + type + ".");
                    return;
                }

                boolean ok = itemsService.increaseQuantityByISBN(isbn);
                if (ok)
                    System.out.println("✅ Quantity increased by 1.");
                else
                    System.out.println("❌ Failed to increase quantity.");

            } else if ("2".equals(choice)) {
                System.out.print("Enter name: ");
                String name = scanner.nextLine().trim();

                System.out.print("Enter author: ");
                String author = scanner.nextLine().trim();

                System.out.print("Enter quantity: ");
                int quantity = Integer.parseInt(scanner.nextLine().trim());

                boolean ok = itemsService.addNewItem(name, author, quantity, type);
                if (ok)
                    System.out.println("✅ Item added successfully.");
                else
                    System.out.println("❌ Failed to add item.");

            } else {
                System.out.println("❌ Invalid choice.");
            }

        } catch (NumberFormatException e) {
            System.out.println("❌ Quantity must be a number.");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    /**
     * Handles searching books by name, author, or ISBN via {@link ItemsService}.
     */
    void handleSearchBook() {
        System.out.println("\n--- Search Book ---");
        System.out.println("1. Search by name");
        System.out.println("2. Search by author");
        System.out.println("3. Search by ISBN");
        System.out.println("4. Back");
        System.out.print("Enter choice: ");
        if (!scanner.hasNextLine()) {
            return;
        }

        String choice = scanner.nextLine().trim();

        try {
            switch (choice) {
                case "1" -> {
                    System.out.print("Enter book name (or part of it): ");
                    String name = scanner.nextLine().trim();
                    List<Items> items = itemsService.searchBooksByName(name);
                    printItems(items, libraryType.BOOK);
                }
                case "2" -> {
                    System.out.print("Enter author name (or part of it): ");
                    String author = scanner.nextLine().trim();
                    List<Items> items = itemsService.searchBooksByAuthor(author);
                    printItems(items, libraryType.BOOK);
                }
                case "3" -> {
                    System.out.print("Enter ISBN (number): ");
                    String isbn = scanner.nextLine().trim();
                    Items item = itemsService.searchByISBN(isbn);
                    if (item.getType() == libraryType.BOOK) {
                        printSingleItem(item);
                    } else {
                        System.out.println("❌ This ISBN belongs to a CD, not a Book.");
                    }
                }
                case "4" -> {
                    return;
                }
                default -> System.out.println("❌ Invalid choice.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    /**
     * Handles searching CDs by name, author, or ISBN via {@link ItemsService}.
     */
    void handleSearchCD() {
        System.out.println("\n--- Search CD ---");
        System.out.println("1. Search by name");
        System.out.println("2. Search by author");
        System.out.println("3. Search by ISBN");
        System.out.println("4. Back");
        System.out.print("Enter choice: ");
        if (!scanner.hasNextLine()) {
            return;
        }

        String choice = scanner.nextLine().trim();

        try {
            switch (choice) {
                case "1" -> {
                    System.out.print("Enter CD name (or part of it): ");
                    String name = scanner.nextLine().trim();
                    List<Items> items = itemsService.searchCDsByName(name);
                    printItems(items, libraryType.CD);
                }
                case "2" -> {
                    System.out.print("Enter artist/author (or part of it): ");
                    String author = scanner.nextLine().trim();
                    List<Items> items = itemsService.searchCDsByAuthor(author);
                    printItems(items, libraryType.CD);
                }
                case "3" -> {
                    System.out.print("Enter ISBN (number): ");
                    String isbn = scanner.nextLine().trim();
                    Items item = itemsService.searchByISBN(isbn);
                    if (item.getType() == libraryType.CD) {
                        printSingleItem(item);
                    } else {
                        System.out.println("❌ This ISBN belongs to a Book, not a CD.");
                    }
                }
                case "4" -> {
                    return;
                }
                default -> System.out.println("❌ Invalid choice.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
        }
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
