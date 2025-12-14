import repository.BorrowRepository;
import repository.userRepository;
import repository.ItemsRepository;
import service.*;
import config.config;

import java.util.Scanner;

public class app {

    public static void main(String[] args) {
        System.out.println("===== Welcome to the Library System =====");

        System.out.println(config.EMAIL + config.EMAIL_PASSWORD);
        AppConfig _appConfig = new AppConfig(config.EMAIL, config.EMAIL_PASSWORD);
        menuService menuService = new menuService(_appConfig.scanner, _appConfig.userService, _appConfig.itemsService , _appConfig.borrowService , _appConfig.emailService);
        menuService.showMainMenu();

        _appConfig.scanner.close();
    }
}
