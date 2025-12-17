package domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {


    @Test
    void getLevel() {
        assertEquals(1, Role.ADMIN.getLevel(), "ADMIN level should be 1");
        assertEquals(2, Role.LIBRARIAN.getLevel(), "LIBRARIAN level should be 2");
        assertEquals(3, Role.STUDENT.getLevel(), "STUDENT level should be 3");
    }

    @Test
    void fromLevel() {
        assertEquals(Role.ADMIN, Role.fromLevel(1));
        assertEquals(Role.LIBRARIAN, Role.fromLevel(2));
        assertEquals(Role.STUDENT, Role.fromLevel(3));
        assertThrows(IllegalArgumentException.class, () -> Role.fromLevel(0));
        assertThrows(IllegalArgumentException.class, () -> Role.fromLevel(99));
        assertThrows(IllegalArgumentException.class, () -> Role.fromLevel(-5));
    }

    @Test
    void fromString() {
        assertEquals(Role.ADMIN, Role.fromString("admin"));
        assertEquals(Role.LIBRARIAN, Role.fromString("LiBrArIaN"));
        assertEquals(Role.STUDENT, Role.fromString("STUDENT"));
        assertThrows(IllegalArgumentException.class, () -> Role.fromString("teacher"));
        assertThrows(IllegalArgumentException.class, () -> Role.fromString(""));
        assertThrows(IllegalArgumentException.class, () -> Role.fromString("123"));
    }

    @Test
    void values() {
        Role[] vals = Role.values();
        Role[] expected = { Role.ADMIN, Role.LIBRARIAN, Role.STUDENT };
        assertArrayEquals(expected, vals, "Role.values() should return enum constants in declaration order");
        assertEquals(3, vals.length, "There should be exactly 3 roles");
    }

    @Test
    void valueOf() {
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
        assertEquals(Role.LIBRARIAN, Role.valueOf("LIBRARIAN"));
        assertEquals(Role.STUDENT, Role.valueOf("STUDENT"));
        assertThrows(IllegalArgumentException.class, () -> Role.valueOf("UNKNOWN"));
    }
}