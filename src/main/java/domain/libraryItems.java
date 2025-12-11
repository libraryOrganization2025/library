package domain;

/**
 * Represents the structure of any library item such as a book or CD.
 * Defines basic getters and setters that all library items must implement.
 *
 * @author Shatha
 * @version 1.0
 */
public interface libraryItems {

    /**
     * return the author of the item
     * @return the author of the item
     */
    String getAuthor();

    /**
     * return the name or title of the item
     * @return the name or title of the item
     */
    String getName();

    /**
     * return the type of the item (Book, CD, etc.)
     * @return the type of the item (Book, CD, etc.)
     */
    libraryType getType();

    /**
     * return the available quantity
     * @return the available quantity
     */
    int getQuantity();

    /**
     * return the item's ISBN
     * @return the item's ISBN
     */
    String getISBN();

    /**
     * Sets the author.
     *
     * @param author the author name
     */
    void setAuthor(String author);

    /**
     * Sets the item name.
     *
     * @param name the title of the item
     */
    void setName(String name);

    /**
     * Sets the item type.
     *
     * @param type the library type
     */
    void setType(libraryType type);

    /**
     * Sets the quantity.
     *
     * @param quantity the number of available copies
     */
    void setQuantity(int quantity);

    /**
     * Sets the ISBN.
     *
     * @param isbn the ISBN value
     */
    void setISBN(String isbn);
}
