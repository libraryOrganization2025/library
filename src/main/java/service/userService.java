package service;

import domain.Role;
import domain.user;
import repository.UserRepo;
import repository.userRepository;
import util.PasswordHasher;

import java.time.LocalDate;
import java.util.List;

/**
 * Service class responsible for managing user registration, authentication,
 * role updates, and inactive user handling.
 * <p>
 * Interacts with {@link userRepository} for persistence and {@link EmailService}
 * for sending email notifications.
 * </p>
 *
 * @author Sara
 * @version 1.0
 */
public class userService {

    private final UserRepo userRepository;
    private final EmailService emailService;

    /**
     * Constructs a {@link userService} with the specified repositories and services.
     *
     * @param userRepository the repository used for user-related operations
     * @param emailService the service used for sending emails
     */
    public userService(UserRepo userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    /**
     * Registers a new user in the system.
     * <p>
     * Validates that the email is available and the password is valid.
     * Sends a welcome email upon successful registration.
     * </p>
     *
     * @param email the email address of the new user
     * @param password the password for the account
     * @param confirmPassword confirmation of the password
     * @return {@code true} if the user was successfully registered; {@code false} otherwise
     * @throws IllegalArgumentException if the email is already used or the password is invalid
     */
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

    /**
     * Authenticates a user by checking email and password.
     *
     * @param email the user's email address
     * @param password the password to verify
     * @return the {@link user} object if authentication is successful
     * @throws IllegalArgumentException if no user exists with the email or the password is incorrect
     */
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

    /**
     * Retrieves a list of users who have been inactive for more than a year.
     *
     * @return a {@link List} of inactive {@link user} objects
     */
    public List<user> getInactiveUsers() {
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        return userRepository.findInactiveUsersSince(oneYearAgo);
    }

    /**
     * Soft deletes an inactive user.
     *
     * @param email the email of the user to remove
     * @return {@code true} if the user was successfully marked as deleted; {@code false} otherwise
     */
    public boolean removeInactiveUser(String email) {
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        return userRepository.softDeleteInactiveUser(email, oneYearAgo);
    }

    /**
     * Updates the role of a user.
     *
     * @param email the email of the user
     * @param newRole the new {@link Role} to assign
     * @return {@code true} if the role was successfully updated; {@code false} otherwise
     */
    public boolean updateUserRole(String email, Role newRole) {
        return userRepository.updateRole(email, newRole);
    }

    /**
     * Checks if an email is available for registration.
     *
     * @param email the email to check
     * @return {@code true} if the email is not used; {@code false} otherwise
     */
    private boolean isEmailAvailable(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }

    /**
     * Validates password and confirmation.
     *
     * @param password the password
     * @param confirmPassword the confirmation password
     * @return {@code true} if password is valid and matches confirmation; {@code false} otherwise
     */
    private boolean isValidPassword(String password, String confirmPassword) {
        return password != null && password.length() >= 8 && password.equals(confirmPassword);
    }
}
