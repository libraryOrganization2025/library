package repository;

import domain.Role;
import domain.user;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface responsible for managing {@link user} entities in the database.
 * Provides CRUD operations, role updates, and management of inactive users.
 */
public interface UserRepo {

    /**
     * Persists a new user record into the database.
     *
     * @param user the user object to save
     * @return true if the user was successfully inserted; false otherwise
     */
    boolean save(user user);

    /**
     * Retrieves a user by their email address.
     *
     * @param email the email of the user to retrieve
     * @return an Optional containing the user if found, or empty otherwise
     */
    Optional<user> findByEmail(String email);

    /**
     * Finds all users who have been inactive since a specified date.
     *
     * @param oneYearAgo the date threshold to determine inactivity
     * @return a list of inactive users
     */
    List<user> findInactiveUsersSince(LocalDate oneYearAgo);

    /**
     * Soft deletes a user by setting their deletedOn timestamp.
     *
     * @param email the email of the user to soft delete
     * @param oneYearAgo the date threshold for inactivity
     * @return true if a user was successfully marked as deleted; false otherwise
     */
    boolean softDeleteInactiveUser(String email, LocalDate oneYearAgo);

    /**
     * Updates the role of an existing user.
     *
     * @param email the email of the user whose role is to be updated
     * @param newRole the new Role to assign to the user
     * @return true if the role was successfully updated; false otherwise
     */
    boolean updateRole(String email, Role newRole);
}
