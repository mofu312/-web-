package com.library.service;

import com.library.model.User;
import com.library.model.User.Role;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

@Service
public class AuthService {

    private final JdbcTemplate db;

    public AuthService(JdbcTemplate db) {
        this.db = db;
    }

    @PostConstruct
    public void init() {
        db.execute(
            "CREATE TABLE IF NOT EXISTS users (" +
            "username TEXT PRIMARY KEY, password TEXT NOT NULL, role TEXT NOT NULL)"
        );
        int count = db.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        if (count == 0) {
            db.update("INSERT INTO users VALUES (?,?,?)", "admin", "admin123", "ADMIN");
        }
    }

    public String register(String username, String password, Role role) {
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            return "用户名和密码不能为空";
        }
        int count = db.queryForObject(
            "SELECT COUNT(*) FROM users WHERE username=?", Integer.class, username
        );
        if (count > 0) {
            return "用户名已存在";
        }
        db.update("INSERT INTO users VALUES (?,?,?)", username, password, role.name());
        return null;
    }

    public User login(String username, String password) {
        var list = db.query(
            "SELECT * FROM users WHERE username=? AND password=?",
            (rs, rowNum) -> {
                User u = new User();
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                u.setRole(Role.valueOf(rs.getString("role")));
                return u;
            },
            username, password
        );
        return list.isEmpty() ? null : list.get(0);
    }
}
