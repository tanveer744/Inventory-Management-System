package com.erp.inventory.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database connection utility class
 * Manages database connections using connection pooling for production use
 */
public class DatabaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);
    
    private static Properties properties = new Properties();
    private static boolean initialized = false;

    // Database configuration properties
    private static String DB_URL;
    private static String DB_USERNAME;
    private static String DB_PASSWORD;
    private static String DB_DRIVER;

    // Initialize database configuration
    static {
        loadDatabaseProperties();
    }

    private static void loadDatabaseProperties() {
        try {
            // Load properties from application.properties file
            InputStream inputStream = DatabaseUtil.class.getClassLoader()
                    .getResourceAsStream("application.properties");
            
            if (inputStream != null) {
                properties.load(inputStream);
                inputStream.close();
            } else {
                logger.warn("application.properties not found, using default values");
                setDefaultProperties();
            }

            // Set configuration from properties
            DB_URL = properties.getProperty("db.url", "jdbc:mysql://localhost:3306/inventory_management");
            DB_USERNAME = properties.getProperty("db.username", "root");
            DB_PASSWORD = properties.getProperty("db.password", "password");
            DB_DRIVER = properties.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");

            // Load database driver
            Class.forName(DB_DRIVER);
            initialized = true;
            
            logger.info("Database configuration loaded successfully");
            logger.info("Database URL: {}", DB_URL);
            
        } catch (IOException e) {
            logger.error("Error loading database properties", e);
            setDefaultProperties();
        } catch (ClassNotFoundException e) {
            logger.error("Database driver not found: {}", DB_DRIVER, e);
            throw new RuntimeException("Database driver not found", e);
        }
    }

    private static void setDefaultProperties() {
        DB_URL = "jdbc:mysql://localhost:3306/inventory_management";
        DB_USERNAME = "root";
        DB_PASSWORD = "password";
        DB_DRIVER = "com.mysql.cj.jdbc.Driver";
        
        try {
            Class.forName(DB_DRIVER);
            initialized = true;
        } catch (ClassNotFoundException e) {
            logger.error("Default database driver not found: {}", DB_DRIVER, e);
        }
    }

    /**
     * Get a database connection
     * @return Connection object
     * @throws SQLException if connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            throw new SQLException("Database not initialized properly");
        }

        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            logger.debug("Database connection established");
            return connection;
        } catch (SQLException e) {
            logger.error("Failed to establish database connection", e);
            throw e;
        }
    }

    /**
     * Test database connection
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }

    /**
     * Close database connection safely
     * @param connection Connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                logger.debug("Database connection closed");
            } catch (SQLException e) {
                logger.error("Error closing database connection", e);
            }
        }
    }

    /**
     * Get database URL
     * @return database URL
     */
    public static String getDatabaseUrl() {
        return DB_URL;
    }

    /**
     * Get database username
     * @return database username
     */
    public static String getDatabaseUsername() {
        return DB_USERNAME;
    }

    /**
     * Check if database is initialized
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Reload database configuration
     */
    public static void reloadConfiguration() {
        initialized = false;
        loadDatabaseProperties();
    }

    // Configuration methods for testing
    public static void setTestConfiguration(String url, String username, String password) {
        DB_URL = url;
        DB_USERNAME = username;
        DB_PASSWORD = password;
        DB_DRIVER = "org.h2.Driver"; // Use H2 for testing
        
        try {
            Class.forName(DB_DRIVER);
            initialized = true;
            logger.info("Test database configuration set");
        } catch (ClassNotFoundException e) {
            logger.error("Test database driver not found", e);
        }
    }
}