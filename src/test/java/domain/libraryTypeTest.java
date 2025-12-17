package domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class libraryTypeTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getType() {
        assertEquals(1, libraryType.CD.getType(), "CD type id should be 1");
        assertEquals(2, libraryType.BOOK.getType(), "Book type id should be 2");
    }

    @Test
    void chooseType() {
        assertEquals(libraryType.CD, libraryType.chooseType(1));
        assertEquals(libraryType.BOOK, libraryType.chooseType(2));
        assertThrows(IllegalArgumentException.class, () -> libraryType.chooseType(0));
        assertThrows(IllegalArgumentException.class, () -> libraryType.chooseType(99));
        assertThrows(IllegalArgumentException.class, () -> libraryType.chooseType(-5));
    }

   /* @Test
    void fromStringType() {
        assertEquals(libraryType.CD, libraryType.fromStringType("cd"));
        assertEquals(libraryType.CD, libraryType.fromStringType("Cd"));
        assertEquals(libraryType.Book, libraryType.fromStringType("book"));
        assertEquals(libraryType.Book, libraryType.fromStringType("BoOk"));
        assertThrows(IllegalArgumentException.class, () -> libraryType.fromStringType("magazine"));
        assertThrows(IllegalArgumentException.class, () -> libraryType.fromStringType(""));
    }*/

    @Test
    void values() {
        libraryType[] vals = libraryType.values();
        libraryType[] expected = { libraryType.CD, libraryType.BOOK};
        assertArrayEquals(expected, vals, "libraryType.values() should return enum constants in declaration order");
        assertEquals(2, vals.length, "There should be exactly 2 library types");
    }

    @Test
    void valueOf() {
        assertEquals(libraryType.CD, libraryType.valueOf("CD"));
        assertEquals(libraryType.BOOK, libraryType.valueOf("BOOK"));
        assertThrows(IllegalArgumentException.class, () -> libraryType.valueOf("UNKNOWN"));
    }
}