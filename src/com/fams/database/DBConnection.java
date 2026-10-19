package com.fams.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = setting("FAMS_DB_URL", "jdbc:mysql://localhost:3306/fams_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    private static final String USER = setting("FAMS_DB_USER", "root");
    private static final String PASSWORD = setting("FAMS_DB_PASSWORD", ""); // Set FAMS_DB_PASSWORD for local MySQL installations

    private static String setting(String key, String fallback) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) value = System.getProperty(key);
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private static boolean useMemoryFallback = false;

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            useMemoryFallback = false;
            return conn;
        } catch (ClassNotFoundException | SQLException e) {
            useMemoryFallback = true;
            return null; // Signals memory fallback mode
        }
    }

    public static boolean isUseMemoryFallback() {
        return useMemoryFallback;
    }

    public static void setUseMemoryFallback(boolean fallback) {
        useMemoryFallback = fallback;
    }
}
