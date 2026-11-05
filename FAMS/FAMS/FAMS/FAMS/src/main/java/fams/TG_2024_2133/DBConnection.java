package fams.TG_2024_2133;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final java.util.Properties PROPS = loadProperties();
    private static final String URL = setting("FAMS_DB_URL", "jdbc:mysql://localhost:3306/fams_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    private static final String USER = setting("FAMS_DB_USER", "root");
    private static final String PASSWORD = setting("FAMS_DB_PASSWORD", "1003");

    /** Reads src/main/resources/database.properties from the classpath (missing file is tolerated). */
    private static java.util.Properties loadProperties() {
        java.util.Properties p = new java.util.Properties();
        try (java.io.InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (in != null) p.load(in);
        } catch (java.io.IOException ignored) {}
        return p;
    }

    /** Priority: environment variable, system property, database.properties, built-in default. */
    private static String setting(String key, String fallback) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) value = System.getProperty(key);
        if (value == null || value.trim().isEmpty()) value = PROPS.getProperty(key);
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
