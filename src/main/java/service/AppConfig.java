package service;

import repository.BorrowRepository;
import repository.ItemsRepository;
import repository.userRepository;
import java.util.Scanner;
public class AppConfig {
    public final Scanner scanner;
    public final EmailService emailService;
    public final userRepository userRepository;
    public final userService userService;
    public final ItemsRepository itemsRepository;
    public final ItemsService itemsService;
    public final BorrowRepository borrowRepository;
    public final BorrowService borrowService;
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
