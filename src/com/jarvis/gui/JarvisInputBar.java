package com.jarvis.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

/** Bottom command field with text and voice input. */
public class JarvisInputBar extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JTextField inputField;
    private final JButton micButton;
    private boolean micActive;
    private Runnable onMicToggle;
    private Consumer<String> onSubmit;
    private static final Color INK = new Color(45, 53, 54);
    private static final Color ACCENT = new Color(190, 111, 87);

    public JarvisInputBar() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(16, 0, 0, 0));
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(new Color(250, 249, 246));
        bar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(224, 222, 214)), new EmptyBorder(0, 12, 0, 10)));
        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        inputField.setForeground(INK);
        inputField.setCaretColor(ACCENT);
        inputField.setBorder(BorderFactory.createEmptyBorder());
        inputField.setOpaque(false);
        inputField.putClientProperty("JTextField.placeholderText", "Try saying something...");
        inputField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) submit();
            }
        });
        micButton = new JButton("◉");
        micButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        micButton.setForeground(ACCENT);
        micButton.setBorderPainted(false);
        micButton.setContentAreaFilled(false);
        micButton.setFocusPainted(false);
        micButton.setPreferredSize(new Dimension(30, 34));
        micButton.setToolTipText("Start voice input (Ctrl+Space)");
        micButton.addActionListener(e -> {
            micActive = !micActive;
            updateMicStyle();
            if (onMicToggle != null) onMicToggle.run();
        });
        bar.add(inputField, BorderLayout.CENTER);
        bar.add(micButton, BorderLayout.EAST);
        bar.setPreferredSize(new Dimension(500, 42));
        add(bar, BorderLayout.CENTER);
    }

    private void submit() {
        String text = inputField.getText().trim();
        if (!text.isBlank() && onSubmit != null) {
            onSubmit.accept(text);
            inputField.setText("");
        }
    }

    private void updateMicStyle() {
        micButton.setForeground(micActive ? new Color(176, 75, 66) : ACCENT);
        micButton.setToolTipText(micActive ? "Stop voice input" : "Start voice input (Ctrl+Space)");
    }

    public void setOnMicToggle(Runnable r) { onMicToggle = r; }
    public void setOnSubmit(Consumer<String> c) { onSubmit = c; }
    public void setMicActive(boolean active) { micActive = active; updateMicStyle(); }
    public String getText() { return inputField.getText().trim(); }
    public void clearText() { inputField.setText(""); }
}
