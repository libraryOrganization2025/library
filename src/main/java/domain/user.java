package domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class user {
    private String email;
    private Role role;
    private String passwordHash;
    private LocalDate lastDateBorrowed;
    private LocalDateTime createdOn;
    private LocalDateTime deletedOn;

    // Constructor
    public user(String email, Role role, String passwordHash) {
        this.email = email;
        this.role = role;
        this.passwordHash = passwordHash;
    }

    public user(String email, Role role, String password_hash,
                LocalDate lastBorrowedDate, LocalDateTime createdOn, LocalDateTime deletedOn) {
        this.email = email;
        this.role = role;
        this.passwordHash = password_hash;
        this.lastDateBorrowed = lastBorrowedDate;
        this.createdOn = createdOn;
        this.deletedOn = deletedOn;
    }

    public user(String mail, Role role) {
        this.email = mail;
        this.role = role;
    }

    // Getters and setters
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public String getPasswordHash() { return passwordHash; }
    public LocalDate getLastDateBorrowed() { return lastDateBorrowed; }
    public void setLastDateBorrowed(LocalDate lastDateBorrowed) { this.lastDateBorrowed = lastDateBorrowed; }
}
