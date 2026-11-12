package com.library.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConfig {
    private static final String URL = String.format("jdbc:postgresql://%s:%s/%s",
            setting("LIBRARY_DB_HOST", "localhost"),
            setting("LIBRARY_DB_PORT", "5432"),
            setting("LIBRARY_DB_NAME", "library_management"));
    private static final String USER = setting("LIBRARY_DB_USER", "postgres");
    private static final String PASSWORD = setting("LIBRARY_DB_PASSWORD", "12345");

    private DatabaseConfig() {
    }

    private static String setting(String key, String defaultValue) {
        return System.getenv().getOrDefault(key, defaultValue);
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
