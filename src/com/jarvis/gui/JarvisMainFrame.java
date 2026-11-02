package com.jarvis.gui;

import com.jarvis.core.CommandResult;
import com.jarvis.core.JarvisEngine;
import com.jarvis.database.ReminderDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Main dashboard window for JARVIS. */
public class JarvisMainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private static final Color PAGE = new Color(247, 246, 242);
    private static final Color SURFACE = new Color(250, 249, 246);
    private static final Color BORDER = new Color(224, 222, 214);
    private static final Color INK = new Color(24, 30, 31);
    private static final Color MUTED = new Color(119, 128, 130);
    private static final Color RAIL = new Color(38, 44, 41);
    private static final Color ACCENT = new Color(190, 111, 87);

    private final JarvisEngine engine;
    private final JarvisTopBar topBar;
    private final JarvisSidebar sidebar;
    private final JarvisInputBar inputBar;
    private final JLabel readyLabel;
    private final JPanel activityRows;
    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pageDeck = new JPanel(pageLayout);
    private final JarvisActivityPage activityPage;
    private String currentPage = "Home";
    private JButton voiceButton;

    public JarvisMainFrame(JarvisEngine engine) {
        super("JARVIS Assistant");
        this.engine = engine;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 640);
        setMinimumSize(new Dimension(820, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(PAGE);
        setLayout(new BorderLayout());

        topBar = new JarvisTopBar();
        add(topBar, BorderLayout.NORTH);
        sidebar = new JarvisSidebar(this::handleNavigation);
        add(sidebar, BorderLayout.WEST);
        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setBackground(PAGE);
        content.setBorder(new EmptyBorder(34, 44, 26, 44));
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JPanel greeting = new JPanel();
        greeting.setOpaque(false);
        greeting.setLayout(new BoxLayout(greeting, BoxLayout.Y_AXIS));
        JLabel date = new JLabel(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, hh:mm a")).toUpperCase());
        date.setFont(new Font("Segoe UI", Font.BOLD, 10));
        date.setForeground(MUTED);
        JLabel hello = new JLabel("Good morning.");
        hello.setFont(new Font("Segoe UI", Font.PLAIN, 30));
        hello.setForeground(INK);
        greeting.add(date);
        greeting.add(Box.createVerticalStrut(8));
        greeting.add(hello);
        readyLabel = new JLabel("●  Ready");
        readyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        readyLabel.setForeground(new Color(83, 137, 99));
        heading.add(greeting, BorderLayout.WEST);
        heading.add(readyLabel, BorderLayout.EAST);
        content.add(heading, BorderLayout.NORTH);

        JPanel dashboard = new JPanel(new BorderLayout(0, 18));
        dashboard.setOpaque(false);
        dashboard.setBorder(new EmptyBorder(26, 0, 0, 0));
        JPanel middle = new JPanel(new BorderLayout(16, 0));
        middle.setOpaque(false);
        JPanel left = new JPanel(new BorderLayout(0, 18));
        left.setOpaque(false);
        left.add(buildVoiceCard(), BorderLayout.NORTH);
        activityRows = new JPanel();
        activityRows.setOpaque(false);
        activityRows.setLayout(new BoxLayout(activityRows, BoxLayout.Y_AXIS));
        activityRows.setPreferredSize(new Dimension(280, 96));
        JLabel emptyActivity = new JLabel("Your recent commands will appear here.");
        emptyActivity.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        emptyActivity.setForeground(MUTED);
        emptyActivity.setBorder(new EmptyBorder(12, 0, 8, 0));
        activityRows.add(emptyActivity);
        JPanel recent = buildRecentCard(activityRows);
        left.add(recent, BorderLayout.CENTER);
        middle.add(left, BorderLayout.CENTER);
        JarvisActionCards actions = new JarvisActionCards(command -> engine.processTextInput(command));
        JPanel actionSlot = new JPanel(new BorderLayout());
        actionSlot.setOpaque(false);
        actionSlot.add(actions, BorderLayout.NORTH);
        middle.add(actionSlot, BorderLayout.EAST);
        dashboard.add(middle, BorderLayout.CENTER);
        content.add(dashboard, BorderLayout.CENTER);

        pageDeck.setBackground(PAGE);
        pageDeck.add(content, "Home");
        activityPage = new JarvisActivityPage(command -> {
            if (command != null && !command.isBlank()) engine.processTextInput(command);
        });
        pageDeck.add(activityPage, "Activity");
        pageDeck.add(new JarvisRemindersPage(engine.getReminderDAO()), "Reminders");
        pageDeck.add(new JarvisAppsPage(command -> engine.processTextInput(command),
                () -> { sidebar.setActive("Reminders"); showPage("Reminders"); }), "Apps");

        inputBar = new JarvisInputBar();
        inputBar.setOnMicToggle(engine::toggleVoiceRecording);
        inputBar.setOnSubmit(text -> { if (!text.isBlank()) engine.processTextInput(text); });
        JPanel appContent = new JPanel(new BorderLayout());
        appContent.setBackground(PAGE);
        appContent.add(pageDeck, BorderLayout.CENTER);
        appContent.add(inputBar, BorderLayout.SOUTH);
        appContent.setBorder(new EmptyBorder(0, 0, 26, 0));
        add(appContent, BorderLayout.CENTER);

        wireEngineListeners();
        wireKeyboardShortcuts();
    }

    private JLabel voiceTranscript;

    private JPanel buildVoiceCard() {
        JPanel card = surfacePanel(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(18, 20, 16, 20));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = eyebrow("VOICE INPUT");
        title.setIcon(JarvisIcons.of("voice", new Color(106, 120, 123), 14));
        title.setIconTextGap(7);
        JLabel dot = new JLabel("●");
        dot.setForeground(new Color(91, 145, 105));
        header.add(title, BorderLayout.WEST);
        header.add(dot, BorderLayout.EAST);
        voiceTranscript = new JLabel("Click the microphone and say something");
        voiceTranscript.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        voiceTranscript.setForeground(INK);
        JPanel lower = new JPanel(new BorderLayout());
        lower.setOpaque(false);
        lower.add(new WaveformPanel(), BorderLayout.WEST);
        voiceButton = new JButton("Start speaking");
        voiceButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        voiceButton.setForeground(Color.WHITE);
        voiceButton.setBackground(ACCENT);
        voiceButton.setFocusPainted(false);
        voiceButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        voiceButton.setBorder(new EmptyBorder(8, 13, 8, 13));
        voiceButton.setIcon(JarvisIcons.of("voice", Color.WHITE, 15));
        voiceButton.setIconTextGap(7);
        voiceButton.setToolTipText("Click to start speaking; click again when your command is complete");
        voiceButton.addActionListener(e -> engine.toggleVoiceRecording());
        lower.add(voiceButton, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);
        card.add(voiceTranscript, BorderLayout.CENTER);
        card.add(lower, BorderLayout.SOUTH);
        card.setPreferredSize(new Dimension(600, 138));
        return card;
    }

    private JPanel buildRecentCard(JPanel rows) {
        JPanel card = surfacePanel(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 16, 15, 16));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(eyebrow("RECENT ACTIVITY"), BorderLayout.WEST);
        JButton viewAll = new JButton("View all →");
        styleTextButton(viewAll);
        viewAll.addActionListener(e -> handleNavigation("Activity"));
        header.add(viewAll, BorderLayout.EAST);
        JSeparator separator = new JSeparator();
        separator.setForeground(BORDER);
        card.add(header, BorderLayout.NORTH);
        card.add(separator, BorderLayout.CENTER);
        card.add(rows, BorderLayout.SOUTH);
        return card;
    }

    private JPanel surfacePanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createLineBorder(BORDER));
        return panel;
    }

    private JLabel eyebrow(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 9));
        label.setForeground(new Color(106, 120, 123));
        return label;
    }

    private void styleTextButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        button.setForeground(new Color(124, 104, 92));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void addActivity(String title, String detail, String icon) {
        SwingUtilities.invokeLater(() -> {
            if (activityRows.getComponentCount() == 1 && activityRows.getComponent(0) instanceof JLabel) activityRows.removeAll();
            JPanel row = new JPanel(new BorderLayout(12, 0));
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(9, 0, 3, 0));
            JLabel glyph = new JLabel("", SwingConstants.CENTER);
            glyph.setOpaque(true);
            glyph.setBackground(new Color(239, 238, 232));
            glyph.setForeground(new Color(79, 103, 110));
            glyph.setIcon(JarvisIcons.of(icon, new Color(79, 103, 110), 18));
            glyph.setPreferredSize(new Dimension(34, 34));
            JPanel copy = new JPanel();
            copy.setOpaque(false);
            copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
            JLabel name = new JLabel(title);
            name.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            name.setForeground(INK);
            JLabel sub = new JLabel(detail);
            sub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            sub.setForeground(MUTED);
            copy.add(name);
            copy.add(Box.createVerticalStrut(2));
            copy.add(sub);
            JLabel time = new JLabel(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
            time.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            time.setForeground(MUTED);
            row.add(glyph, BorderLayout.WEST);
            row.add(copy, BorderLayout.CENTER);
            row.add(time, BorderLayout.EAST);
            rowsOrFallback(row);
        });
    }

    private void rowsOrFallback(JPanel row) {
        activityRows.add(row, 0);
        while (activityRows.getComponentCount() > 3) activityRows.remove(activityRows.getComponentCount() - 1);
        activityRows.revalidate();
        activityRows.repaint();
    }

    private void handleNavigation(String navItem) {
        switch (navItem) {
            case "Settings" -> {
                SettingsDialog dialog = new SettingsDialog(this, engine.getTtsService());
                dialog.setVisible(true);
                sidebar.setActive(currentPage);
            }
            case "Reminders" -> showPage("Reminders");
            case "Activity" -> showPage("Activity");
            case "Apps" -> showPage("Apps");
            case "Home" -> showPage("Home");
            default -> { }
        }
    }

    private void showPage(String page) {
        currentPage = page;
        pageLayout.show(pageDeck, page);
        if ("Reminders".equals(page)) {
            for (Component component : pageDeck.getComponents()) {
                if (component instanceof JarvisRemindersPage reminders) reminders.refresh();
            }
        }
    }

    private void wireEngineListeners() {
        engine.addListener(new JarvisEngine.EngineListener() {
            @Override public void onStateChanged(JarvisEngine.EngineState state, String statusText) {
                SwingUtilities.invokeLater(() -> {
                    topBar.updateEngineState(state, statusText);
                    boolean listening = state == JarvisEngine.EngineState.LISTENING;
                    inputBar.setMicActive(listening);
                    voiceButton.setText(switch (state) {
                        case LISTENING -> "Stop & send";
                        case PROCESSING -> "Processing…";
                        default -> "Start speaking";
                    });
                    voiceButton.setEnabled(state != JarvisEngine.EngineState.PROCESSING);
                    voiceButton.setBackground(listening ? new Color(166, 74, 63) : ACCENT);
                    readyLabel.setText("●  " + statusText);
                    readyLabel.setForeground(listening ? ACCENT : state == JarvisEngine.EngineState.ERROR ? new Color(176, 75, 66) : new Color(83, 137, 99));
                });
            }
            @Override public void onUserUtterance(String text) {
                SwingUtilities.invokeLater(() -> voiceTranscript.setText("“" + text + "”"));
                addActivity("Voice command", text, "voice");
                activityPage.addEntry("Voice command", text, text);
            }
            @Override public void onAssistantResponse(CommandResult result, String categoryTag) {
                addActivity(categoryTag == null ? "Jarvis response" : categoryTag, result.getDisplayMessage(), "assistant");
                activityPage.addEntry(categoryTag == null ? "Jarvis response" : categoryTag, result.getDisplayMessage(), null);
            }
            @Override public void onAudioLevel(double level) { }
            @Override public void onReminderAlert(ReminderDAO.ReminderItem item) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(JarvisMainFrame.this, "JARVIS REMINDER:\n\n" + item.getTitle(), "Reminder Alert", JOptionPane.INFORMATION_MESSAGE);
                });
            }
        });
    }

    private void wireKeyboardShortcuts() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK), "toggleMic");
        getRootPane().getActionMap().put("toggleMic", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { engine.toggleVoiceRecording(); }
        });
    }

    private static class WaveformPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private double tick;
        private final Timer timer;
        WaveformPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(300, 26));
            timer = new Timer(70, e -> { tick += .35; repaint(); });
            timer.start();
        }
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(ACCENT);
            int bars = 38;
            int gap = 3;
            int width = Math.max(2, (getWidth() - (bars - 1) * gap) / bars);
            for (int i = 0; i < bars; i++) {
                int h = 5 + (int)(Math.abs(Math.sin(tick + i * .64)) * 17);
                g.fillRoundRect(i * (width + gap), (getHeight() - h) / 2, width, h, 2, 2);
            }
            g.dispose();
        }
    }
}
