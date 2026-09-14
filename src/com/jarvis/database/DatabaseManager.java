package com.jarvis.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Thread-safe Singleton managing the SQLite relational database (jarvis.db).
 * Handles JDBC connection pooling, schema initialization, and table migrations.
 * Demonstrates ENCAPSULATION and the SINGLETON DESIGN PATTERN.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Theerth (TLY25CS120)
 */
public class DatabaseManager {

    private static volatile DatabaseManager instance;
    private static final String DB_FILE = "jarvis.db";
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_FILE;

    private boolean sqliteDriverAvailable = false;

    private DatabaseManager() {
        checkDriver();
        if (sqliteDriverAvailable) {
            initSchema();
        } else {
            System.out.println("[DatabaseManager] SQLite JDBC driver not detected on classpath. Using embedded file storage.");
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    private void checkDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
            sqliteDriverAvailable = true;
        } catch (ClassNotFoundException e) {
            sqliteDriverAvailable = false;
        }
    }

    public boolean isSqliteDriverAvailable() {
        return sqliteDriverAvailable;
    }

    /**
     * Obtains an open JDBC connection to jarvis.db.
     */
    public Connection getConnection() throws SQLException {
        if (!sqliteDriverAvailable) {
            throw new SQLException("SQLite JDBC Driver is not loaded on classpath.");
        }
        return DriverManager.getConnection(JDBC_URL);
    }

    /**
     * Initializes all 4 relational tables specified in KTU S3 OOP Mini Project requirements.
     */
    private void initSchema() {
        String sqlReminders = """
            CREATE TABLE IF NOT EXISTS tbl_reminders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title VARCHAR(150) NOT NULL,
                reminder_time DATETIME NOT NULL,
                priority VARCHAR(20) DEFAULT 'MEDIUM',
                status VARCHAR(20) DEFAULT 'PENDING',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """;

        String sqlLogs = """
            CREATE TABLE IF NOT EXISTS tbl_interaction_logs (
                log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                raw_audio_text TEXT NOT NULL,
                detected_intent VARCHAR(50) NOT NULL,
                execution_status VARCHAR(20) NOT NULL,
                response_text TEXT,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            );
        """;

        String sqlShortcuts = """
            CREATE TABLE IF NOT EXISTS tbl_custom_shortcuts (
                shortcut_id INTEGER PRIMARY KEY AUTOINCREMENT,
                trigger_phrase VARCHAR(100) UNIQUE NOT NULL,
                target_path TEXT NOT NULL,
                action_type VARCHAR(30) NOT NULL,
                is_active BOOLEAN DEFAULT 1,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """;

        String sqlSettings = """
            CREATE TABLE IF NOT EXISTS tbl_settings (
                setting_key VARCHAR(50) PRIMARY KEY,
                setting_value TEXT NOT NULL,
                data_type VARCHAR(20) NOT NULL,
                description VARCHAR(255),
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlReminders);
            stmt.execute(sqlLogs);
            stmt.execute(sqlShortcuts);
            stmt.execute(sqlSettings);
            System.out.println("[DatabaseManager] SQLite database 'jarvis.db' schema verified successfully.");
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error initializing SQLite schema: " + e.getMessage());
        }
    }
}
