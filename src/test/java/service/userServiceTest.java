package service;

import domain.Role;
import domain.user;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import repository.UserRepo;
import repository.userRepository;
import util.PasswordHasher;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class userServiceTest {

    @Mock
    private UserRepo userRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private userService userService;

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
        verify(userRepository).save(any(user.class));
        verify(emailService)
                .sendEmail(eq(email), anyString(), anyString());
    }

    @Test
    void registerUserWithShortPasswordException() {
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser("a@test.com", "short", "short")
        );

        assertEquals(
                "Password must be at least 8 characters and match the confirmation.",
                ex.getMessage()
        );
    }

    @Test
    void authenticate() {
        String email = "sara@gmail.com";
        String password = "sarasara";

        user u = new user(
                email,
                Role.STUDENT,
                PasswordHasher.hashPassword(password)
        );

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(u));

        user result = userService.authenticate(email, password);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }

    @Test
    void getInactiveUsers() {
        user u1 = new user("a@test.com", Role.STUDENT, null);
        user u2 = new user("b@test.com", Role.STUDENT, null);

        when(userRepository.findInactiveUsersSince(any()))
                .thenReturn(List.of(u1, u2));

        List<user> result = userService.getInactiveUsers();

        assertEquals(2, result.size());
        verify(userRepository).findInactiveUsersSince(any());
    }

    @Test
    void registerUserEmailAlreadyUsedException() {
        when(userRepository.findByEmail("a@test.com"))
                .thenReturn(Optional.of(new user()));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser("a@test.com", "password123", "password123")
        );

        assertEquals("This email is already used.", ex.getMessage());
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void registerUserPasswordMismatchException() {
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser("a@test.com", "password123", "password321")
        );

        assertEquals(
                "Password must be at least 8 characters and match the confirmation.",
                ex.getMessage()
        );
    }

    @Test
    void registerUserSaveFailsNoEmailSent() {
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(user.class)))
                .thenReturn(false);

        boolean result = userService.registerUser(
                "a@test.com",
                "password123",
                "password123"
        );

        assertFalse(result);
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void authenticateWrongPasswordException() {
        user u = new user(
                "a@test.com",
                Role.STUDENT,
                PasswordHasher.hashPassword("correctPass")
        );

        when(userRepository.findByEmail("a@test.com"))
                .thenReturn(Optional.of(u));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.authenticate("a@test.com", "wrongPass")
        );

        assertEquals("Incorrect password.", ex.getMessage());
    }

    @Test
    void authenticateUserNotFoundException() {
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.authenticate("x@test.com", "pass")
        );

        assertEquals("No user found with this email.", ex.getMessage());
    }

    @Test
    void removeInactiveUserSuccess() {
        when(userRepository.softDeleteInactiveUser(anyString(), any()))
                .thenReturn(true);

        boolean result = userService.removeInactiveUser("a@test.com");

        assertTrue(result);
        verify(userRepository).softDeleteInactiveUser(anyString(), any());
    }

    @Test
    void updateUserRoleSuccess() {
        when(userRepository.updateRole("a@test.com", Role.ADMIN))
                .thenReturn(true);

        boolean result = userService.updateUserRole("a@test.com", Role.ADMIN);

        assertTrue(result);
        verify(userRepository).updateRole("a@test.com", Role.ADMIN);
    }


}
