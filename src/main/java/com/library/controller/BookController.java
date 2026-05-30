package com.library.controller;

import com.library.model.Book;
import com.library.service.LibraryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final LibraryService lib;

    public BookController(LibraryService lib) {
        this.lib = lib;
    }

    @GetMapping
    public List<Book> list() {
        return lib.listBooks();
    }

    @GetMapping("/{isbn}")
    public Book get(@PathVariable String isbn) {
        return lib.findBook(isbn);
    }

    @GetMapping("/search")
    public List<Book> search(@RequestParam String keyword) {
        return lib.searchBooks(keyword);
    }

    @PostMapping
    public Map<String, Object> add(@RequestBody Book book) {
        lib.addBook(book);
        return Map.of("success", true, "message", "添加成功");
    }

    @PutMapping("/{isbn}")
    public Map<String, Object> update(@PathVariable String isbn, @RequestBody Book book) {
        String msg = lib.updateBook(isbn, book.getTitle(), book.getAuthor(), book.getTotalCopies());
        return Map.of("success", true, "message", msg);
    }

    @DeleteMapping("/{isbn}")
    public Map<String, Object> delete(@PathVariable String isbn) {
        boolean ok = lib.deleteBook(isbn);
        return Map.of("success", ok, "message", ok ? "删除成功" : "没找到这本书");
    }
}
