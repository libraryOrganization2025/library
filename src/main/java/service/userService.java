package service;

import domain.Role;
import domain.user;
import repository.userRepository;
import util.PasswordHasher;

import java.time.LocalDate;
import java.util.List;

public class userService {
    private final userRepository userRepository;
    private final EmailService emailService;

    public userService(userRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public boolean registerUser(String email, String password, String confirmPassword) {
        if (!isEmailAvailable(email)) {
            throw new IllegalArgumentException("This email is already used.");
        }

        if (!isValidPassword(password, confirmPassword)) {
            throw new IllegalArgumentException("Password must be at least 8 characters and match the confirmation.");
        }

        String hashedPassword = PasswordHasher.hashPassword(password);
        user user = new user(email, Role.STUDENT, hashedPassword);

        boolean saved = userRepository.save(user);

        if (saved) {
            String subject = "Welcome to the Library System";
            String text = """
                    Hello %s,
                    
                    Your account has been created successfully in the Library System.
                    
                    Enjoy your journey! 📚
                    """.formatted(email);

            emailService.sendEmail(email, subject, text);
        }

        return saved;
    }

    public user authenticate(String email, String password) {
        user user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No user found with this email."));

        String hashedPassword = PasswordHasher.hashPassword(password);
        if (hashedPassword.equals(user.getPasswordHash())) {
            return user;
        } else {
            throw new IllegalArgumentException("Incorrect password.");
        }
    }

    public List<user> getInactiveUsers() {
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        return userRepository.findInactiveUsersSince(oneYearAgo);
    }

    public boolean removeInactiveUser(String email) {
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        return userRepository.softDeleteInactiveUser(email, oneYearAgo);
    }

    public boolean updateUserRole(String email, Role newRole) {
        return userRepository.updateRole(email, newRole);
    }

    private boolean isEmailAvailable(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }

    private boolean isValidPassword(String password, String confirmPassword) {
        return password != null && password.length() >= 8 && password.equals(confirmPassword);
    }
}
