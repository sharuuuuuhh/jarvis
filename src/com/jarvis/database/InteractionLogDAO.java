package com.jarvis.database;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Data Access Object (DAO) for persisting system audit logs (tbl_interaction_logs).
 * Demonstrates the DAO DESIGN PATTERN and persistent transaction auditing.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Theerth (TLY25CS120)
 */
public class InteractionLogDAO {

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DatabaseManager dbManager = DatabaseManager.getInstance();
    private static final File LOG_FALLBACK_FILE = new File("jarvis_audit_logs.txt");

    public boolean logInteraction(String rawText, String intent, String status, String responseText) {
        if (rawText == null) rawText = "";
        if (intent == null) intent = "UNKNOWN";
        if (status == null) status = "SUCCESS";
        if (responseText == null) responseText = "";

        if (dbManager.isSqliteDriverAvailable()) {
            String sql = "INSERT INTO tbl_interaction_logs (raw_audio_text, detected_intent, execution_status, response_text, timestamp) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, rawText);
                pstmt.setString(2, intent);
                pstmt.setString(3, status);
                pstmt.setString(4, responseText);
                pstmt.setString(5, LocalDateTime.now().format(DT_FORMAT));
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[InteractionLogDAO] SQL error: " + e.getMessage());
            }
        }

        // Fallback append to file
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(LOG_FALLBACK_FILE, true), StandardCharsets.UTF_8))) {
            writer.println(String.format("[%s] INTENT: %s | STATUS: %s | INPUT: \"%s\" | RESPONSE: \"%s\"",
                    LocalDateTime.now().format(DT_FORMAT), intent, status, rawText, responseText));
            return true;
        } catch (IOException ignored) {}
        return false;
    }
}
