package com.jarvis.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/** Session activity page with a repeat action for previous user commands. */
public class JarvisActivityPage extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Color PAGE = new Color(247, 246, 242);
    private static final Color SURFACE = new Color(250, 249, 246);
    private static final Color BORDER = new Color(224, 222, 214);
    private static final Color INK = new Color(24, 30, 31);
    private static final Color MUTED = new Color(119, 128, 130);
    private final DefaultListModel<Entry> model = new DefaultListModel<>();
    private final JList<Entry> list = new JList<>(model);
    private final JLabel emptyLabel = new JLabel("Your commands and Jarvis responses will appear here.");
    private final Consumer<String> onRepeat;

    public JarvisActivityPage(Consumer<String> onRepeat) {
        this.onRepeat = onRepeat;
        setLayout(new BorderLayout(0, 22));
        setBackground(PAGE);
        setBorder(new EmptyBorder(34, 44, 26, 44));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("YOUR SESSION");
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 10));
        eyebrow.setForeground(MUTED);
        JLabel title = new JLabel("Activity");
        title.setFont(new Font("Segoe UI", Font.PLAIN, 30));
        title.setForeground(INK);
        title.setIcon(JarvisIcons.of("activity", new Color(190, 111, 87), 22));
        title.setIconTextGap(10);
        titleBlock.add(eyebrow);
        titleBlock.add(Box.createVerticalStrut(7));
        titleBlock.add(title);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        JButton repeat = button("Repeat command");
        repeat.addActionListener(e -> repeatSelected());
        JButton clear = button("Clear history");
        clear.addActionListener(e -> model.clear());
        buttons.add(repeat);
        buttons.add(clear);
        header.add(titleBlock, BorderLayout.WEST);
        header.add(buttons, BorderLayout.EAST);

        list.setBackground(SURFACE);
        list.setSelectionBackground(new Color(239, 237, 229));
        list.setSelectionForeground(INK);
        list.setFixedCellHeight(76);
        list.setCellRenderer(new EntryRenderer());
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(SURFACE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(SURFACE);
        content.setBorder(new EmptyBorder(16, 18, 16, 18));
        emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emptyLabel.setForeground(MUTED);
        content.add(emptyLabel, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
        updateEmptyState();
    }

    public void addEntry(String title, String detail, String command) {
        SwingUtilities.invokeLater(() -> {
            model.add(0, new Entry(title, detail, command, command == null ? "assistant" : "voice",
                    LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))));
            updateEmptyState();
        });
    }

    private void updateEmptyState() {
        emptyLabel.setVisible(model.isEmpty());
        if (!model.isEmpty()) emptyLabel.setBorder(new EmptyBorder(0, 0, 12, 0));
    }

    private void repeatSelected() {
        Entry entry = list.getSelectedValue();
        if (entry == null || entry.command == null) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        onRepeat.accept(entry.command);
    }

    private JButton button(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        button.setForeground(new Color(94, 104, 103));
        button.setBackground(SURFACE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(8, 12, 8, 12)));
        return button;
    }

    private static final class Entry {
        final String title, detail, command, icon, time;
        Entry(String title, String detail, String command, String icon, String time) {
            this.title = title;
            this.detail = detail == null ? "" : detail;
            this.command = command;
            this.icon = icon;
            this.time = time;
        }
    }

    private static final class EntryRenderer extends JPanel implements ListCellRenderer<Entry> {
        private static final long serialVersionUID = 1L;
        private final JLabel title = new JLabel();
        private final JLabel detail = new JLabel();
        private final JLabel time = new JLabel();
        private final JLabel icon = new JLabel("", SwingConstants.CENTER);
        EntryRenderer() {
            setLayout(new BorderLayout(12, 0));
            setBorder(new EmptyBorder(10, 12, 10, 12));
            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            title.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            title.setForeground(INK);
            detail.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            detail.setForeground(MUTED);
            text.add(title);
            text.add(Box.createVerticalStrut(4));
            text.add(detail);
            time.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            time.setForeground(MUTED);
            icon.setOpaque(true);
            icon.setBackground(new Color(239, 238, 232));
            icon.setPreferredSize(new Dimension(36, 36));
            add(text, BorderLayout.CENTER);
            add(time, BorderLayout.EAST);
            add(icon, BorderLayout.WEST);
        }
        @Override public Component getListCellRendererComponent(JList<? extends Entry> list, Entry entry, int index, boolean selected, boolean focus) {
            title.setText(entry.title);
            detail.setText(entry.detail);
            time.setText(entry.time);
            icon.setIcon(JarvisIcons.of(entry.icon, new Color(79, 103, 110), 18));
            setBackground(selected ? new Color(239, 237, 229) : SURFACE);
            setOpaque(true);
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(10, 12, 10, 12)));
            return this;
        }
    }
}
