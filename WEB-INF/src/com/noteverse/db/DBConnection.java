package com.noteverse.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central JDBC connection handler for NoteVerse.
 * Reads credentials from db.properties so nothing is hardcoded in the code.
 */
public class DBConnection {

    private static final String CONFIG_FILE = "db.properties";
    private static Properties props = null;

    // Loads db.properties once and caches it.
    // Uses the classloader (not a raw file path) so this works both when run
    // standalone from VS Code AND when deployed inside Tomcat, where the
    // "current directory" isn't your project folder anymore.
    private static Properties loadProperties() {
        if (props != null) {
            return props;
        }
        props = new Properties();
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException(
                    "Could not find " + CONFIG_FILE + " on the classpath. " +
                    "Make sure it's sitting directly in WEB-INF/classes (next to the com folder).");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + CONFIG_FILE, e);
        }
        return props;
    }

    /**
     * Opens a fresh JDBC connection to the MySQL database.
     * Caller is responsible for closing it (use try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        Properties p = loadProperties();
        String url = p.getProperty("db.url");
        String user = p.getProperty("db.user");
        String password = p.getProperty("db.password");

        if (url == null || user == null || password == null) {
            throw new RuntimeException(
                CONFIG_FILE + " must define db.url, db.user, and db.password.");
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found. Add mysql-connector-j jar to your classpath.", e);
        }

        return DriverManager.getConnection(url, user, password);
    }

    // Quick manual test: run this file directly to check your connection works
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Connected to NoteVerse database successfully!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Connection failed:");
            e.printStackTrace();
        }
    }
}