package com.jarvis.gui;

import com.jarvis.core.JarvisEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * Custom Swing component rendering an animated glowing Stark Arc Reactor.
 * Reacts to microphone audio volume and assistant speaking state in real time.
 * Demonstrates EVENT-DRIVEN GUI PROGRAMMING, 2D GRAPHICS, and ANIMATION.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Theerth (TLY25CS120)
 */
public class ArcReactorVisualizer extends JPanel {

    private double audioLevel = 0.0;
    private double smoothedLevel = 0.0;
    private double rotationAngle = 0.0;
    private JarvisEngine.EngineState engineState = JarvisEngine.EngineState.IDLE;
    private final Timer animationTimer;

    // Palette
    private static final Color BG_COLOR = new Color(10, 15, 29);
    private static final Color CYAN_CORE = new Color(0, 240, 255);
    private static final Color CYAN_GLOW = new Color(0, 200, 255, 60);
    private static final Color ACTIVE_ORANGE = new Color(255, 140, 0);
    private static final Color ERROR_RED = new Color(255, 60, 60);

    public ArcReactorVisualizer() {
        setPreferredSize(new Dimension(180, 180));
        setMinimumSize(new Dimension(140, 140));
        setBackground(BG_COLOR);
        setOpaque(true);

        // 30 FPS animation loop
        animationTimer = new Timer(33, e -> {
            rotationAngle += 0.03;
            if (rotationAngle >= Math.PI * 2) {
                rotationAngle = 0;
            }

            // Smooth interpolation of audio level
            smoothedLevel += (audioLevel - smoothedLevel) * 0.3;
            repaint();
        });
        animationTimer.start();
    }

    public void setAudioLevel(double level) {
        this.audioLevel = Math.max(0.0, Math.min(1.0, level));
    }

    public void setEngineState(JarvisEngine.EngineState state) {
        this.engineState = state;
        if (state == JarvisEngine.EngineState.LISTENING) {
            this.audioLevel = Math.max(this.audioLevel, 0.2);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int baseRadius = Math.min(width, height) / 2 - 15;

        // Dynamic pulse factor based on audio volume or state
        double pulse = 1.0 + (smoothedLevel * 0.35);
        if (engineState == JarvisEngine.EngineState.LISTENING) {
            pulse += Math.sin(rotationAngle * 3) * 0.08;
        } else if (engineState == JarvisEngine.EngineState.SPEAKING) {
            pulse += Math.sin(rotationAngle * 5) * 0.12;
        }

        int currentRadius = (int) (baseRadius * pulse);

        // State-based accent color
        Color primaryColor;
        switch (engineState) {
            case LISTENING -> primaryColor = ACTIVE_ORANGE;
            case ERROR -> primaryColor = ERROR_RED;
            case SPEAKING -> primaryColor = new Color(0, 255, 170);
            default -> primaryColor = CYAN_CORE;
        }

        // 1. Outer Glow Aura
        g2.setColor(new Color(primaryColor.getRed(), primaryColor.getGreen(), primaryColor.getBlue(), 30));
        g2.fill(new Ellipse2D.Double(centerX - currentRadius - 8, centerY - currentRadius - 8, (currentRadius + 8) * 2, (currentRadius + 8) * 2));

        // 2. Outer Rotating Ring with Segments
        g2.setColor(new Color(primaryColor.getRed(), primaryColor.getGreen(), primaryColor.getBlue(), 120));
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int segments = 12;
        for (int i = 0; i < segments; i++) {
            double angle = rotationAngle + (i * (Math.PI * 2 / segments));
            int x1 = (int) (centerX + Math.cos(angle) * (currentRadius - 6));
            int y1 = (int) (centerY + Math.sin(angle) * (currentRadius - 6));
            int x2 = (int) (centerX + Math.cos(angle) * currentRadius);
            int y2 = (int) (centerY + Math.sin(angle) * currentRadius);
            g2.drawLine(x1, y1, x2, y2);
        }

        // 3. Middle Circular Track
        g2.setColor(new Color(primaryColor.getRed(), primaryColor.getGreen(), primaryColor.getBlue(), 80));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new Ellipse2D.Double(centerX - currentRadius + 10, centerY - currentRadius + 10, (currentRadius - 10) * 2, (currentRadius - 10) * 2));

        // 4. Counter-rotating inner triangles / ticks
        int innerTicks = 6;
        for (int i = 0; i < innerTicks; i++) {
            double angle = -rotationAngle * 1.5 + (i * (Math.PI * 2 / innerTicks));
            int tx = (int) (centerX + Math.cos(angle) * (currentRadius * 0.55));
            int ty = (int) (centerY + Math.sin(angle) * (currentRadius * 0.55));
            g2.setColor(primaryColor);
            g2.fillOval(tx - 3, ty - 3, 6, 6);
        }

        // 5. Glowing Central Core (Arc Reactor)
        int coreRadius = (int) (currentRadius * 0.35);
        RadialGradientPaint corePaint = new RadialGradientPaint(
                centerX, centerY, coreRadius,
                new float[]{0.0f, 0.6f, 1.0f},
                new Color[]{Color.WHITE, primaryColor, new Color(primaryColor.getRed(), primaryColor.getGreen(), primaryColor.getBlue(), 0)}
        );
        g2.setPaint(corePaint);
        g2.fill(new Ellipse2D.Double(centerX - coreRadius, centerY - coreRadius, coreRadius * 2, coreRadius * 2));

        // 6. Central Status Text
        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        g2.setColor(Color.WHITE);
        String label = engineState.name();
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(label);
        g2.drawString(label, centerX - (textWidth / 2), centerY + 4);

        g2.dispose();
    }
}
