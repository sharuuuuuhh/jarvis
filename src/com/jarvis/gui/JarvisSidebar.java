package com.jarvis.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

/**
 * Left vertical sidebar with navigation icons.
 * Shows Home, Apps, Tasks, Ideas, Settings nav items.
 * Matches the reference image sidebar design.
 */
public class JarvisSidebar extends JPanel {

    private static final Color BG = new Color(14, 19, 34);
    private static final Color ITEM_BG = new Color(20, 28, 48);
    private static final Color ACTIVE_BG = new Color(25, 60, 110);
    private static final Color ACTIVE_ICON = new Color(60, 160, 255);
    private static final Color IDLE_ICON = new Color(90, 110, 150);
    private static final Color TEXT_DIM = new Color(100, 120, 160);

    private final Consumer<String> onNavigate;
    private String activeItem = "Home";

    private static final String[][] NAV_ITEMS = {
        {"🏠", "Home"},
        {"⊞",  "Apps"},
        {"☑",  "Tasks"},
        {"💡", "Ideas"},
        {"⚙",  "Settings"},
    };

    public JarvisSidebar(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BG);
        setBorder(new EmptyBorder(20, 8, 20, 8));
        setPreferredSize(new Dimension(88, 0));

        add(Box.createVerticalStrut(8));
        for (String[] item : NAV_ITEMS) {
            add(createNavItem(item[0], item[1]));
            add(Box.createVerticalStrut(6));
        }
        add(Box.createVerticalGlue());
    }

    private JPanel createNavItem(String emoji, String label) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setMaximumSize(new Dimension(72, 72));
        panel.setPreferredSize(new Dimension(72, 72));
        panel.setBackground(label.equals(activeItem) ? ACTIVE_BG : ITEM_BG);
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Round corners via border painter
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(30, 50, 90), 1, true),
                new EmptyBorder(8, 6, 6, 6)
        ));

        JLabel iconLabel = new JLabel(emoji, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        iconLabel.setForeground(label.equals(activeItem) ? ACTIVE_ICON : IDLE_ICON);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel textLabel = new JLabel(label, SwingConstants.CENTER);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        textLabel.setForeground(label.equals(activeItem) ? ACTIVE_ICON : TEXT_DIM);
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(iconLabel);
        panel.add(Box.createVerticalStrut(2));
        panel.add(textLabel);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setActive(label);
                if (onNavigate != null) onNavigate.accept(label);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!label.equals(activeItem)) {
                    panel.setBackground(new Color(22, 34, 60));
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (!label.equals(activeItem)) {
                    panel.setBackground(ITEM_BG);
                }
            }
        });

        return panel;
    }

    public void setActive(String label) {
        this.activeItem = label;
        // Rebuild sidebar items with new active state
        removeAll();
        setBorder(new EmptyBorder(20, 8, 20, 8));
        add(Box.createVerticalStrut(8));
        for (String[] item : NAV_ITEMS) {
            add(createNavItem(item[0], item[1]));
            add(Box.createVerticalStrut(6));
        }
        add(Box.createVerticalGlue());
        revalidate();
        repaint();
    }
}
