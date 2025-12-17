package service;

import domain.Items;
import domain.libraryType;
import repository.ItemsRepo;
import repository.ItemsRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class responsible for managing library items such as books and CDs.
 * <p>
 * Provides functionality to add new items, increase quantity, and search for items
 * by name, author, or ISBN. Interacts with {@link ItemsRepository} for database operations.
 * </p>
 *
 * @author Shatha ,Sara
 * @version 1.0
 */
public class ItemsService {

    private final ItemsRepo itemsRepository;

    /**
     * Constructs an {@link ItemsService} with the specified repository.
     *
     * @param itemsRepository the repository used for item operations
     */
    public ItemsService(ItemsRepo itemsRepository) {
        this.itemsRepository = itemsRepository;
    }

    /**
     * Adds a new item to the library.
     *
     * @param name the name of the item
     * @param author the author of the item
     * @param quantity the quantity of the item (must be greater than 0)
     * @param type the type of the item ({@link libraryType})
     * @return {@code true} if the item was successfully added, {@code false} otherwise
     * @throws IllegalArgumentException if name or author is empty, quantity is non-positive, or type is null
     */
    public boolean addNewItem(String name, String author, int quantity, libraryType type) {

        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Item name cannot be empty.");
        if (author == null || author.isBlank())
            throw new IllegalArgumentException("Author cannot be empty.");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be greater than 0.");
        if (type == null)
            throw new IllegalArgumentException("Type (BOOK / CD) is required.");

        Items item = new Items(author, name, type, quantity, "");
        return itemsRepository.addItem(item);
    }

    /**
     * Increases the quantity of an existing item by its ISBN.
     *
     * @param isbnInput the ISBN of the item as a string
     * @return {@code true} if the quantity was successfully increased, {@code false} otherwise
     * @throws IllegalArgumentException if ISBN is empty, not numeric, or no item exists with this ISBN
     */
    public boolean increaseQuantityByISBN(String isbnInput) {
        if (isbnInput == null || isbnInput.isBlank())
            throw new IllegalArgumentException("ISBN cannot be empty.");

        int isbn;
        try {
            isbn = Integer.parseInt(isbnInput);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ISBN must be a number.");
        }

        var existing = itemsRepository.findByISBN(isbn);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("No item found with this ISBN.");
        }

        return itemsRepository.increaseQuantity(isbn);
    }

    /**
     * Searches items by name and optionally filters by type.
     *
     * @param name the name to search
     * @param type the optional type filter ({@link libraryType}), null to include all types
     * @return a list of matching {@link Items}
     */
    public List<Items> searchByName(String name, libraryType type) {
        List<Items> result = itemsRepository.findByName(name);
        if (type == null) return result;
        return result.stream()
                .filter(i -> i.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Searches items by author and optionally filters by type.
     *
     * @param author the author to search
     * @param type the optional type filter ({@link libraryType}), null to include all types
     * @return a list of matching {@link Items}
     */
    public List<Items> searchByAuthor(String author, libraryType type) {
        List<Items> result = itemsRepository.findByAuthor(author);
        if (type == null) return result;
        return result.stream()
                .filter(i -> i.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Searches an item by ISBN.
     *
     * @param isbnInput the ISBN of the item as a string
     * @return the {@link Items} object if found
     * @throws IllegalArgumentException if ISBN is empty, not numeric, or no item exists with this ISBN
     */
    public Items searchByISBN(String isbnInput) {
        if (isbnInput == null || isbnInput.isBlank())
            throw new IllegalArgumentException("ISBN cannot be empty.");

        int isbn;
        try {
            isbn = Integer.parseInt(isbnInput);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ISBN must be a number.");
        }

        return itemsRepository.findByISBN(isbn)
                .orElseThrow(() -> new IllegalArgumentException("No item found with this ISBN."));
    }

    /**
     * Searches for books by name.
     *
     * @param name the name of the book
     * @return a list of matching {@link Items} of type {@link libraryType#BOOK}
     */
    public List<Items> searchBooksByName(String name) {
        return searchByName(name, libraryType.BOOK);
    }

    /**
     * Searches for CDs by name.
     *
     * @param name the name of the CD
     * @return a list of matching {@link Items} of type {@link libraryType#CD}
     */
    public List<Items> searchCDsByName(String name) {
        return searchByName(name, libraryType.CD);
    }

    /**
     * Searches for books by author.
     *
     * @param author the author of the book
     * @return a list of matching {@link Items} of type {@link libraryType#BOOK}
     */
    public List<Items> searchBooksByAuthor(String author) {
        return searchByAuthor(author, libraryType.BOOK);
    }

    /**
     * Searches for CDs by author.
     *
     * @param author the author of the CD
     * @return a list of matching {@link Items} of type {@link libraryType#CD}
     */
    public List<Items> searchCDsByAuthor(String author) {
        return searchByAuthor(author, libraryType.CD);
    }
}
