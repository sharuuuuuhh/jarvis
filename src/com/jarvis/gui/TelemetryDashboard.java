package com.jarvis.gui;

import com.jarvis.core.JarvisEngine;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Top header bar displaying real-time system metrics, WhisperFlow status, and clock.
 * Demonstrates GUI EVENT-DRIVEN PROGRAMMING and PERIODIC TELEMETRY POLLING.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Theerth (TLY25CS120)
 */
public class TelemetryDashboard extends JPanel {
    private static final long serialVersionUID = 1L;

    private final JLabel statusLabel;
    private final JLabel clockLabel;
    private final JProgressBar ramBar;
    private final JLabel engineBadge;
    private final JButton settingsBtn;

    private static final Color BG_DARK = new Color(7, 12, 23);
    private static final Color CYAN_ACCENT = new Color(0, 240, 255);
    private static final DateTimeFormatter CLOCK_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public TelemetryDashboard(Runnable onOpenSettings) {
        setLayout(new BorderLayout(15, 0));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(10, 18, 10, 18));

        // Left Panel: Title & Engine Badge
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("J.A.R.V.I.S");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(CYAN_ACCENT);

        engineBadge = new JLabel("WHISPERFLOW ACTIVE");
        engineBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        engineBadge.setForeground(new Color(0, 255, 170));
        engineBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 255, 170), 1, true),
                new EmptyBorder(2, 6, 2, 6)
        ));

        statusLabel = new JLabel("● System Online");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(180, 200, 230));

        leftPanel.add(titleLabel);
        leftPanel.add(engineBadge);
        leftPanel.add(statusLabel);

        // Right Panel: RAM Bar, Clock & Settings Button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);

        JLabel ramLabel = new JLabel("RAM:");
        ramLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        ramLabel.setForeground(new Color(140, 160, 190));

        ramBar = new JProgressBar(0, 100);
        ramBar.setPreferredSize(new Dimension(80, 12));
        ramBar.setForeground(CYAN_ACCENT);
        ramBar.setBackground(new Color(20, 30, 50));
        ramBar.setBorder(BorderFactory.createEmptyBorder());

        clockLabel = new JLabel(LocalTime.now().format(CLOCK_FMT));
        clockLabel.setFont(new Font("Consolas", Font.BOLD, 14));
        clockLabel.setForeground(CYAN_ACCENT);

        settingsBtn = new JButton("⚙ Settings");
        settingsBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        settingsBtn.setForeground(Color.WHITE);
        settingsBtn.setBackground(new Color(25, 40, 70));
        settingsBtn.setFocusPainted(false);
        settingsBtn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        settingsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        settingsBtn.addActionListener(e -> {
            if (onOpenSettings != null) onOpenSettings.run();
        });

        rightPanel.add(ramLabel);
        rightPanel.add(ramBar);
        rightPanel.add(clockLabel);
        rightPanel.add(settingsBtn);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);

        // Timer for clock and RAM updates
        Timer telemetryTimer = new Timer(1000, e -> updateTelemetry());
        telemetryTimer.start();
        updateTelemetry();
    }

    private void updateTelemetry() {
        clockLabel.setText(LocalTime.now().format(CLOCK_FMT));

        Runtime r = Runtime.getRuntime();
        long total = r.totalMemory();
        long free = r.freeMemory();
        long used = total - free;
        int percent = (int) ((used * 100) / total);
        ramBar.setValue(percent);
        ramBar.setToolTipText(String.format("JVM RAM: %d MB / %d MB (%d%%)", used / (1024 * 1024), total / (1024 * 1024), percent));
    }

    public void updateEngineStatus(JarvisEngine.EngineState state, String text) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("● " + text);
            switch (state) {
                case LISTENING -> {
                    engineBadge.setText("LISTENING (MIC)");
                    engineBadge.setForeground(new Color(255, 140, 0));
                }
                case PROCESSING -> {
                    engineBadge.setText("TRANSCRIBING");
                    engineBadge.setForeground(CYAN_ACCENT);
                }
                case SPEAKING -> {
                    engineBadge.setText("SPEAKING");
                    engineBadge.setForeground(new Color(0, 255, 170));
                }
                case ERROR -> {
                    engineBadge.setText("ALERT");
                    engineBadge.setForeground(new Color(255, 60, 60));
                }
                default -> {
                    engineBadge.setText("WHISPERFLOW READY");
                    engineBadge.setForeground(new Color(0, 255, 170));
                }
            }
        });
    }
}
