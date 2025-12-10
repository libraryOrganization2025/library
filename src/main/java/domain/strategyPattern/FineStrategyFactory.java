package domain.strategyPattern;

/**
 * Factory class responsible for selecting the appropriate
 * fine calculation strategy based on the item type.
 *
 *
 * @author Shatha
 * @version 1.0
 */
public class FineStrategyFactory {

    /**
     * Returns the fine calculation strategy for a specific item type.
     *
     * @param itemType the type of item (e.g., "book", "cd")
     * @return the strategy used to calculate fines
     * @throws IllegalArgumentException if the item type is invalid
     */
    public static FineStrategy getStrategy(String itemType) {
        switch (itemType.toLowerCase()) {
            case "book":
                return new BookFineStrategy();
            case "cd":
                return new CDFineStrategy();
            default:
                throw new IllegalArgumentException("Unknown item type: " + itemType);
        }
    }

    /**
     * Calculates the fine for a given item type using the appropriate strategy.
     *
     * @param itemType the type of the borrowed item
     * @param overdueDays number of overdue days
     * @return calculated fine
     * @throws IllegalArgumentException if itemType is invalid
     */
    public int calculateFineForItem(String itemType, int overdueDays) {
        FineStrategy strategy = FineStrategyFactory.getStrategy(itemType);
        return strategy.calculateFine(overdueDays);
    }
}
