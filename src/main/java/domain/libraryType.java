package domain;

/**
 * Represents the type of items available in the library.
 * Each type has an ID, an overdue period, and a price.
 *
 * <p>Example types include:
 * <ul>
 *   <li>Book</li>
 *   <li>CD</li>
 * </ul>
 *
 * @author Shatha
 * @version 1.0
 */
public enum libraryType {

    /** Compact Disc item: 1 type ID, 7-day overdue limit, 20-unit price. */
    CD(1, 7, 20),

    /** Book item: 2 type ID, 28-day overdue limit, 10-unit price. */
    Book(2, 28, 10);

    /** Unique type ID. */
    private int id;

    /** Number of days before the item becomes overdue. */
    private int overdue;

    /** Base price of the item. */
    private int price;

    /**
     * Creates a library type with the given attributes.
     *
     * @param id unique type ID
     * @param overdue number of days before overdue
     * @param price base item price
     */
    libraryType(int id, int overdue, int price) {
        this.id = id;
        this.overdue = overdue;
        this.price = price;
    }

    /**
     * return the unique type ID
     * @return the unique type ID
     */
    public int getType() {
        return id;
    }

    /**
     * Converts a numeric type ID into a {@link libraryType} value.
     *
     * @param type the type ID
     * @return the matching library type
     * @throws IllegalArgumentException if type ID is invalid
     */
    public static libraryType chooseType(int type) {
        for (libraryType ty : libraryType.values()) {
            if (ty.getType() == type) return ty;
        }
        throw new IllegalArgumentException("This item: (" + type + ") isn't valid :(");
    }

    /**
     * Converts a string into a library type.
     * Accepts case-insensitive values.
     *
     * @param str the string representing the type
     * @return the matching library type
     */
    public static libraryType fromStringType(String str) {
        return libraryType.valueOf(str.toUpperCase());
    }
}
