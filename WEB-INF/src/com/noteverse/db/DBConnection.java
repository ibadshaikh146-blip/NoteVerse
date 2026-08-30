package com.noteverse.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central JDBC connection handler for NoteVerse.
 *
 * Connection details are resolved in this order:
 *   1. Railway/production environment variables (MYSQLHOST, MYSQLPORT,
 *      MYSQLDATABASE, MYSQLUSER, MYSQLPASSWORD) — set automatically when
 *      you attach a MySQL plugin on Railway.
 *   2. Local db.properties file (for development in VS Code / local Tomcat).
 *
 * This means the exact same code runs locally and on Railway with zero
 * changes — nothing is hardcoded.
 */
public class DBConnection {

    private static final String CONFIG_FILE = "db.properties";
    private static Properties props = null;

    private static Properties loadProperties() {
        if (props != null) {
            return props;
        }
        props = new Properties();
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                // Not fatal here — we might be running on Railway where
                // env vars are used instead of a properties file.
                return props;
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
        String url;
        String user;
        String password;

        // 1. Try Railway-style environment variables first
        String envHost = System.getenv("MYSQLHOST");
        String envPort = System.getenv("MYSQLPORT");
        String envDb   = System.getenv("MYSQLDATABASE");
        String envUser = System.getenv("MYSQLUSER");
        String envPass = System.getenv("MYSQLPASSWORD");

        if (envHost != null && envUser != null && envPass != null && envDb != null) {
            String port = (envPort != null) ? envPort : "3306";
            url = "jdbc:mysql://" + envHost + ":" + port + "/" + envDb
                    + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            user = envUser;
            password = envPass;
        } else {
            // 2. Fall back to local db.properties
            Properties p = loadProperties();
            url = p.getProperty("db.url");
            user = p.getProperty("db.username", p.getProperty("db.user"));
            password = p.getProperty("db.password");

            if (url == null || user == null || password == null) {
                throw new RuntimeException(
                    "No database config found. Either set MYSQLHOST/MYSQLUSER/MYSQLPASSWORD/MYSQLDATABASE " +
                    "environment variables, or provide " + CONFIG_FILE +
                    " with db.url, db.username, and db.password.");
            }
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
                System.out.println("Connected to NoteVerse database successfully!");
            }
        } catch (SQLException e) {
            System.out.println("Connection failed:");
            e.printStackTrace();
        }
    }
}