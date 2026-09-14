package com.jarvis.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

/**
 * Right-side quick action card panel.
 * Four action cards: Ask anything, Manage my day, Play something, Control my system.
 * Matches the reference image card layout.
 */
public class JarvisActionCards extends JPanel {

    private static final Color BG = new Color(10, 14, 26);
    private static final Color CARD_BG = new Color(16, 22, 40);
    private static final Color CARD_HOVER = new Color(22, 32, 58);
    private static final Color CARD_BORDER = new Color(30, 48, 85);
    private static final Color CARD_BORDER_HOVER = new Color(50, 100, 180);
    private static final Color ICON_COLOR = new Color(60, 150, 230);
    private static final Color TEXT_PRIMARY = new Color(210, 225, 250);
    private static final Color TEXT_DIM = new Color(100, 125, 165);
    private static final Color ARROW_COLOR = new Color(80, 120, 180);

    private static final Object[][] CARDS = {
        {"💬", "Ask anything",        "\"What's the latest news?\"",  "search google for latest news"},
        {"📅", "Manage my day",        "\"Show my schedule\"",          "what time is it"},
        {"▶",  "Play something",       "\"Play my focus playlist\"",    "open chrome"},
        {"🖥", "Control my system",    "\"Open VS Code\"",              "open notepad"},
    };

    public JarvisActionCards(Consumer<String> onAction) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BG);
        setBorder(new EmptyBorder(10, 12, 10, 10));
        setPreferredSize(new Dimension(260, 0));

        for (Object[] card : CARDS) {
            add(createCard((String) card[0], (String) card[1], (String) card[2], (String) card[3], onAction));
            add(Box.createVerticalStrut(10));
        }
        add(Box.createVerticalGlue());
    }

    private JPanel createCard(String icon, String title, String subtitle, String command, Consumer<String> onAction) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(CARD_BG);
        card.setMaximumSize(new Dimension(260, 68));
        card.setPreferredSize(new Dimension(240, 62));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(10, 14, 10, 10)
        ));

        // Icon
        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        iconLabel.setForeground(ICON_COLOR);
        iconLabel.setPreferredSize(new Dimension(30, 30));

        // Text block
        JPanel textBlock = new JPanel();
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
        textBlock.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLabel.setForeground(TEXT_DIM);

        textBlock.add(titleLabel);
        textBlock.add(Box.createVerticalStrut(2));
        textBlock.add(subLabel);

        // Arrow
        JLabel arrow = new JLabel("›");
        arrow.setFont(new Font("Segoe UI", Font.BOLD, 20));
        arrow.setForeground(ARROW_COLOR);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(textBlock, BorderLayout.CENTER);
        card.add(arrow, BorderLayout.EAST);

        // Hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(CARD_HOVER);
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(CARD_BORDER_HOVER, 1, true),
                        new EmptyBorder(10, 14, 10, 10)
                ));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(CARD_BG);
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                        new EmptyBorder(10, 14, 10, 10)
                ));
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                onAction.accept(command);
            }
        });

        return card;
    }
}
