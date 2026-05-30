package com.library.service;

import com.library.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;

@Service
public class LibraryService {

    private final JdbcTemplate db;

    public LibraryService(JdbcTemplate db) {
        this.db = db;
    }

    @PostConstruct
    public void init() {
        db.execute(
            "CREATE TABLE IF NOT EXISTS books (" +
            "isbn TEXT PRIMARY KEY, title TEXT NOT NULL, author TEXT NOT NULL, " +
            "totalCopies INTEGER, availableCopies INTEGER)"
        );
        db.execute(
            "CREATE TABLE IF NOT EXISTS readers (" +
            "id TEXT PRIMARY KEY, name TEXT NOT NULL, " +
            "borrowedCount INTEGER, maxBorrow INTEGER)"
        );
        db.execute(
            "CREATE TABLE IF NOT EXISTS borrow_records (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, bookIsbn TEXT, readerId TEXT, " +
            "borrowDate TEXT, dueDate TEXT, returnDate TEXT, returned INTEGER)"
        );

    }

    // ===== 图书管理 =====

    public List<Book> listBooks() {
        return db.query("SELECT * FROM books", (rs, n) -> {
            Book b = new Book();
            b.setIsbn(rs.getString("isbn"));
            b.setTitle(rs.getString("title"));
            b.setAuthor(rs.getString("author"));
            b.setTotalCopies(rs.getInt("totalCopies"));
            b.setAvailableCopies(rs.getInt("availableCopies"));
            return b;
        });
    }

