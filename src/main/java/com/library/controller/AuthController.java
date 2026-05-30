package com.library.controller;

import com.library.model.Reader;
import com.library.model.User;
import com.library.model.User.Role;
import com.library.service.AuthService;
import com.library.service.LibraryService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final LibraryService libraryService;

    public AuthController(AuthService authService, LibraryService libraryService) {
        this.authService = authService;
        this.libraryService = libraryService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        User user = authService.login(username, password);
        if (user == null) {
            return Map.of("success", false, "message", "用户名或密码错误");
        }
        return Map.of("success", true, "username", user.getUsername(), "role", user.getRole().name());
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String roleStr = body.get("role");
        Role role = "ADMIN".equals(roleStr) ? Role.ADMIN : Role.STUDENT;

        String error = authService.register(username, password, role);
        if (error != null) {
            return Map.of("success", false, "message", error);
        }

        // 学生注册时自动创建读者记录
        if (role == Role.STUDENT) {
            libraryService.addReader(new Reader(username, username, 5));
        }
        return Map.of("success", true, "message", "注册成功");
    }
}
