package repository;

import domain.Items;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for item-related database operations.
 */
public interface ItemsRepo {

    boolean addItem(Items item);

    List<Items> findByName(String name);

    List<Items> findByAuthor(String author);

    Optional<Items> findByISBN(int isbn);

    boolean increaseQuantity(int isbn);
}