    public Book findBook(String isbn) {
        var list = db.query("SELECT * FROM books WHERE isbn=?", (rs, n) -> {
            Book b = new Book();
            b.setIsbn(rs.getString("isbn"));
            b.setTitle(rs.getString("title"));
            b.setAuthor(rs.getString("author"));
            b.setTotalCopies(rs.getInt("totalCopies"));
            b.setAvailableCopies(rs.getInt("availableCopies"));
            return b;
        }, isbn);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Book> searchBooks(String keyword) {
        String kw = "%" + keyword + "%";
        return db.query(
            "SELECT * FROM books WHERE title LIKE ? OR author LIKE ?",
            (rs, n) -> {
                Book b = new Book();
                b.setIsbn(rs.getString("isbn"));
                b.setTitle(rs.getString("title"));
                b.setAuthor(rs.getString("author"));
                b.setTotalCopies(rs.getInt("totalCopies"));
                b.setAvailableCopies(rs.getInt("availableCopies"));
                return b;
            },
            kw, kw
        );
    }

    public void addBook(Book book) {
        db.update(
            "INSERT INTO books VALUES (?,?,?,?,?)",
            book.getIsbn(), book.getTitle(), book.getAuthor(),
            book.getTotalCopies(), book.getTotalCopies()
        );
    }

    public boolean deleteBook(String isbn) {
        return db.update("DELETE FROM books WHERE isbn=?", isbn) > 0;
    }

    public String updateBook(String isbn, String newTitle, String newAuthor, int newTotal) {
        Book b = findBook(isbn);
        if (b == null) {
            return "没有这本书";
        }
        int borrowed = b.getTotalCopies() - b.getAvailableCopies();
        if (newTotal < borrowed) {
            newTotal = borrowed;
        }
        db.update(
            "UPDATE books SET title=?, author=?, totalCopies=?, availableCopies=? WHERE isbn=?",
            newTitle, newAuthor, newTotal, newTotal - borrowed, isbn
        );
        return "修改成功";
    }

    // ===== 读者管理 =====

    public List<Reader> listReaders() {
        return db.query("SELECT * FROM readers", (rs, n) -> {
            Reader r = new Reader();
            r.setName(rs.getString("name"));
            r.setId(rs.getString("id"));
            r.setBorrowedCount(rs.getInt("borrowedCount"));
            r.setMaxBorrow(rs.getInt("maxBorrow"));
            return r;
        });
    }

    public Reader findReader(String id) {
        var list = db.query("SELECT * FROM readers WHERE id=?", (rs, n) -> {
            Reader r = new Reader();
            r.setName(rs.getString("name"));
            r.setId(rs.getString("id"));
            r.setBorrowedCount(rs.getInt("borrowedCount"));
            r.setMaxBorrow(rs.getInt("maxBorrow"));
            return r;
        }, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public void addReader(Reader reader) {
        db.update(
            "INSERT INTO readers VALUES (?,?,?,?)",
            reader.getId(), reader.getName(),
            reader.getBorrowedCount(), reader.getMaxBorrow()
        );
    }

    public boolean deleteReader(String id) {
        return db.update("DELETE FROM readers WHERE id=?", id) > 0;
    }

    // ===== 借阅归还 =====

    @Transactional
    public String borrowBook(String isbn, String readerId, int days) {
        Book book = findBook(isbn);
        if (book == null) {
            return "没有这本书";
        }
        if (!book.isAvailable()) {
            return "这本书已全部借出";
        }
        Reader reader = findReader(readerId);
        if (reader == null) {
            return "没有这个读者";
        }
        if (!reader.canBorrow()) {
            return "该读者已达到借书上限（" + reader.getMaxBorrow() + "本）";
        }

        LocalDate now = LocalDate.now();
        LocalDate due = now.plusDays(days);

        db.update("UPDATE books SET availableCopies=availableCopies-1 WHERE isbn=?", isbn);
        db.update("UPDATE readers SET borrowedCount=borrowedCount+1 WHERE id=?", readerId);
        db.update(
            "INSERT INTO borrow_records (bookIsbn,readerId,borrowDate,dueDate,returned) VALUES (?,?,?,?,0)",
            isbn, readerId, now.toString(), due.toString()
        );

        return "借阅成功：《" + book.getTitle() + "》应还日期：" + due;
    }

    @Transactional
    public String returnBook(String isbn, String readerId) {
        var list = db.query(
            "SELECT * FROM borrow_records " +
            "WHERE bookIsbn=? AND readerId=? AND returned=0 ORDER BY id DESC LIMIT 1",
            (rs, n) -> {
                BorrowRecord r = new BorrowRecord();
                r.setId(rs.getLong("id"));
                r.setBookIsbn(rs.getString("bookIsbn"));
                r.setReaderId(rs.getString("readerId"));
                r.setBorrowDate(LocalDate.parse(rs.getString("borrowDate")));
                r.setDueDate(LocalDate.parse(rs.getString("dueDate")));
                r.setReturned(rs.getInt("returned") == 1);
                return r;
            },
            isbn, readerId
        );

        if (list.isEmpty()) {
            return "没有找到这条未归还的借阅记录";
        }
        BorrowRecord target = list.get(0);

        db.update("UPDATE books SET availableCopies=availableCopies+1 WHERE isbn=?", isbn);
        db.update("UPDATE readers SET borrowedCount=borrowedCount-1 WHERE id=?", readerId);
        db.update(
            "UPDATE borrow_records SET returned=1, returnDate=? WHERE id=?",
            LocalDate.now().toString(), target.getId()
        );

        String result = "归还成功";
        long overdueDays = LocalDate.now().toEpochDay() - target.getDueDate().toEpochDay();
        if (overdueDays > 0) {
            result += "（逾期" + overdueDays + "天）";
        }
        return result;
    }

    public List<BorrowRecord> listRecords() {
        return db.query("SELECT * FROM borrow_records ORDER BY id DESC", (rs, n) -> {
            BorrowRecord r = new BorrowRecord();
            r.setId(rs.getLong("id"));
            r.setBookIsbn(rs.getString("bookIsbn"));
            r.setReaderId(rs.getString("readerId"));
            r.setBorrowDate(LocalDate.parse(rs.getString("borrowDate")));
            r.setDueDate(LocalDate.parse(rs.getString("dueDate")));
            String rd = rs.getString("returnDate");
            r.setReturnDate(rd == null ? null : LocalDate.parse(rd));
            r.setReturned(rs.getInt("returned") == 1);
            return r;
        });
    }

    public List<BorrowRecord> getOverdueRecords() {
        String today = LocalDate.now().toString();
        return db.query(
            "SELECT * FROM borrow_records WHERE returned=0 AND dueDate < ?",
            (rs, n) -> {
                BorrowRecord r = new BorrowRecord();
                r.setId(rs.getLong("id"));
                r.setBookIsbn(rs.getString("bookIsbn"));
                r.setReaderId(rs.getString("readerId"));
                r.setBorrowDate(LocalDate.parse(rs.getString("borrowDate")));
                r.setDueDate(LocalDate.parse(rs.getString("dueDate")));
                r.setReturned(false);
                return r;
            },
            today
        );
    }

    public List<BorrowRecord> myRecords(String readerId) {
        return db.query(
            "SELECT * FROM borrow_records WHERE readerId=? ORDER BY id DESC",
            (rs, n) -> {
                BorrowRecord r = new BorrowRecord();
                r.setId(rs.getLong("id"));
                r.setBookIsbn(rs.getString("bookIsbn"));
                r.setReaderId(rs.getString("readerId"));
                r.setBorrowDate(LocalDate.parse(rs.getString("borrowDate")));
                r.setDueDate(LocalDate.parse(rs.getString("dueDate")));
                String rd = rs.getString("returnDate");
                r.setReturnDate(rd == null ? null : LocalDate.parse(rd));
                r.setReturned(rs.getInt("returned") == 1);
                return r;
            },
            readerId
        );
    }
}
