package com.library.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class DatabaseConfig {
    private static final String URL = String.format("jdbc:postgresql://%s:%s/%s",
            setting("LIBRARY_DB_HOST", "localhost"),
            setting("LIBRARY_DB_PORT", "5432"),
            setting("LIBRARY_DB_NAME", "library_management"));
    private static final String USER = setting("LIBRARY_DB_USER", "postgres");

    // Cache mật khẩu đang hoạt động để tránh thử lại nhiều lần
    private static volatile String cachedPassword = null;

    private DatabaseConfig() {
    }

    private static String setting(String key, String defaultValue) {
        return System.getenv().getOrDefault(key, defaultValue);
    }

    public static Connection getConnection() throws SQLException {
        // 1. Thử dùng mật khẩu đã xác thực thành công trước đó
        if (cachedPassword != null) {
            try {
                return DriverManager.getConnection(URL, USER, cachedPassword);
            } catch (SQLException e) {
                if (!"28P01".equals(e.getSQLState())) {
                    throw e;
                }
                cachedPassword = null; // Reset nếu mật khẩu bị thay đổi
            }
        }

        // 2. Danh sách các mật khẩu phổ biến của nhóm (tránh xung đột khi pull giữa các máy)
        List<String> candidates = new ArrayList<>();
        String envPass = System.getenv("LIBRARY_DB_PASSWORD");
        if (envPass != null && !envPass.isBlank()) {
            candidates.add(envPass);
        }
        candidates.add("12345");   // Mật khẩu mặc định máy của Nam
        candidates.add("120906");  // Mật khẩu máy của Phúc
        candidates.add("postgres");
        candidates.add("admin");
        candidates.add("root");

        SQLException lastAuthError = null;
        for (String pass : candidates) {
            try {
                Connection conn = DriverManager.getConnection(URL, USER, pass);
                cachedPassword = pass;
                return conn;
            } catch (SQLException e) {
                // 28P01: invalid_password (password authentication failed)
                if ("28P01".equals(e.getSQLState())) {
                    lastAuthError = e;
                    continue;
                }
                // Các lỗi khác (Port, Host, Database không tồn tại...) thì ném lỗi ngay
                throw e;
            }
        }

        throw lastAuthError != null ? lastAuthError : new SQLException("Không thể kết nối CSDL với các mật khẩu đã thử.");
    }
}
