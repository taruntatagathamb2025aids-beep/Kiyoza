package kiyoza.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Creates JDBC connections. Defaults match a local MySQL with user root and an empty password.
 * To override, create a file named db.properties in the folder you run the app from:
 *   url=jdbc:mysql://localhost:3306/kiyoza?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
 *   user=root
 *   password=your_password
 */
public final class DBConnection {
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties p = new Properties();
        p.setProperty("url", "jdbc:mysql://localhost:3306/kiyoza?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        p.setProperty("user", "root");
        p.setProperty("password", "");
        File file = new File("db.properties");
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                p.load(in);
            } catch (IOException e) {
                System.err.println("Could not read db.properties, using defaults: " + e.getMessage());
            }
        }
        URL = p.getProperty("url");
        USER = p.getProperty("user");
        PASSWORD = p.getProperty("password");
    }

    private DBConnection() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
