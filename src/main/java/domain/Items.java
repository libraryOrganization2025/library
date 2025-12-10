package domain;

/**
 * Represents an item in the library, such as a book or CD.
 * Each item contains information about its author, name, type,
 * quantity available, and ISBN. This class implements the
 * {@link libraryItems} interface.
 *
 * @author Shatha
 * @version 1.0
 */
public class Items implements libraryItems {

    /** The author or creator of the library item. */
    private String author;

    /** The name or title of the item. */
    private String name;

    /** The type of the item (e.g., Book, CD). */
    private libraryType type;

    /** The quantity of this item available in the library. */
    private int quantity;

    /** The ISBN identifier for the item. */
    private String isbn;

    /**
     * Creates a new library item with the specified details.
     *
     * @param author the item's author
     * @param name the item's name/title
     * @param type the type of the library item
     * @param quantity how many copies exist
     * @param isbn the ISBN of the item
     */
    public Items(String author, String name, libraryType type, int quantity, String isbn) {
        this.author = author;
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.isbn = isbn;
    }

    /** {@inheritDoc} */
    @Override
    public String getAuthor() {
        return author;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public libraryType getType() {
        return type;
    }

    /** {@inheritDoc} */
    @Override
    public int getQuantity() {
        return quantity;
    }

    /** {@inheritDoc} */
    @Override
    public String getISBN() {
        return isbn;
    }

    /** {@inheritDoc} */
    @Override
    public void setAuthor(String author) {
        this.author = author;
    }

    /** {@inheritDoc} */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public void setType(libraryType type) {
        this.type = type;
    }

    /** {@inheritDoc} */
    @Override
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /** {@inheritDoc} */
    @Override
    public void setISBN(String isbn) {
        this.isbn = isbn;
    }
}
