package com.jarvis.database;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Data Access Object (DAO) for managing scheduled reminders (tbl_reminders).
 * Demonstrates the DAO DESIGN PATTERN and ABSTRACTION of database operations.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Theerth (TLY25CS120)
 */
public class ReminderDAO {

    public static class ReminderItem {
        private final int id;
        private final String title;
        private final LocalDateTime reminderTime;
        private final String priority;
        private String status;

        public ReminderItem(int id, String title, LocalDateTime reminderTime, String priority, String status) {
            this.id = id;
            this.title = title;
            this.reminderTime = reminderTime;
            this.priority = priority;
            this.status = status;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public LocalDateTime getReminderTime() { return reminderTime; }
        public String getPriority() { return priority; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        @Override
        public String toString() {
            return String.format("[%s] %s at %s (%s)", priority, title, 
                    reminderTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")), status);
        }
    }

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    // In-memory / file fallback for zero-dependency portability
    private static final List<ReminderItem> fallbackList = new ArrayList<>();
    private static final AtomicInteger fallbackIdGen = new AtomicInteger(1);
    private static final File FALLBACK_FILE = new File("jarvis_reminders_fallback.txt");

    static {
        loadFallbackFromFile();
    }

    public boolean createReminder(String title, LocalDateTime reminderTime, String priority) {
        if (dbManager.isSqliteDriverAvailable()) {
            String sql = "INSERT INTO tbl_reminders (title, reminder_time, priority, status) VALUES (?, ?, ?, 'PENDING')";
            try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, title);
                pstmt.setString(2, reminderTime.format(DT_FORMAT));
                pstmt.setString(3, priority);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[ReminderDAO] SQL error: " + e.getMessage());
            }
        }

        // Fallback execution
        synchronized (fallbackList) {
            ReminderItem item = new ReminderItem(fallbackIdGen.getAndIncrement(), title, reminderTime, priority, "PENDING");
            fallbackList.add(item);
            saveFallbackToFile();
            return true;
        }
    }

    public List<ReminderItem> getPendingReminders() {
        List<ReminderItem> list = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        if (dbManager.isSqliteDriverAvailable()) {
            String sql = "SELECT id, title, reminder_time, priority, status FROM tbl_reminders WHERE status = 'PENDING'";
            try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String title = rs.getString("title");
                    String timeStr = rs.getString("reminder_time");
                    String priority = rs.getString("priority");
                    String status = rs.getString("status");
                    LocalDateTime time = LocalDateTime.parse(timeStr, DT_FORMAT);

                    if (time.isBefore(now) || time.isEqual(now)) {
                        list.add(new ReminderItem(id, title, time, priority, status));
                    }
                }
                return list;
            } catch (SQLException e) {
                System.err.println("[ReminderDAO] SQL query error: " + e.getMessage());
            }
        }

        // Fallback
        synchronized (fallbackList) {
            for (ReminderItem item : fallbackList) {
                if ("PENDING".equalsIgnoreCase(item.getStatus()) && (item.getReminderTime().isBefore(now) || item.getReminderTime().isEqual(now))) {
                    list.add(item);
                }
            }
        }
        return list;
    }

    public boolean markCompleted(int id) {
        if (dbManager.isSqliteDriverAvailable()) {
            String sql = "UPDATE tbl_reminders SET status = 'COMPLETED' WHERE id = ?";
            try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[ReminderDAO] SQL update error: " + e.getMessage());
            }
        }

        synchronized (fallbackList) {
            for (ReminderItem item : fallbackList) {
                if (item.getId() == id) {
                    item.setStatus("COMPLETED");
                    saveFallbackToFile();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean markCancelled(int id) {
        if (dbManager.isSqliteDriverAvailable()) {
            String sql = "UPDATE tbl_reminders SET status = 'CANCELLED' WHERE id = ? AND status = 'PENDING'";
            try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[ReminderDAO] SQL cancel error: " + e.getMessage());
            }
        }
        synchronized (fallbackList) {
            for (ReminderItem item : fallbackList) {
                if (item.getId() == id && "PENDING".equalsIgnoreCase(item.getStatus())) {
                    item.setStatus("CANCELLED");
                    saveFallbackToFile();
                    return true;
                }
            }
        }
        return false;
    }

    public List<ReminderItem> getAllReminders() {
        List<ReminderItem> list = new ArrayList<>();
        if (dbManager.isSqliteDriverAvailable()) {
            String sql = "SELECT id, title, reminder_time, priority, status FROM tbl_reminders ORDER BY id DESC LIMIT 50";
            try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(new ReminderItem(
                            rs.getInt("id"),
                            rs.getString("title"),
                            LocalDateTime.parse(rs.getString("reminder_time"), DT_FORMAT),
                            rs.getString("priority"),
                            rs.getString("status")
                    ));
                }
                return list;
            } catch (SQLException ignored) {}
        }

        synchronized (fallbackList) {
            return new ArrayList<>(fallbackList);
        }
    }

    private static synchronized void saveFallbackToFile() {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(FALLBACK_FILE), StandardCharsets.UTF_8))) {
            for (ReminderItem r : fallbackList) {
                writer.println(r.getId() + "|" + r.getTitle() + "|" + r.getReminderTime().format(DT_FORMAT) + "|" + r.getPriority() + "|" + r.getStatus());
            }
        } catch (IOException ignored) {}
    }

    private static synchronized void loadFallbackFromFile() {
        if (!FALLBACK_FILE.exists()) return;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(FALLBACK_FILE), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    int id = Integer.parseInt(parts[0]);
                    fallbackIdGen.updateAndGet(curr -> Math.max(curr, id + 1));
                    fallbackList.add(new ReminderItem(id, parts[1], LocalDateTime.parse(parts[2], DT_FORMAT), parts[3], parts[4]));
                }
            }
        } catch (Exception ignored) {}
    }
}
