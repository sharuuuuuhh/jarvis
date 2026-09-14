package com.jarvis.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Panel rendering one-click command chips for rapid testing and demonstrations.
 * Demonstrates EVENT-DRIVEN PROGRAMMING and LAMBDA EXPRESSIONS in Java Swing.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Theerth (TLY25CS120)
 */
public class QuickActionsPanel extends JPanel {

    private static final Color BG_DARK = new Color(10, 15, 29);
    private static final Color CHIP_BG = new Color(20, 32, 56);
    private static final Color CHIP_HOVER = new Color(28, 48, 86);
    private static final Color CYAN_BORDER = new Color(0, 180, 216);

    public QuickActionsPanel(Consumer<String> onActionTriggered) {
        setLayout(new FlowLayout(FlowLayout.CENTER, 8, 6));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(4, 10, 8, 10));

        String[][] actions = {
            {"📝 Notepad", "open notepad"},
            {"🧮 Calculator", "open calculator"},
            {"📊 System Info", "system info"},
            {"⏰ 10s Timer", "remind me to present project in 10 seconds"},
            {"🌐 Search KTU", "search google for KTU CSE syllabus"},
            {"🕒 What Time", "what time is it"},
            {"❓ Help", "help"}
        };

        for (String[] act : actions) {
            JButton chip = createChip(act[0], () -> onActionTriggered.accept(act[1]));
            add(chip);
        }
    }

    private JButton createChip(String text, Runnable onClick) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setForeground(new Color(220, 235, 255));
        btn.setBackground(CHIP_BG);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CYAN_BORDER, 1, true),
                new EmptyBorder(5, 11, 5, 11)
        ));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(CHIP_HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(CHIP_BG);
            }
        });

        btn.addActionListener(e -> onClick.run());
        return btn;
    }
}
