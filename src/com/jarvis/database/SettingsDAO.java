package com.jarvis.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object (DAO) for application settings stored in SQLite (tbl_settings).
 * Demonstrates the DAO DESIGN PATTERN.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Theerth (TLY25CS120)
 */
public class SettingsDAO {

    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    public String getSetting(String key, String defaultValue) {
        if (!dbManager.isSqliteDriverAvailable()) return defaultValue;
        String sql = "SELECT setting_value FROM tbl_settings WHERE setting_key = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("setting_value");
                }
            }
        } catch (SQLException ignored) {}
        return defaultValue;
    }

    public boolean setSetting(String key, String value, String dataType, String description) {
        if (!dbManager.isSqliteDriverAvailable()) return false;
        String sql = "INSERT OR REPLACE INTO tbl_settings (setting_key, setting_value, data_type, description, updated_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.setString(3, dataType);
            pstmt.setString(4, description);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[SettingsDAO] SQL error: " + e.getMessage());
            return false;
        }
    }
}
