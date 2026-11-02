package com.jarvis.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/** Shortcuts page for common desktop assistant commands. */
public class JarvisAppsPage extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Color PAGE = new Color(247, 246, 242);
    private static final Color SURFACE = new Color(250, 249, 246);
    private static final Color BORDER = new Color(224, 222, 214);
    private static final Color INK = new Color(24, 30, 31);
    private static final Color MUTED = new Color(119, 128, 130);
    private final Consumer<String> onCommand;
    private final Runnable onReminders;

    private static final String[][] SHORTCUTS = {
            {"notepad", "Open Notepad", "Launch a simple text editor", "open notepad"},
            {"calculator", "Open Calculator", "Open the system calculator", "open calculator"},
            {"system", "System information", "Read current system details", "system info"},
            {"web", "Search the web", "Search Google for a topic", "search google for Java tutorials"},
            {"web", "Check the weather", "Get live weather for a city", "what's the weather in Kochi"},
            {"notepad", "Take a note", "Save a note privately on this computer", "take a note buy groceries"},
            {"assistant", "Ask JARVIS", "Ask an open-ended question or get help writing", "explain how solar panels work"},
            {"time", "Current time", "Ask Jarvis for the local time", "what time is it"},
            {"help", "Help", "See commands Jarvis understands", "help"}
    };

    public JarvisAppsPage(Consumer<String> onCommand, Runnable onReminders) {
        this.onCommand = onCommand;
        this.onReminders = onReminders;
        setLayout(new BorderLayout(0, 22));
        setBackground(PAGE);
        setBorder(new EmptyBorder(34, 44, 26, 44));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildGrid(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel title = new JPanel();
        title.setOpaque(false);
        title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("YOUR SHORTCUTS");
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 10));
        eyebrow.setForeground(MUTED);
        JLabel heading = new JLabel("Apps & actions");
        heading.setFont(new Font("Segoe UI", Font.PLAIN, 30));
        heading.setForeground(INK);
        heading.setIcon(JarvisIcons.of("apps", new Color(190, 111, 87), 22));
        heading.setIconTextGap(10);
        title.add(eyebrow);
        title.add(Box.createVerticalStrut(7));
        title.add(heading);
        JButton reminders = new JButton("Manage reminders");
        styleButton(reminders);
        reminders.addActionListener(e -> onReminders.run());
        header.add(title, BorderLayout.WEST);
        header.add(reminders, BorderLayout.EAST);
        return header;
    }

    private JScrollPane buildGrid() {
        JPanel grid = new JPanel(new GridLayout(0, 2, 14, 14));
        grid.setOpaque(false);
        for (String[] shortcut : SHORTCUTS) grid.add(createShortcut(shortcut));
        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JButton createShortcut(String[] item) {
        JButton card = new JButton("<html><b>" + item[1] + "</b><br><span style='font-weight:normal'>" + item[2] + "</span></html>");
        card.setLayout(new BorderLayout(14, 0));
        card.setHorizontalAlignment(SwingConstants.LEFT);
        card.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        card.setForeground(INK);
        card.setBackground(SURFACE);
        card.setOpaque(true);
        card.setFocusPainted(false);
        card.setContentAreaFilled(true);
        card.setBorderPainted(true);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(16, 16, 16, 14)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setIcon(JarvisIcons.of(item[0], new Color(79, 103, 110), 22));
        card.setIconTextGap(14);
        card.getAccessibleContext().setAccessibleName(item[1]);
        card.getAccessibleContext().setAccessibleDescription(item[2]);
        card.addActionListener(e -> onCommand.accept(item[3]));
        card.addChangeListener(e -> card.setBackground(card.getModel().isRollover() ? new Color(243, 242, 236) : SURFACE));
        return card;
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        button.setForeground(new Color(75, 87, 86));
        button.setBackground(SURFACE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(8, 12, 8, 12)));
    }
}
