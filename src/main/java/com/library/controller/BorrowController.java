package com.library.controller;

import com.library.model.BorrowRecord;
import com.library.service.LibraryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BorrowController {

    private final LibraryService lib;

    public BorrowController(LibraryService lib) {
        this.lib = lib;
    }

    @PostMapping("/borrow")
    public Map<String, Object> borrow(@RequestBody Map<String, Object> body) {
        String isbn = (String) body.get("isbn");
        String readerId = (String) body.get("readerId");
        int days = Integer.parseInt(body.get("days").toString());
        String msg = lib.borrowBook(isbn, readerId, days);
        return Map.of("success", msg.startsWith("借阅"), "message", msg);
    }

    @PostMapping("/return")
    public Map<String, Object> returnBook(@RequestBody Map<String, String> body) {
        String isbn = body.get("isbn");
        String readerId = body.get("readerId");
        String msg = lib.returnBook(isbn, readerId);
        return Map.of("success", msg.startsWith("归还"), "message", msg);
    }

    @GetMapping("/records")
    public List<BorrowRecord> records() {
        return lib.listRecords();
    }

    @GetMapping("/records/overdue")
    public List<BorrowRecord> overdue() {
        return lib.getOverdueRecords();
    }

    @GetMapping("/records/my/{readerId}")
    public List<BorrowRecord> myRecords(@PathVariable String readerId) {
        return lib.myRecords(readerId);
    }
}
