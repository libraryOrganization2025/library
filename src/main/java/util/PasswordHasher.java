package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for hashing passwords using the SHA-256 algorithm.
 * <p>
 * Provides a static method to securely hash a plain-text password into a hexadecimal string.
 * </p>
 *
 * <p>This class is designed for password storage and comparison in authentication workflows.</p>
 *
 * @author Sara
 * @version 1.0
 */
public class PasswordHasher {

    /**
     * Hashes the given plain-text password using SHA-256.
     *
     * @param password the plain-text password to hash
     * @return the SHA-256 hashed password as a hexadecimal string
     * @throws RuntimeException if the SHA-256 algorithm is not available
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
