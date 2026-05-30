package com.library.model;

import java.time.LocalDate;

public class BorrowRecord {
    private Long id;
    private String bookIsbn;
    private String readerId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private boolean returned;

    public BorrowRecord() {
    }

    public BorrowRecord(String bookIsbn, String readerId, LocalDate borrowDate, int borrowDays) {
        this.bookIsbn = bookIsbn;
        this.readerId = readerId;
        this.borrowDate = borrowDate;
        this.dueDate = borrowDate.plusDays(borrowDays);
        this.returned = false;
        this.returnDate = null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookIsbn() {
        return bookIsbn;
    }

    public void setBookIsbn(String bookIsbn) {
        this.bookIsbn = bookIsbn;
    }

    public String getReaderId() {
        return readerId;
    }

    public void setReaderId(String readerId) {
        this.readerId = readerId;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public boolean isReturned() {
        return returned;
    }

    public void setReturned(boolean returned) {
        this.returned = returned;
    }

    public void markReturned() {
        this.returned = true;
        this.returnDate = LocalDate.now();
    }

    public boolean getOverdue() {
        return !returned && LocalDate.now().isAfter(dueDate);
    }

    public long getOverdueDays() {
        if (!returned && LocalDate.now().isAfter(dueDate)) {
            return LocalDate.now().toEpochDay() - dueDate.toEpochDay();
        }
        return 0;
    }
}
