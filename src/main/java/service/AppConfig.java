package service;

import repository.BorrowRepository;
import repository.ItemsRepository;
import repository.userRepository;
import java.util.Scanner;

/**
 * Central configuration class for initializing and providing access
 * to all services and repositories used in the library application.
 *
 * <p>This class sets up:
 * <ul>
 *     <li>Scanner for user input</li>
 *     <li>Email service for notifications</li>
 *     <li>User repository and service</li>
 *     <li>Items repository and service</li>
 *     <li>Borrow repository and service</li>
 * </ul>
 *
 * <p>By instantiating this class, all dependencies are created and ready to use,
 * following a basic dependency injection pattern.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 *     AppConfig config = new AppConfig("yourEmail@example.com", "password123");
 *     userService userSvc = config.userService;
 *     ItemsService itemsSvc = config.itemsService;
 * </pre>
 *
 * @author Sara
 * @version 1.0
 */
public class AppConfig {
    /** Scanner for console input. */
    public final Scanner scanner;

    /** Email service for sending notifications. */
    public final EmailService emailService;

    /** Repository for user-related database operations. */
    public final userRepository userRepository;

    /** Service layer for managing users. */
    public final userService userService;

    /** Repository for item-related database operations. */
    public final ItemsRepository itemsRepository;

    /** Service layer for managing items. */
    public final ItemsService itemsService;

    /** Repository for borrowing-related database operations. */
    public final BorrowRepository borrowRepository;

    /** Service layer for managing borrow operations. */
    public final BorrowService borrowService;

    /**
     * Constructs the application configuration with the specified email credentials.
     * All repositories and services are initialized here.
     *
     * @param emailUsername the username/email for EmailService
     * @param emailPassword the password for EmailService
     */
    public AppConfig(String emailUsername, String emailPassword) {
        this.scanner = new Scanner(System.in);
        this.emailService = new EmailService(emailUsername, emailPassword);
        this.userRepository = new userRepository();
        this.itemsRepository = new ItemsRepository();
        this.borrowRepository = new BorrowRepository();
        this.userService = new userService(userRepository, emailService);
        this.itemsService = new ItemsService(itemsRepository);
        this.borrowService = new BorrowService(borrowRepository, itemsRepository);
    }
}
