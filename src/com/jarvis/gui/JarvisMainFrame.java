package com.jarvis.gui;

import com.jarvis.core.CommandResult;
import com.jarvis.core.JarvisEngine;
import com.jarvis.database.ReminderDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Main application window for JARVIS — redesigned to match the modern dark UI reference image.
 *
 * Layout:
 *   TOP    : JarvisTopBar (logo + clock + weather)
 *   LEFT   : JarvisSidebar (nav icons)
 *   CENTER : Orb visualizer + greeting text
 *   RIGHT  : JarvisActionCards (quick action cards)
 *   BOTTOM : JarvisInputBar (voice + text input)
 *   FOOTER : Bottom tagline
 *
 * KTU S3 CSE OOP Mini Project - Lead: Theerth (TLY25CS120)
 */
public class JarvisMainFrame extends JFrame {

    private final JarvisEngine engine;
    private final JarvisTopBar topBar;
    private final JarvisSidebar sidebar;
    private JarvisOrb orbVisualizer;
    private final JarvisActionCards actionCards;
    private JarvisInputBar inputBar;

    // Keep legacy chat panel hidden but active for engine callback compatibility
    private final ChatBubblePanel chatPanel;

    // Colors — dark navy palette matching reference
    private static final Color BG_MAIN       = new Color(10, 14, 26);
    private static final Color BG_CENTER     = new Color(10, 14, 26);
    private static final Color WAVE_BG       = new Color(12, 17, 32);
    private static final Color FOOTER_TEXT   = new Color(60, 80, 120);
    private static final Color CYAN_ACCENT   = new Color(40, 130, 220);

