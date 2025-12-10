package domain.strategyPattern;

/**
 * Fine calculation strategy for books.
 * Books have a fixed fine rate of 10 units per overdue day.
 *
 *
 * @author Shatha
 * @version 1.0
 */
public class BookFineStrategy implements FineStrategy {

    /**
     * Calculates the fine for overdue books.
     *
     * @param overdueDays number of overdue days
     * @return the fine amount (overdueDays * 10)
     */
    @Override
    public int calculateFine(int overdueDays) {
        return overdueDays * 10;
    }
}
