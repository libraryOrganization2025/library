package domain;

import java.time.LocalDate;

public class Borrow {

    private Integer id;
    private String studentEmail;
    private int isbn;
    private LocalDate borrowDate;
    private LocalDate overdueDate;
    private boolean returned;
    private int fine;

    public Borrow() {}
    public Borrow(String studentEmail, int isbn, LocalDate borrowDate, LocalDate overdueDate, boolean returned) {
        this(null, studentEmail, isbn, borrowDate, overdueDate, returned, 0);
    }


    public Borrow(Integer id, String studentEmail, int isbn, LocalDate borrowDate, LocalDate overdueDate, boolean returned, int fine) {
        this.id = id;
        this.studentEmail = studentEmail;
        this.isbn = isbn;
        this.borrowDate = borrowDate;
        this.overdueDate = overdueDate;
        this.returned = returned;
        this.fine = fine;
    }

    public Integer getId() { return id; }
    public String getStudentEmail() { return studentEmail; }
    public int getIsbn() { return isbn; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getOverdueDate() { return overdueDate; }
    public boolean isReturned() { return returned; }
    public int getFine() { return fine; }

    @Override
    public String toString() {
        return studentEmail + " " +isbn + "   "+borrowDate+"   "+overdueDate+"    "+fine;
    }
}
