package example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {
    private static String dbUrl;
    private static String dbUsername;
    private static String dbPassword;

    static {
        try {
            Properties props = new Properties();
            props.load(DatabaseManager.class.getClassLoader().getResourceAsStream("db.properties"));
            dbUrl = props.getProperty("db.url");
            dbUsername = props.getProperty("db.username");
            dbPassword = props.getProperty("db.password");

            // Load the PostgreSQL driver
            Class.forName(props.getProperty("db.driver"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DatabaseManager", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }
}