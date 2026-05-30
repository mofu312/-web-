package com.library.controller;

import com.library.model.Reader;
import com.library.service.LibraryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/readers")
public class ReaderController {

    private final LibraryService lib;

    public ReaderController(LibraryService lib) {
        this.lib = lib;
    }

    @GetMapping
    public List<Reader> list() {
        return lib.listReaders();
    }

    @GetMapping("/{id}")
    public Reader get(@PathVariable String id) {
        return lib.findReader(id);
    }

    @PostMapping
    public Map<String, Object> add(@RequestBody Reader reader) {
        lib.addReader(reader);
        return Map.of("success", true, "message", "添加成功");
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable String id) {
        boolean ok = lib.deleteReader(id);
        return Map.of("success", ok, "message", ok ? "删除成功" : "没找到这个读者");
    }
}
