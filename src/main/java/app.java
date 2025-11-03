import domain.Role;
import domain.user;
import repository.userRepository;
import service.menuService;
import service.userService;

import java.util.List;
import java.util.Scanner;

public class app {

    public static void main(String[] args) {
        System.out.println("===== Welcome to the Library System =====");
        Scanner scanner = new Scanner(System.in);
        userRepository userRepository = new userRepository();
        userService userService = new userService(userRepository);
        menuService menuService = new menuService(scanner, userService);

        menuService.showMainMenu();
        scanner.close();
    }
}
