package service;

import domain.Role;
import domain.user;

import java.util.List;
import java.util.Scanner;

public class menuService {
    private final Scanner scanner;
    private final userService userService;

    public menuService(Scanner scanner, userService userService) {
        this.scanner = scanner;
        this.userService = userService;
    }

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

    private void handleSignUp() {
        System.out.println("\n--- Sign Up ---");
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter password (min 8 chars): ");
        String password = scanner.nextLine();

        System.out.print("Confirm password: ");
        String confirm = scanner.nextLine();

        try {
            boolean success = userService.registerUser(email, password, confirm);
            if (success) System.out.println("✅ User registered successfully!");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    private void handleLogin() {
        System.out.println("\n--- Log In ---");
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try {
            user user = userService.authenticate(email, password);
            System.out.println("\n✅ Login successful! Welcome, " + user.getEmail());
            showRoleBasedMenu(user);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    private void showRoleBasedMenu(user user) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n===== " + user.getRole() + " Interface =====");
            switch (user.getRole()) {
                case ADMIN -> loggedIn = showAdminMenu();
                case LIBRARIAN -> loggedIn = showLibrarianMenu();
                case STUDENT -> loggedIn = showStudentMenu();
                default -> {
                    System.out.println("❌ Unknown role.");
                    loggedIn = false;
                }
            }
        }
    }

    private boolean showAdminMenu() {
        System.out.println("\n--- Admin Menu ---");
        System.out.println("1. See Inactive Accounts");
        System.out.println("2. Delete Inactive Account");
        System.out.println("3. Change User's Role");
        System.out.println("4. Add Book (not implemented)");
        System.out.println("5. Logout");
        System.out.print("Choose option: ");

        String choice = scanner.nextLine();
        switch (choice) {
            case "1" -> showInactiveAccounts();
            case "2" -> deleteInactiveAccount();
            case "3" -> changeUserRole();
            case "4" -> System.out.println("📚 [Admin] Add Book selected (not implemented yet).");
            case "5" -> {
                System.out.println("🚪 Logging out...");
                return false;
            }
            default -> System.out.println("❌ Invalid choice.");
        }
        return true;
    }

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

    private void deleteInactiveAccount() {
        System.out.print("\nEnter email of inactive account to delete: ");
        String email = scanner.nextLine().trim();

        boolean success = userService.removeInactiveUser(email);
        if (success)
            System.out.println("🗑️ Account deleted successfully.");
        else
            System.out.println("❌ Account not found or not inactive.");
    }

    private void changeUserRole() {
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

    private boolean showLibrarianMenu() {
        System.out.println("\n--- Librarian Menu ---");
        System.out.println("1. See Inactive Accounts");
        System.out.println("2. Logout");
        System.out.print("Choose option: ");

        String choice = scanner.nextLine();
        switch (choice) {
            case "1" -> System.out.println("📨 [Librarian] See Inactive Accounts selected (not implemented yet).");
            case "2" -> {
                System.out.println("🚪 Logging out...");
                return false;
            }
            default -> System.out.println("❌ Invalid choice.");
        }
        return true;
    }

    private boolean showStudentMenu() {
        System.out.println("\n--- Student Menu ---");
        System.out.println("1. Search Book");
        System.out.println("2. Search CD");
        System.out.println("3. Logout");
        System.out.print("Choose option: ");

        String choice = scanner.nextLine();
        switch (choice) {
            case "1" -> System.out.println("📖 [Student] Search Book selected (not implemented yet).");
            case "2" -> System.out.println("💿 [Student] Search CD selected (not implemented yet).");
            case "3" -> {
                System.out.println("🚪 Logging out...");
                return false;
            }
            default -> System.out.println("❌ Invalid choice.");
        }
        return true;
    }
}