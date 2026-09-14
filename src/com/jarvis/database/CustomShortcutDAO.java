package com.jarvis.database;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Access Object (DAO) for user-defined custom voice macros (tbl_custom_shortcuts).
 * Demonstrates the DAO DESIGN PATTERN and persistent shortcut resolution.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Theerth (TLY25CS120)
 */
public class CustomShortcutDAO {

    public static class ShortcutEntry {
        private final String triggerPhrase;
        private final String targetPath;
        private final String actionType; // 'APP_LAUNCH', 'BROWSER', 'SYSTEM_CMD'

        public ShortcutEntry(String triggerPhrase, String targetPath, String actionType) {
            this.triggerPhrase = triggerPhrase.toLowerCase().trim();
            this.targetPath = targetPath.trim();
            this.actionType = actionType.toUpperCase().trim();
        }

        public String getTriggerPhrase() { return triggerPhrase; }
        public String getTargetPath() { return targetPath; }
        public String getActionType() { return actionType; }
    }

    private final DatabaseManager dbManager = DatabaseManager.getInstance();
    private static final Map<String, ShortcutEntry> fallbackShortcuts = new HashMap<>();
    private static final File SHORTCUT_FALLBACK_FILE = new File("jarvis_shortcuts.txt");

    static {
        loadFallback();
        // Add default sample shortcuts
        if (fallbackShortcuts.isEmpty()) {
            fallbackShortcuts.put("open github", new ShortcutEntry("open github", "https://github.com", "BROWSER"));
            fallbackShortcuts.put("open youtube", new ShortcutEntry("open youtube", "https://youtube.com", "BROWSER"));
            fallbackShortcuts.put("launch code", new ShortcutEntry("launch code", "code", "APP_LAUNCH"));
        }
    }

    public boolean registerShortcut(String trigger, String target, String actionType) {
        trigger = trigger.toLowerCase().trim();
        if (dbManager.isSqliteDriverAvailable()) {
            String sql = "INSERT OR REPLACE INTO tbl_custom_shortcuts (trigger_phrase, target_path, action_type, is_active) VALUES (?, ?, ?, 1)";
            try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, trigger);
                pstmt.setString(2, target);
                pstmt.setString(3, actionType);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[CustomShortcutDAO] SQL error: " + e.getMessage());
            }
        }

        fallbackShortcuts.put(trigger, new ShortcutEntry(trigger, target, actionType));
        saveFallback();
        return true;
    }

    public ShortcutEntry findShortcut(String phrase) {
        if (phrase == null) return null;
        phrase = phrase.toLowerCase().trim();

        if (dbManager.isSqliteDriverAvailable()) {
            String sql = "SELECT trigger_phrase, target_path, action_type FROM tbl_custom_shortcuts WHERE is_active = 1 AND LOWER(trigger_phrase) = ?";
            try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, phrase);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return new ShortcutEntry(rs.getString("trigger_phrase"), rs.getString("target_path"), rs.getString("action_type"));
                    }
                }
            } catch (SQLException ignored) {}
        }

        return fallbackShortcuts.get(phrase);
    }

    public Map<String, ShortcutEntry> getAllShortcuts() {
        Map<String, ShortcutEntry> result = new HashMap<>();
        if (dbManager.isSqliteDriverAvailable()) {
            String sql = "SELECT trigger_phrase, target_path, action_type FROM tbl_custom_shortcuts WHERE is_active = 1";
            try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    String trigger = rs.getString("trigger_phrase");
                    result.put(trigger, new ShortcutEntry(trigger, rs.getString("target_path"), rs.getString("action_type")));
                }
                return result;
            } catch (SQLException ignored) {}
        }

        return new HashMap<>(fallbackShortcuts);
    }

    private static synchronized void saveFallback() {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(SHORTCUT_FALLBACK_FILE), StandardCharsets.UTF_8))) {
            for (ShortcutEntry s : fallbackShortcuts.values()) {
                writer.println(s.getTriggerPhrase() + "|" + s.getTargetPath() + "|" + s.getActionType());
            }
        } catch (IOException ignored) {}
    }

    private static synchronized void loadFallback() {
        if (!SHORTCUT_FALLBACK_FILE.exists()) return;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(SHORTCUT_FALLBACK_FILE), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    fallbackShortcuts.put(parts[0], new ShortcutEntry(parts[0], parts[1], parts[2]));
                }
            }
        } catch (Exception ignored) {}
    }
}
