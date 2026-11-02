package com.jarvis.gui;

import com.jarvis.core.CommandResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Scrollable conversation panel rendering futuristic dialogue bubbles.
 * Demonstrates EVENT-DRIVEN GUI PROGRAMMING, SWING CUSTOM RENDERING, and LAYOUTS.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Theerth (TLY25CS120)
 */
public class ChatBubblePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final JPanel messagesContainer;
    private final JScrollPane scrollPane;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    // Colors
    private static final Color BG_DARK = new Color(13, 20, 38);
    private static final Color USER_BUBBLE_BG = new Color(16, 44, 87);
    private static final Color JARVIS_BUBBLE_BG = new Color(20, 30, 52);
    private static final Color CYAN_ACCENT = new Color(0, 240, 255);
    private static final Color TEXT_LIGHT = new Color(230, 240, 255);
    private static final Color TEXT_MUTED = new Color(140, 160, 190);

    public ChatBubblePanel() {
        setLayout(new BorderLayout());
        setBackground(BG_DARK);

        messagesContainer = new JPanel();
        messagesContainer.setLayout(new BoxLayout(messagesContainer, BoxLayout.Y_AXIS));
        messagesContainer.setBackground(BG_DARK);
        messagesContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        scrollPane = new JScrollPane(messagesContainer);
        scrollPane.setBackground(BG_DARK);
        scrollPane.getViewport().setBackground(BG_DARK);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        // Initial welcome bubble
        addAssistantMessage(
                "Greetings! I am JARVIS, your intelligent desktop voice assistant.\n" +
                "Developed for KTU S3 CSE OOP Mini Project by Jobin, Farsin, Sharon, and Theerth.\n" +
                "Press the Arc Reactor button or type a command below to get started.",
                "SYSTEM INIT"
        );
    }

    public void addUserMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
            row.setOpaque(false);

            JPanel bubble = new JPanel();
            bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
            bubble.setBackground(USER_BUBBLE_BG);
            bubble.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 180, 216), 1, true),
                    new EmptyBorder(8, 12, 8, 12)
            ));

            JLabel senderLabel = new JLabel("YOU • " + LocalTime.now().format(TIME_FMT));
            senderLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
            senderLabel.setForeground(CYAN_ACCENT);

            JLabel msgLabel = new JLabel("<html><body style='width: 320px; color: #FFFFFF; font-family: Segoe UI; font-size: 11pt;'>" +
                    escapeHtml(message) + "</body></html>");

            bubble.add(senderLabel);
            bubble.add(Box.createVerticalStrut(3));
            bubble.add(msgLabel);

            row.add(bubble);
            messagesContainer.add(row);
            messagesContainer.add(Box.createVerticalStrut(6));

            messagesContainer.revalidate();
            scrollToBottom();
        });
    }

    public void addAssistantMessage(String message, String categoryTag) {
        SwingUtilities.invokeLater(() -> {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
            row.setOpaque(false);

            JPanel bubble = new JPanel();
            bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
            bubble.setBackground(JARVIS_BUBBLE_BG);
            bubble.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 150, 180), 1, true),
                    new EmptyBorder(8, 12, 8, 12)
            ));

            String badge = (categoryTag != null && !categoryTag.isBlank()) ? " [" + categoryTag.toUpperCase() + "]" : "";
            JLabel senderLabel = new JLabel("JARVIS" + badge + " • " + LocalTime.now().format(TIME_FMT));
            senderLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
            senderLabel.setForeground(new Color(0, 255, 170));

            String formattedText = escapeHtml(message).replace("\n", "<br>");
            JLabel msgLabel = new JLabel("<html><body style='width: 360px; color: #E6F0FF; font-family: Segoe UI; font-size: 11pt;'>" +
                    formattedText + "</body></html>");

            bubble.add(senderLabel);
            bubble.add(Box.createVerticalStrut(3));
            bubble.add(msgLabel);

            row.add(bubble);
            messagesContainer.add(row);
            messagesContainer.add(Box.createVerticalStrut(6));

            messagesContainer.revalidate();
            scrollToBottom();
        });
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
