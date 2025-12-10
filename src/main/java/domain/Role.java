package domain;

/**
 * Represents user roles within the library system.
 * Each role has an associated permission level.
 *
 * <p>Roles include:
 * <ul>
 *     <li>ADMIN</li>
 *     <li>LIBRARIAN</li>
 *     <li>STUDENT</li>
 * </ul>
 *
 * @author Sara
 * @version 1.0
 */
public enum Role {

    /** Administrative role with the highest privileges. */
    ADMIN(1),

    /** Librarian role with mid-level access. */
    LIBRARIAN(2),

    /** Student role with limited permissions. */
    STUDENT(3);

    /** Numeric access level. */
    private final int level;

    /**
     * Creates a role with a permission level.
     *
     * @param level the role level
     */
    Role(int level) {
        this.level = level;
    }

    /**
     * @return the numeric role level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Converts a numeric level to a role.
     *
     * @param level the level value
     * @return the matching role
     * @throws IllegalArgumentException if level is invalid
     */
    public static Role fromLevel(int level) {
        for (Role role : Role.values()) {
            if (role.getLevel() == level) return role;
        }
        throw new IllegalArgumentException("Invalid role level: " + level);
    }

    /**
     * Converts a string into a role.
     * Accepts case-insensitive values.
     *
     * @param str the role name
     * @return the matching role
     */
    public static Role fromString(String str) {
        return Role.valueOf(str.toUpperCase());
    }
}
