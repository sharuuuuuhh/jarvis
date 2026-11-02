package com.jarvis.gui;

import com.jarvis.database.ReminderDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** Reminder management page backed by ReminderDAO. */
public class JarvisRemindersPage extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Color PAGE = new Color(247, 246, 242);
    private static final Color SURFACE = new Color(250, 249, 246);
    private static final Color BORDER = new Color(224, 222, 214);
    private static final Color INK = new Color(24, 30, 31);
    private static final Color MUTED = new Color(119, 128, 130);
    private final ReminderDAO dao;
    private final JTextField titleField = new JTextField();
    private final JSpinner timeField;
    private final JComboBox<String> priorityField = new JComboBox<>(new String[]{"NORMAL", "HIGH", "LOW"});
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"Reminder", "Due", "Priority", "Status"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JLabel statusLabel = new JLabel(" ");
    private List<ReminderDAO.ReminderItem> reminders = List.of();
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("EEE, dd MMM · hh:mm a");

    public JarvisRemindersPage(ReminderDAO dao) {
        this.dao = dao;
        Date initial = Date.from(LocalDateTime.now().plusHours(1).withSecond(0).withNano(0).atZone(ZoneId.systemDefault()).toInstant());
        timeField = new JSpinner(new SpinnerDateModel(initial, null, null, Calendar.MINUTE));
        timeField.setEditor(new JSpinner.DateEditor(timeField, "yyyy-MM-dd HH:mm"));
        setLayout(new BorderLayout(0, 20));
        setBackground(PAGE);
        setBorder(new EmptyBorder(34, 44, 26, 44));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel title = new JPanel();
        title.setOpaque(false);
        title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("PLAN AHEAD");
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 10));
        eyebrow.setForeground(MUTED);
        JLabel heading = new JLabel("Reminders");
        heading.setFont(new Font("Segoe UI", Font.PLAIN, 30));
        heading.setForeground(INK);
        heading.setIcon(JarvisIcons.of("reminders", new Color(190, 111, 87), 22));
        heading.setIconTextGap(10);
        title.add(eyebrow);
        title.add(Box.createVerticalStrut(7));
        title.add(heading);
        JButton refresh = button("Refresh");
        refresh.addActionListener(e -> refresh());
        header.add(title, BorderLayout.WEST);
        header.add(refresh, BorderLayout.EAST);
        return header;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        JPanel createCard = surface(new BorderLayout(14, 12));
        createCard.setBorder(new EmptyBorder(16, 18, 16, 18));
        JLabel title = new JLabel("NEW REMINDER");
        title.setFont(new Font("Segoe UI", Font.BOLD, 9));
        title.setForeground(new Color(106, 120, 123));
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.insets = new Insets(0, 0, 0, 10);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        fields.add(fieldBlock("REMINDER", titleField), c);
        c.weightx = 0;
        c.gridx = 1;
        fields.add(fieldBlock("DATE & TIME", timeField), c);
        c.gridx = 2;
        fields.add(fieldBlock("PRIORITY", priorityField), c);
        JButton add = button("Add reminder");
        add.setBackground(new Color(233, 232, 224));
        add.addActionListener(e -> createReminder());
        JPanel form = new JPanel(new BorderLayout(10, 0));
        form.setOpaque(false);
        form.add(fields, BorderLayout.CENTER);
        form.add(add, BorderLayout.EAST);
        createCard.add(title, BorderLayout.NORTH);
        createCard.add(form, BorderLayout.CENTER);

        JPanel listCard = surface(new BorderLayout(0, 10));
        listCard.setBorder(new EmptyBorder(16, 18, 16, 18));
        JLabel listTitle = new JLabel("SCHEDULED REMINDERS");
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 9));
        listTitle.setForeground(new Color(106, 120, 123));
        table.setRowHeight(34);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        table.setForeground(INK);
        table.setBackground(SURFACE);
        table.setSelectionBackground(new Color(239, 237, 229));
        table.setSelectionForeground(INK);
        table.setGridColor(BORDER);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 10));
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setBackground(new Color(244, 243, 237));
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(SURFACE);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton complete = button("Mark complete");
        complete.addActionListener(e -> completeSelected());
        actions.add(statusLabel);
        actions.add(complete);
        listCard.add(listTitle, BorderLayout.NORTH);
        listCard.add(scroll, BorderLayout.CENTER);
        listCard.add(actions, BorderLayout.SOUTH);
        content.add(createCard, BorderLayout.NORTH);
        content.add(listCard, BorderLayout.CENTER);
        return content;
    }

    private JPanel fieldBlock(String label, JComponent field) {
        JPanel block = new JPanel();
        block.setOpaque(false);
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        JLabel caption = new JLabel(label);
        caption.setFont(new Font("Segoe UI", Font.BOLD, 8));
        caption.setForeground(MUTED);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        field.setForeground(INK);
        field.setBackground(SURFACE);
        field.setPreferredSize(new Dimension(label.equals("DATE & TIME") ? 150 : 100, 34));
        block.add(caption);
        block.add(Box.createVerticalStrut(5));
        block.add(field);
        return block;
    }

    private JPanel surface(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createLineBorder(BORDER));
        return panel;
    }

    private JButton button(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        button.setForeground(new Color(75, 87, 86));
        button.setBackground(SURFACE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(8, 12, 8, 12)));
        return button;
    }

    private void createReminder() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            setStatus("Enter a reminder first.", true);
            titleField.requestFocusInWindow();
            return;
        }
        LocalDateTime due = ((Date) timeField.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        if (!due.isAfter(LocalDateTime.now())) {
            setStatus("Choose a future date and time.", true);
            return;
        }
        if (dao.createReminder(title, due, String.valueOf(priorityField.getSelectedItem()))) {
            titleField.setText("");
            setStatus("Reminder added.", false);
            refresh();
        } else setStatus("Could not save the reminder.", true);
    }

    private void completeSelected() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= reminders.size()) {
            setStatus("Select a reminder to update.", true);
            return;
        }
        ReminderDAO.ReminderItem item = reminders.get(table.convertRowIndexToModel(row));
        if (!"PENDING".equalsIgnoreCase(item.getStatus())) {
            setStatus("That reminder is already complete.", true);
            return;
        }
        if (dao.markCompleted(item.getId())) {
            setStatus("Reminder marked complete.", false);
            refresh();
        } else setStatus("Could not update the reminder.", true);
    }

    public void refresh() {
        reminders = dao.getAllReminders();
        model.setRowCount(0);
        for (ReminderDAO.ReminderItem reminder : reminders) {
            model.addRow(new Object[]{reminder.getTitle(), reminder.getReminderTime().format(DISPLAY), reminder.getPriority(), reminder.getStatus()});
        }
    }

    private void setStatus(String text, boolean error) {
        statusLabel.setText(text);
        statusLabel.setForeground(error ? new Color(166, 74, 63) : new Color(83, 137, 99));
    }
}
