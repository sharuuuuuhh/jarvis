package com.jarvis.gui;

import com.jarvis.core.JarvisEngine;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Top header bar for the redesigned JARVIS UI.
 * Shows: JARVIS logo + subtitle (left), clock + date + weather widget (right).
 * Matches the reference image design — clean, minimal, dark.
 */
public class JarvisTopBar extends JPanel {

    private final JLabel clockLabel;
    private final JLabel dateLabel;
    private final JLabel engineStateLabel;

    private static final Color BG = new Color(10, 14, 26);
    private static final Color TEXT_PRIMARY = new Color(220, 235, 255);
    private static final Color TEXT_DIM = new Color(130, 155, 190);
    private static final Color BLUE_LINE = new Color(30, 130, 220);
    private static final DateTimeFormatter CLOCK_FMT = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy");

    public JarvisTopBar() {
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(16, 28, 12, 28));
        setPreferredSize(new Dimension(0, 80));

        // ---- LEFT: Logo ----
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        JLabel logo = new JLabel("J A R V I S");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(TEXT_PRIMARY);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Blue underline accent
        JPanel underline = new JPanel();
        underline.setBackground(BLUE_LINE);
        underline.setPreferredSize(new Dimension(36, 2));
        underline.setMaximumSize(new Dimension(36, 2));
        underline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("YOUR PERSONAL ASSISTANT");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        subtitle.setForeground(TEXT_DIM);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(logo);
        leftPanel.add(Box.createVerticalStrut(3));
        leftPanel.add(underline);
        leftPanel.add(Box.createVerticalStrut(3));
        leftPanel.add(subtitle);

        // ---- RIGHT: Clock + Weather ----
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);

        // Clock & date block
        JPanel clockBlock = new JPanel();
        clockBlock.setLayout(new BoxLayout(clockBlock, BoxLayout.Y_AXIS));
        clockBlock.setOpaque(false);

        clockLabel = new JLabel(LocalDateTime.now().format(CLOCK_FMT));
        clockLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        clockLabel.setForeground(TEXT_PRIMARY);
        clockLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        dateLabel = new JLabel(LocalDateTime.now().format(DATE_FMT));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dateLabel.setForeground(TEXT_DIM);
        dateLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        clockBlock.add(clockLabel);
        clockBlock.add(dateLabel);

        // Divider
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setForeground(new Color(40, 60, 100));
        sep.setPreferredSize(new Dimension(1, 40));

        // Weather block
        JPanel weatherBlock = new JPanel();
        weatherBlock.setLayout(new BoxLayout(weatherBlock, BoxLayout.Y_AXIS));
        weatherBlock.setOpaque(false);
        weatherBlock.setBorder(new EmptyBorder(0, 18, 0, 0));

        JLabel weatherIcon = new JLabel("⛅");
        weatherIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        weatherIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tempLabel = new JLabel("28°C");
        tempLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tempLabel.setForeground(TEXT_PRIMARY);
        tempLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel cityLabel = new JLabel("Kochi");
        cityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        cityLabel.setForeground(TEXT_DIM);
        cityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        weatherBlock.add(weatherIcon);
        weatherBlock.add(tempLabel);
        weatherBlock.add(cityLabel);

        // Engine state indicator (small, subtle)
        engineStateLabel = new JLabel("● READY");
        engineStateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        engineStateLabel.setForeground(new Color(60, 200, 130));

        rightPanel.add(clockBlock);
        rightPanel.add(Box.createHorizontalStrut(20));
        rightPanel.add(sep);
        rightPanel.add(weatherBlock);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
        add(engineStateLabel, BorderLayout.SOUTH);

        // Live clock timer
        new Timer(1000, e -> {
            LocalDateTime now = LocalDateTime.now();
            clockLabel.setText(now.format(CLOCK_FMT));
            dateLabel.setText(now.format(DATE_FMT));
        }).start();
    }

    public void updateEngineState(JarvisEngine.EngineState state, String text) {
        SwingUtilities.invokeLater(() -> {
            String dot = "● ";
            switch (state) {
                case LISTENING   -> { engineStateLabel.setText(dot + "LISTENING"); engineStateLabel.setForeground(new Color(255, 160, 50)); }
                case PROCESSING  -> { engineStateLabel.setText(dot + "PROCESSING"); engineStateLabel.setForeground(new Color(0, 200, 255)); }
                case SPEAKING    -> { engineStateLabel.setText(dot + "SPEAKING"); engineStateLabel.setForeground(new Color(80, 230, 160)); }
                case ERROR       -> { engineStateLabel.setText(dot + "ERROR"); engineStateLabel.setForeground(new Color(255, 80, 80)); }
                default          -> { engineStateLabel.setText(dot + "READY"); engineStateLabel.setForeground(new Color(60, 200, 130)); }
            }
        });
    }
}
