package domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a system user in the library.
 * Stores login information, role, and timestamps.
 *
 * <p>Users may be:
 * <ul>
 *     <li>ADMIN</li>
 *     <li>LIBRARIAN</li>
 *     <li>STUDENT</li>
 * </ul>
 *
 * @author Sara
 * @version 1.0
 */
public class user {

    /** Email address used as the user's identifier. */
    private String email;

    /** The role of the user (Admin, Librarian, Student). */
    private Role role;

    /** The hashed password stored for authentication. */
    private String passwordHash;

    /** The last date the user borrowed an item. */
    private LocalDate lastDateBorrowed;

    /** Timestamp of account creation. */
    private LocalDateTime createdOn;

    /** Timestamp when the account was marked deleted (if any). */
    private LocalDateTime deletedOn;

    /**
     * Creates a basic user with email, role, and password.
     *
     * @param email the user's email
     * @param role the user's role
     * @param passwordHash the hashed password
     */
    public user(String email, Role role, String passwordHash) {
        this.email = email;
        this.role = role;
        this.passwordHash = passwordHash;
    }

    /**
     * Creates a full user with all timestamp fields.
     *
     * @param email the user's email
     * @param role the user role
     * @param password_hash the hashed password
     * @param lastBorrowedDate last borrow date
     * @param createdOn account creation timestamp
     * @param deletedOn account deletion timestamp
     */
    public user(String email, Role role, String password_hash,
                LocalDate lastBorrowedDate, LocalDateTime createdOn, LocalDateTime deletedOn) {
        this.email = email;
        this.role = role;
        this.passwordHash = password_hash;
        this.lastDateBorrowed = lastBorrowedDate;
        this.createdOn = createdOn;
        this.deletedOn = deletedOn;
    }

    /**
     * Creates a user with only email and role.
     *
     * @param mail the user's email
     * @param role the user's role
     */
    public user(String mail, Role role) {
        this.email = mail;
        this.role = role;
    }
    /**
     * Default constructor for creating an empty user object.
     *
     * <p>This constructor initializes a user without setting any fields.
     * It can be used when the user details will be set later through setters.</p>
     */
    public user() {

    }

    /** @return the user's email */
    public String getEmail() { return email; }

    /** @return the user's role */
    public Role getRole() { return role; }

    /** @return the user's password hash */
    public String getPasswordHash() { return passwordHash; }

    /** @return the last date borrowed */
    public LocalDate getLastDateBorrowed() { return lastDateBorrowed; }

    /**
     * Sets the last date borrowed.
     *
     * @param lastDateBorrowed the new last borrowed date
     */
    public void setLastDateBorrowed(LocalDate lastDateBorrowed) {
        this.lastDateBorrowed = lastDateBorrowed;
    }
}
