package domain.strategyPattern;

/**
 * Fine calculation strategy for CDs.
 * CDs have a fine rate of 20 units per overdue day.
 *
 * <p>Example: 3 overdue days → 3 * 20 = 60
 *
 * @author Shatha
 * @version 1.0
 */
public class CDFineStrategy implements FineStrategy {

    /**
     * Calculates the fine for overdue CDs.
     *
     * @param overdueDays number of overdue days
     * @return the fine amount (overdueDays * 20)
     */
    @Override
    public int calculateFine(int overdueDays) {
        return overdueDays * 20;
    }
}
