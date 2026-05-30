package com.library.model;

public class Reader {
    private String name;
    private String id;
    private int borrowedCount;
    private int maxBorrow;

    public Reader() {
    }

    public Reader(String name, String id, int maxBorrow) {
        this.name = name;
        this.id = id;
        this.maxBorrow = maxBorrow;
        this.borrowedCount = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getBorrowedCount() {
        return borrowedCount;
    }

    public void setBorrowedCount(int borrowedCount) {
        this.borrowedCount = borrowedCount;
    }

    public int getMaxBorrow() {
        return maxBorrow;
    }

    public void setMaxBorrow(int maxBorrow) {
        this.maxBorrow = maxBorrow;
    }

    public boolean canBorrow() {
        return borrowedCount < maxBorrow;
    }

    public void increaseBorrowed() {
        borrowedCount++;
    }

    public void decreaseBorrowed() {
        if (borrowedCount > 0) {
            borrowedCount--;
        }
    }
}
