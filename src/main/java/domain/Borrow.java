package domain;

import java.time.LocalDate;

/**
 * Represents a borrowing record in the library system.
 * Stores information about which student borrowed which item,
 * the borrow date, overdue date, whether the item was returned,
 * and any fine associated with the borrowing.
 *
 * @author Shatha , Sara
 * @version 1.0
 */
public class Borrow {

    /** Unique ID of the borrow record (nullable before saving). */
    private Integer id;

    /** Email of the student who borrowed the item. */
    private String studentEmail;

    /** ISBN of the borrowed item. */
    private int isbn;

    /** The date on which the book was borrowed. */
    private LocalDate borrowDate;

    /** The date on which the book becomes overdue. */
    private LocalDate overdueDate;

    /** Indicates whether the book has been returned. */
    private boolean returned;

    /** The fine amount associated with this borrowing record. */
    private int fine;

    /**
     * Default no-argument constructor.
     */
    public Borrow() {}

    /**
     * Creates a borrow record without specifying an ID or fine.
     *
     * @param studentEmail the email of the student
     * @param isbn the ISBN of the borrowed item
     * @param borrowDate the date the item was borrowed
     * @param overdueDate the date the item becomes overdue
     * @param returned whether the item has been returned
     */
    public Borrow(String studentEmail, int isbn, LocalDate borrowDate,
                  LocalDate overdueDate, boolean returned) {
        this(null, studentEmail, isbn, borrowDate, overdueDate, returned, 0);
    }

    /**
     * Creates a complete borrow record.
     *
     * @param id the unique record ID (maybe null before saving)
     * @param studentEmail the email of the student
     * @param isbn the ISBN of the borrowed item
     * @param borrowDate the borrow date
     * @param overdueDate the overdue date
     * @param returned whether the book has been returned
     * @param fine the fine amount
     */
    public Borrow(Integer id, String studentEmail, int isbn, LocalDate borrowDate,
                  LocalDate overdueDate, boolean returned, int fine) {
        this.id = id;
        this.studentEmail = studentEmail;
        this.isbn = isbn;
        this.borrowDate = borrowDate;
        this.overdueDate = overdueDate;
        this.returned = returned;
        this.fine = fine;
    }

    /**
     * @return the unique borrow record ID
     */
    public Integer getId() { return id; }

    /**
     * @return the email of the student
     */
    public String getStudentEmail() { return studentEmail; }

    /**
     * @return the ISBN of the borrowed item
     */
    public int getIsbn() { return isbn; }

    /**
     * @return the borrow date
     */
    public LocalDate getBorrowDate() { return borrowDate; }

    /**
     * @return the overdue date
     */
    public LocalDate getOverdueDate() { return overdueDate; }

    /**
     * @return true if the item has been returned, false otherwise
     */
    public boolean isReturned() { return returned; }

    /**
     * @return the fine amount
     */
    public int getFine() { return fine; }

    /**
     * Returns a formatted string representation of the borrowing record.
     *
     * @return a textual summary of the borrow details
     */
    @Override
    public String toString() {
        return studentEmail + " " + isbn + "   " + borrowDate + "   " + overdueDate + "    " + fine;
    }
}
