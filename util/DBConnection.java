package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = setting("FAMS_DB_URL", "jdbc:mysql://localhost:3306/fams_db?useSSL=false&serverTimezone=UTC");
    private static final String USER = setting("FAMS_DB_USER", "root");
    private static final String PASSWORD = setting("FAMS_DB_PASSWORD", "1234");

    private static String setting(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
