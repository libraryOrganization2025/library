package domain.strategyPattern;

/**
 * Strategy interface for calculating fines based on item type.
 * Different items (Book, CD, etc.) may apply different fine rules.
 *
 * <p>This interface is implemented by specific fine calculation strategies.
 *
 * @author Shatha
 * @version 1.0
 */
public interface FineStrategy {

    /**
     * Calculates the fine amount based on the number of overdue days.
     *
     * @param overdueDays number of days the item is overdue
     * @return calculated fine amount
     */
    int calculateFine(int overdueDays);
}
