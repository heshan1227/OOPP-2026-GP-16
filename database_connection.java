package com.fams.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/fams_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Default XAMPP / MySQL root password

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
