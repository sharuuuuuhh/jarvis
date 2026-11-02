package com.jarvis.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/** Single featured quick action on the dashboard. */
public class JarvisActionCards extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Color CARD = new Color(233, 232, 224);
    private static final Color HOVER = new Color(225, 224, 215);

    public JarvisActionCards(Consumer<String> onAction) {
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(210, 154));
        setMinimumSize(new Dimension(180, 140));
        JButton card = new JButton("<html><div style='font-size:9px; letter-spacing:1px; color:#6A787B'>QUICK ACTION</div><br><div style='font-size:15px; color:#181E1F'>What can I do?</div></html>");
        card.setLayout(new BorderLayout());
        card.setHorizontalAlignment(SwingConstants.LEFT);
        card.setVerticalAlignment(SwingConstants.TOP);
        card.setBackground(CARD);
        card.setOpaque(true);
        card.setFocusPainted(false);
        card.setBorderPainted(false);
        card.setContentAreaFilled(true);
        card.setBorder(new EmptyBorder(18, 16, 14, 16));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JLabel arrow = new JLabel("↗");
        arrow.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        arrow.setForeground(new Color(190, 111, 87));
        card.add(arrow, BorderLayout.SOUTH);
        card.getAccessibleContext().setAccessibleName("Quick action: What can I do?");
        card.addActionListener(e -> onAction.accept("help"));
        card.addChangeListener(e -> card.setBackground(card.getModel().isRollover() ? HOVER : CARD));
        add(card, BorderLayout.CENTER);
    }
}
