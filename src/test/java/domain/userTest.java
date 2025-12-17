package domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class userTest {

    @Test
    void getEmail() {
        user u = new user("sara@gmail.com", Role.STUDENT, "abc123");
        assertEquals("sara@gmail.com", u.getEmail());
    }

    @Test
    void getRole() {
        user u = new user("sara@gmail.com", Role.STUDENT, "abc123");
        assertEquals(Role.STUDENT, u.getRole());
    }

    @Test
    void getPasswordHash() {
        user u = new user("sara@gmail.com", Role.STUDENT, "abc123");
        assertEquals("abc123", u.getPasswordHash());
    }

    @Test
    void getLastDateBorrowed() {
        LocalDate date = LocalDate.of(2024, 12, 1);
        user u = new user("a@a.com", Role.STUDENT, "h", date, LocalDateTime.now(), null);
        assertEquals(date, u.getLastDateBorrowed());
    }

    @Test
    void setLastDateBorrowed() {
        user u = new user("a@a.com", Role.STUDENT, "pass");
        LocalDate newDate = LocalDate.of(2025, 1, 5);
        u.setLastDateBorrowed(newDate);
        assertEquals(newDate, u.getLastDateBorrowed());
    }
}