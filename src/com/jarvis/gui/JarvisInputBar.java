package com.jarvis.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

/**
 * Bottom voice input bar for JARVIS redesigned UI.
 * Shows animated waveform icon, "Speak to JARVIS..." placeholder,
 * keyboard shortcut hint, and mic button.
 * Matches the reference image bottom input bar design.
 */
public class JarvisInputBar extends JPanel {

    private final JTextField inputField;
    private final JButton micButton;
    private boolean micActive = false;
    private final Timer waveTimer;
    private double waveTick = 0;

    private static final Color BG = new Color(10, 14, 26);
    private static final Color INPUT_BG = new Color(16, 22, 40);
    private static final Color INPUT_BORDER = new Color(35, 55, 100);
    private static final Color INPUT_BORDER_FOCUS = new Color(50, 130, 220);
    private static final Color MIC_BG = new Color(40, 110, 220);
    private static final Color MIC_BG_ACTIVE = new Color(220, 80, 40);
    private static final Color TEXT_MAIN = new Color(210, 225, 250);
    private static final Color TEXT_DIM = new Color(90, 115, 160);
    private static final Color SHORTCUT_BG = new Color(22, 32, 58);
    private static final Color SHORTCUT_TEXT = new Color(100, 130, 180);

    private Runnable onMicToggle;
    private Consumer<String> onSubmit;

    public JarvisInputBar() {
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(10, 28, 18, 28));

        JPanel bar = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                g2.setColor(micActive ? INPUT_BORDER_FOCUS : INPUT_BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 50, 50);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 18, 0, 8));

        // Wave icon (left)
        WaveIconPanel waveIcon = new WaveIconPanel();
        waveIcon.setPreferredSize(new Dimension(36, 44));

        // Text field
        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setForeground(TEXT_MAIN);
        inputField.setBackground(new Color(0, 0, 0, 0));
        inputField.setCaretColor(new Color(60, 160, 255));
        inputField.setBorder(BorderFactory.createEmptyBorder());
        inputField.setOpaque(false);
        inputField.putClientProperty("JTextField.placeholderText", "Speak to JARVIS...");

        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String text = inputField.getText().trim();
                    if (!text.isBlank() && onSubmit != null) {
                        onSubmit.accept(text);
                        inputField.setText("");
                    }
                }
            }
        });

        // Keyboard shortcut badge
        JLabel shortcutBadge = new JLabel("Ctrl + Space");
        shortcutBadge.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        shortcutBadge.setForeground(SHORTCUT_TEXT);
        shortcutBadge.setOpaque(true);
        shortcutBadge.setBackground(SHORTCUT_BG);
        shortcutBadge.setBorder(new EmptyBorder(4, 10, 4, 10));

        bar.add(waveIcon, BorderLayout.WEST);
        bar.add(inputField, BorderLayout.CENTER);
        bar.add(shortcutBadge, BorderLayout.EAST);

        // Mic button (circular)
        micButton = new JButton("🎤") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Glow
                if (micActive) {
                    g2.setColor(new Color(220, 80, 40, 40));
                    g2.fillOval(-4, -4, getWidth() + 8, getHeight() + 8);
                }
                g2.setColor(micActive ? MIC_BG_ACTIVE : MIC_BG);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        micButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        micButton.setForeground(Color.WHITE);
        micButton.setContentAreaFilled(false);
        micButton.setFocusPainted(false);
        micButton.setBorderPainted(false);
        micButton.setOpaque(false);
        micButton.setPreferredSize(new Dimension(46, 46));
        micButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        micButton.setToolTipText("Click to speak (Ctrl+Space)");

        micButton.addActionListener(e -> {
            micActive = !micActive;
            micButton.repaint();
            bar.repaint();
            if (onMicToggle != null) onMicToggle.run();
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(micButton);

        // Full bar wrapper
        JPanel wrapper = new JPanel(new BorderLayout(10, 0));
        wrapper.setOpaque(false);
        wrapper.add(bar, BorderLayout.CENTER);
        wrapper.add(rightPanel, BorderLayout.EAST);

        add(wrapper, BorderLayout.CENTER);

        // Wave animation
        waveTimer = new Timer(40, e -> {
            waveTick += 0.1;
            waveIcon.setTick(waveTick, micActive);
            waveIcon.repaint();
        });
        waveTimer.start();
    }

    public void setOnMicToggle(Runnable r) { this.onMicToggle = r; }
    public void setOnSubmit(Consumer<String> c) { this.onSubmit = c; }

    public void setMicActive(boolean active) {
        this.micActive = active;
        micButton.repaint();
        repaint();
    }

    public String getText() { return inputField.getText().trim(); }
    public void clearText() { inputField.setText(""); }

    /**
     * Small wave icon panel drawn with custom painting.
     */
    static class WaveIconPanel extends JPanel {
        private double tick = 0;
        private boolean active = false;
        WaveIconPanel() { setOpaque(false); }
        void setTick(double t, boolean a) { tick = t; active = a; }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int bars = 5;
            int bw = 3;
            int gap = 3;
            int totalW = bars * bw + (bars - 1) * gap;
            int startX = (getWidth() - totalW) / 2;
            int cy = getHeight() / 2;
            Color barColor = active ? new Color(60, 160, 255) : new Color(80, 110, 160);
            g2.setColor(barColor);
            for (int i = 0; i < bars; i++) {
                double h = 4 + Math.abs(Math.sin(tick + i * 0.7)) * (active ? 14 : 6);
                int x = startX + i * (bw + gap);
                g2.fillRoundRect(x, cy - (int)(h / 2), bw, (int) h, 2, 2);
            }
            g2.dispose();
        }
    }
}
