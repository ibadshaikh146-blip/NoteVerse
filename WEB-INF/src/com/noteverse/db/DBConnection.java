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
 *   1. A full JDBC_URL + DB_USER + DB_PASSWORD — the most portable option,
 *      works with any provider (Aiven, Render, PlanetScale, etc). Set
 *      JDBC_URL to something like:
 *      jdbc:mysql://host:port/dbname?sslMode=REQUIRED&serverTimezone=UTC
 *   2. Railway-style discrete environment variables (MYSQLHOST, MYSQLPORT,
 *      MYSQLDATABASE, MYSQLUSER, MYSQLPASSWORD) — kept for backward
 *      compatibility with a Railway deployment.
 *   3. Local db.properties file (for development in VS Code / local Tomcat).
 *
 * This means the exact same code runs locally and on any cloud provider
 * with zero code changes — only environment variables differ.
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

        // 1. Full JDBC URL — most portable, works with any provider
        String jdbcUrl = System.getenv("JDBC_URL");
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");

        if (jdbcUrl != null && dbUser != null && dbPassword != null) {
            url = jdbcUrl;
            user = dbUser;
            password = dbPassword;
        } else {
            // 2. Railway-style discrete environment variables
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
                // 3. Fall back to local db.properties
                Properties p = loadProperties();
                url = p.getProperty("db.url");
                user = p.getProperty("db.username", p.getProperty("db.user"));
                password = p.getProperty("db.password");

                if (url == null || user == null || password == null) {
                    throw new RuntimeException(
                        "No database config found. Set JDBC_URL/DB_USER/DB_PASSWORD, " +
                        "or MYSQLHOST/MYSQLUSER/MYSQLPASSWORD/MYSQLDATABASE, " +
                        "or provide " + CONFIG_FILE + " locally.");
                }
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
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Connected to NoteVerse database successfully!");
            }
        } catch (SQLException e) {
            System.out.println("Connection failed:");
            e.printStackTrace();
        }
    }
}