package com.jarvis.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Compact icon rail used by the JARVIS dashboard. */
public class JarvisSidebar extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Color RAIL = new Color(38, 44, 41);
    private static final Color ACTIVE = new Color(66, 76, 68);
    private static final Color ICON = new Color(221, 224, 215);
    private static final String[][] ITEMS = {{"home", "Home"}, {"activity", "Activity"}, {"reminders", "Reminders"}, {"apps", "Apps"}, {"settings", "Settings"}};
    private final Consumer<String> onNavigate;
    private final List<JButton> itemPanels = new ArrayList<>();
    private String active = "Home";

    public JarvisSidebar(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        setLayout(new BorderLayout());
        setBackground(RAIL);
        setBorder(new EmptyBorder(16, 12, 16, 12));
        setPreferredSize(new Dimension(68, 0));
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JLabel mark = new JLabel("J", SwingConstants.CENTER);
        mark.setForeground(new Color(247, 246, 242));
        mark.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        mark.setBorder(BorderFactory.createLineBorder(new Color(133, 145, 131)));
        mark.setPreferredSize(new Dimension(34, 34));
        mark.setMaximumSize(new Dimension(34, 34));
        mark.setAlignmentX(Component.CENTER_ALIGNMENT);
        top.add(mark);
        JLabel dot = new JLabel("·", SwingConstants.CENTER);
        dot.setForeground(new Color(191, 112, 86));
        dot.setAlignmentX(Component.CENTER_ALIGNMENT);
        top.add(dot);
        for (String[] item : ITEMS) {
            JButton panel = createItem(item[0], item[1]);
            itemPanels.add(panel);
            top.add(panel);
            top.add(Box.createVerticalStrut(8));
        }
        add(top, BorderLayout.NORTH);
        JLabel profile = new JLabel("S", SwingConstants.CENTER);
        profile.setForeground(ICON);
        profile.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        profile.setBorder(BorderFactory.createLineBorder(new Color(133, 145, 131)));
        profile.setPreferredSize(new Dimension(34, 34));
        JPanel bottom = new JPanel(new GridBagLayout());
        bottom.setOpaque(false);
        bottom.add(profile);
        add(bottom, BorderLayout.SOUTH);
    }

    private JButton createItem(String iconKey, String name) {
        JButton item = new JButton(JarvisIcons.of(iconKey, ICON, 18));
        item.setOpaque(true);
        item.setBackground(name.equals(active) ? ACTIVE : RAIL);
        item.setForeground(ICON);
        item.setPreferredSize(new Dimension(42, 42));
        item.setMaximumSize(new Dimension(42, 42));
        item.setAlignmentX(Component.CENTER_ALIGNMENT);
        item.setBorderPainted(false);
        item.setFocusPainted(false);
        item.setContentAreaFilled(true);
        item.setToolTipText(name);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.getAccessibleContext().setAccessibleName(name);
        item.addActionListener(e -> {
            setActive(name);
            if (onNavigate != null) onNavigate.accept(name);
        });
        return item;
    }

    public void setActive(String name) {
        active = name;
        for (int i = 0; i < itemPanels.size(); i++) itemPanels.get(i).setBackground(ITEMS[i][1].equals(active) ? ACTIVE : RAIL);
    }
}