    public JarvisMainFrame(JarvisEngine engine) {
        super("JARVIS • Personal AI Assistant");
        this.engine = engine;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1060, 660);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);
        setUndecorated(false);
        getContentPane().setBackground(BG_MAIN);
        setLayout(new BorderLayout());

        // ── TOP BAR ──────────────────────────────────────────────
        topBar = new JarvisTopBar();
        add(topBar, BorderLayout.NORTH);

        // ── SIDEBAR ──────────────────────────────────────────────
        sidebar = new JarvisSidebar(navItem -> handleNavigation(navItem));
        add(sidebar, BorderLayout.WEST);

        // ── CENTER PANEL (orb + greeting) ────────────────────────
        JPanel centerPanel = buildCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // ── ACTION CARDS (right) ─────────────────────────────────
        actionCards = new JarvisActionCards(cmd -> engine.processTextInput(cmd));
        add(actionCards, BorderLayout.EAST);

        // ── BOTTOM (input bar + footer) ──────────────────────────
        JPanel bottomSection = buildBottomSection();
        add(bottomSection, BorderLayout.SOUTH);

        // Hidden legacy chat panel for engine callbacks
        chatPanel = new ChatBubblePanel();

        // ── WIRE ENGINE EVENTS ───────────────────────────────────
        wireEngineListeners();
        wireKeyboardShortcuts();
    }

    // ─────────────────────────────────────────────────────────────
    //  BUILD PANELS
    // ─────────────────────────────────────────────────────────────

    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CENTER);

        // Orb + greeting stacked vertically
        JPanel orbColumn = new JPanel();
        orbColumn.setLayout(new BoxLayout(orbColumn, BoxLayout.Y_AXIS));
        orbColumn.setBackground(BG_CENTER);
        orbColumn.setBorder(new EmptyBorder(10, 0, 0, 0));

        orbVisualizer = new JarvisOrb();
        orbVisualizer.setAlignmentX(Component.CENTER_ALIGNMENT);
        orbVisualizer.setPreferredSize(new Dimension(300, 300));
        orbVisualizer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        orbVisualizer.setToolTipText("Click orb to toggle voice recording");
        orbVisualizer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                engine.toggleVoiceRecording();
            }
        });

        // Greeting text
        JLabel greetingLabel = buildGreetingLabel();

        orbColumn.add(Box.createVerticalGlue());
        orbColumn.add(orbVisualizer);
        orbColumn.add(Box.createVerticalStrut(18));
        orbColumn.add(greetingLabel);
        orbColumn.add(Box.createVerticalGlue());

        panel.add(orbColumn, BorderLayout.CENTER);
        return panel;
    }

    private JLabel buildGreetingLabel() {
        JLabel label = new JLabel(
            "<html><div style='text-align:center; color:#7EB4E8;'>" +
            "<span style='font-size:18pt; color:#7098C8;'>\u201C</span>" +
            "<span style='font-size:15pt; color:#B8D0F0;'> How can I help you today? </span>" +
            "<span style='font-size:18pt; color:#7098C8;'>\u201D</span>" +
            "</div></html>",
            SwingConstants.CENTER
        );
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setMaximumSize(new Dimension(500, 60));
        return label;
    }

    private JPanel buildBottomSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(BG_MAIN);

        inputBar = new JarvisInputBar();
        inputBar.setOnMicToggle(() -> engine.toggleVoiceRecording());
        inputBar.setOnSubmit(text -> {
            if (!text.isBlank()) {
                engine.processTextInput(text);
            }
        });

        // Footer tagline
        JPanel footer = buildFooter();

        section.add(inputBar, BorderLayout.CENTER);
        section.add(footer, BorderLayout.SOUTH);

        return section;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BG_MAIN);
        footer.setBorder(new EmptyBorder(2, 28, 10, 28));

        JLabel leftTag = new JLabel(
            "<html><span style='color:#2A4070; font-size:8pt;'>" +
            "MORE THAN AN ASSISTANT.<br>A SMARTER YOU.</span></html>"
        );

        JLabel rightTag = new JLabel("STARK TECHNOLOGIES");
        rightTag.setFont(new Font("Segoe UI", Font.BOLD, 9));
        rightTag.setForeground(new Color(40, 65, 110));

        footer.add(leftTag, BorderLayout.WEST);
        footer.add(rightTag, BorderLayout.EAST);
        return footer;
    }

    // ─────────────────────────────────────────────────────────────
    //  NAVIGATION
    // ─────────────────────────────────────────────────────────────

    private void handleNavigation(String navItem) {
        switch (navItem) {
            case "Settings" -> openSettings();
            case "Tasks"    -> engine.processTextInput("show reminders");
            case "Ideas"    -> engine.processTextInput("help");
            case "Apps"     -> engine.processTextInput("system info");
            default         -> { /* Home – no action */ }
        }
    }

    private void openSettings() {
        SettingsDialog dialog = new SettingsDialog(this, engine.getTtsService());
        dialog.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────
    //  ENGINE WIRING
    // ─────────────────────────────────────────────────────────────

    private void wireEngineListeners() {
        engine.addListener(new JarvisEngine.EngineListener() {
            @Override
            public void onStateChanged(JarvisEngine.EngineState state, String statusText) {
                SwingUtilities.invokeLater(() -> {
                    orbVisualizer.setEngineState(state);
                    topBar.updateEngineState(state, statusText);
                    boolean listening = state == JarvisEngine.EngineState.LISTENING;
                    inputBar.setMicActive(listening);
                });
            }

            @Override
            public void onUserUtterance(String text) {
                chatPanel.addUserMessage(text);
            }

            @Override
            public void onAssistantResponse(CommandResult result, String categoryTag) {
                chatPanel.addAssistantMessage(result.getDisplayMessage(), categoryTag);
                // Also show response as a transient overlay / tooltip on the orb
                SwingUtilities.invokeLater(() -> {
                    orbVisualizer.setToolTipText(result.getDisplayMessage());
                });
            }

            @Override
            public void onAudioLevel(double level) {
                orbVisualizer.setAudioLevel(level);
            }

            @Override
            public void onReminderAlert(ReminderDAO.ReminderItem item) {
                SwingUtilities.invokeLater(() -> {
                    chatPanel.addAssistantMessage("⏰ ALARM DUE: " + item.getTitle(), "REMINDER ALERT");
                    JOptionPane.showMessageDialog(
                        JarvisMainFrame.this,
                        "⏰ JARVIS REMINDER:\n\n" + item.getTitle(),
                        "Reminder Alert",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                });
            }
        });
    }

    private void wireKeyboardShortcuts() {
        // Ctrl+Space toggles mic
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK), "toggleMic");
        getRootPane().getActionMap().put("toggleMic", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                engine.toggleVoiceRecording();
            }
        });
    }
}
