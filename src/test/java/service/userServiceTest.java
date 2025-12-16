package service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.userRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import domain.user;
import domain.Role;
import util.PasswordHasher;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class userServiceTest {
    private userRepository userRepository;
    private EmailService emailService;
    private userService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(userRepository.class);
        emailService = mock(EmailService.class);
        userService = new userService(userRepository, emailService);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void registerUser() {
        String email = "sara@gmail.com";
        String password = "sarasara";
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(user.class)))
                .thenReturn(true);
        boolean result = userService.registerUser(email, password, password);
        assertTrue(result);
        verify(userRepository, times(1)).save(any(user.class));
        verify(emailService, times(1))
                .sendEmail(eq(email), anyString(), anyString());

    }

    @Test
    void registerUserWithEmailAlreadyUsedException() {
        String email = "sara@gmail.com";
        String password = "sarasara";

        user existing = new user(email, Role.STUDENT, null);
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(existing));

        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(email, password, password)
        );

        assertEquals("This email is already used.", ex.getMessage());

        verify(userRepository, never()).save(any(user.class));
        verify(emailService, never())
                .sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void registerUserWithShortPasswordException() {
        String email = "sara@gmail.com";
        String password = "sara";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(email, password, password)
        );

        assertEquals("Password must be at least 8 characters and match the confirmation.", ex.getMessage());

        verify(userRepository, never()).save(any(user.class));
        verify(emailService, never())
                .sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void registerUserPasswordMismatchException() {
        String email = "sara@gmail.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(email, "password123", "notMatching")
        );

        assertEquals("Password must be at least 8 characters and match the confirmation.", ex.getMessage());

        verify(userRepository, never()).save(any(user.class));
        verify(emailService, never())
                .sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void registerUserSaveFailsDoesNotSendEmail() {
        String email = "sara@gmail.com";
        String password = "sarasara";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(user.class)))
                .thenReturn(false);

        boolean result = userService.registerUser(email, password, password);

        assertFalse(result);
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void authenticate() {
        String email = "sara@gmail.com";
        String password = "sarasara";
        String hashed = PasswordHasher.hashPassword(password);
        user u = new user(email, Role.STUDENT, hashed);
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(u));
        user result = userService.authenticate(email, password);
        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }

    @Test
    void authenticateWrongPasswordException() {
        String email = "sara@gmail.com";
        String correctPassword = "sarasara";
        String wrongPassword = "notCorrect123";

        String hashed = PasswordHasher.hashPassword(correctPassword);
        user u = new user(email, Role.STUDENT, hashed);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(u));

        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.authenticate(email, wrongPassword)
        );

        assertEquals("Incorrect password.", ex.getMessage());
    }

    @Test
    void authenticateEmailNotFoundThrowsException() {
        String email = "sara@gmail.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.authenticate(email, "anyPassword123")
        );

        assertEquals("No user found with this email.", ex.getMessage());
    }

    @Test
    void getInactiveUsers() {
        user u1 = new user("sara@gmail.com", Role.STUDENT, null);
        user u2 = new user("sara1@gmail.com", Role.STUDENT, null);
        when(userRepository.findInactiveUsersSince(any()))
                .thenReturn(java.util.List.of(u1, u2));
        java.util.List<user> result = userService.getInactiveUsers();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(u1, result.get(0));
        assertEquals(u2, result.get(1));
        verify(userRepository, times(1)).findInactiveUsersSince(any());

    }

    @Test
    void removeInactiveUser() {
        String email = "sara@gmail.com";
        when(userRepository.softDeleteInactiveUser(eq(email), any()))
                .thenReturn(true);
        boolean result = userService.removeInactiveUser(email);
        assertTrue(result);
        verify(userRepository, times(1)).softDeleteInactiveUser(eq(email), any());
    }

    @Test
    void updateUserRole() {
        String email = "sara@gmail.com";
        Role role = Role.LIBRARIAN;
        when(userRepository.updateRole(email, role))
                .thenReturn(true);
        boolean result = userService.updateUserRole(email, role);
        assertTrue(result);
        verify(userRepository, times(1)).updateRole(email, role);
    }
}
