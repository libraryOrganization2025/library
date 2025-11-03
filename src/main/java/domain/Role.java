package domain;

public enum  Role {
    ADMIN(1),
    LIBRARIAN(2),
    STUDENT(3);
    private final int level;

    Role(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public static Role fromLevel(int level) {
        for (Role role : Role.values()) {
            if (role.getLevel() == level) return role;
        }
        throw new IllegalArgumentException("Invalid role level: " + level);
    }

    public static Role fromString(String str) {
        return Role.valueOf(str.toUpperCase());
    }
}
