package com.jarvis.gui;

import com.jarvis.core.JarvisEngine;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Minimal application title strip. */
public class JarvisTopBar extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JLabel stateLabel = new JLabel("•••");
    private static final Color BG = new Color(239, 238, 233);

    public JarvisTopBar() {
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(207, 205, 198)),
                new EmptyBorder(0, 18, 0, 18)));
        setPreferredSize(new Dimension(0, 48));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        controls.setOpaque(false);
        for (int i = 0; i < 3; i++) {
            JLabel dot = new JLabel("●");
            dot.setForeground(new Color(199, 200, 193));
            dot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            controls.add(dot);
        }
        JLabel title = new JLabel("JARVIS Assistant", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        title.setForeground(new Color(73, 106, 127));
        stateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        stateLabel.setForeground(new Color(73, 106, 127));
        add(controls, BorderLayout.WEST);
        add(title, BorderLayout.CENTER);
        add(stateLabel, BorderLayout.EAST);
    }

    public void updateEngineState(JarvisEngine.EngineState state, String text) {
        SwingUtilities.invokeLater(() -> {
            stateLabel.setText(state == JarvisEngine.EngineState.ERROR ? "!" : "•••");
            stateLabel.setForeground(state == JarvisEngine.EngineState.ERROR ? new Color(176, 75, 66) : new Color(73, 106, 127));
            stateLabel.setToolTipText(text);
        });
    }
}
