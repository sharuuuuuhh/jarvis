package com.jarvis.gui;

import com.jarvis.core.JarvisEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

/**
 * Animated central JARVIS orb visualizer.
 * Renders a glowing sphere with animated wave/waveform inside,
 * concentric dashed orbit rings, and state-reactive glow.
 *
 * Matches the design from the JARVIS UI reference image.
 */
public class JarvisOrb extends JPanel {

    private double audioLevel = 0.0;
    private double smoothedLevel = 0.0;
    private double tick = 0.0;
    private JarvisEngine.EngineState engineState = JarvisEngine.EngineState.IDLE;
    private final Timer animTimer;

    // Dark theme palette
    private static final Color BG = new Color(10, 14, 26);
    private static final Color CYAN = new Color(0, 180, 255);
    private static final Color CYAN_BRIGHT = new Color(80, 210, 255);
    private static final Color CYAN_DIM = new Color(0, 100, 180, 60);
    private static final Color WAVE_1 = new Color(0, 160, 230, 180);
    private static final Color WAVE_2 = new Color(0, 220, 255, 120);
    private static final Color WAVE_3 = new Color(80, 180, 255, 100);

    public JarvisOrb() {
        setOpaque(false);
        setPreferredSize(new Dimension(300, 300));
        animTimer = new Timer(25, e -> {
            tick += 0.04;
            if (tick > Math.PI * 2000) tick = 0;
            smoothedLevel += (audioLevel - smoothedLevel) * 0.15;
            repaint();
        });
        animTimer.start();
    }

    public void setAudioLevel(double level) {
        this.audioLevel = Math.max(0.0, Math.min(1.0, level));
    }

    public void setEngineState(JarvisEngine.EngineState state) {
        this.engineState = state;
        if (state == JarvisEngine.EngineState.LISTENING) {
            audioLevel = Math.max(audioLevel, 0.3);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth(), h = getHeight();
        int cx = w / 2, cy = h / 2;
        int orbR = Math.min(w, h) / 2 - 24;

        // --- Outer dashed orbit rings ---
        drawOrbitRing(g2, cx, cy, orbR + 22, new Color(0, 150, 220, 40), 1.2f, new float[]{6, 8});
        drawOrbitRing(g2, cx, cy, orbR + 10, new Color(0, 160, 230, 55), 1.0f, new float[]{4, 6});

        // --- Orb body: radial gradient sphere ---
        drawOrbBody(g2, cx, cy, orbR);

        // --- Animated waves inside orb (clipped to orb circle) ---
        Shape orbClip = new Ellipse2D.Double(cx - orbR + 4, cy - orbR + 4, (orbR - 4) * 2, (orbR - 4) * 2);
        g2.setClip(orbClip);
        drawWaves(g2, cx, cy, orbR);
        g2.setClip(null);

        // --- Inner rim glow ---
        drawInnerRim(g2, cx, cy, orbR);

        // --- Central text ---
        drawCenterText(g2, cx, cy);

        g2.dispose();
    }

    private void drawOrbitRing(Graphics2D g2, int cx, int cy, int r, Color c, float stroke, float[] dash) {
        g2.setColor(c);
        g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, dash, (float) (tick * 8 % 20)));
        g2.draw(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
    }

    private void drawOrbBody(Graphics2D g2, int cx, int cy, int r) {
        // Outer glow halo
        for (int i = 5; i >= 1; i--) {
            int alpha = 12 + i * 5;
            g2.setColor(new Color(0, 160, 230, alpha));
            int gr = r + i * 6;
            g2.fill(new Ellipse2D.Double(cx - gr, cy - gr, gr * 2, gr * 2));
        }

        // Sphere body — dark with gradient
        RadialGradientPaint bodyPaint = new RadialGradientPaint(
                new Point2D.Float(cx - r * 0.25f, cy - r * 0.25f), r,
                new float[]{0f, 0.5f, 1.0f},
                new Color[]{new Color(30, 80, 130, 220), new Color(10, 30, 70, 240), new Color(5, 15, 40, 255)}
        );
        g2.setPaint(bodyPaint);
        g2.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));

        // Specular highlight top-left
        RadialGradientPaint specular = new RadialGradientPaint(
                new Point2D.Float(cx - r * 0.3f, cy - r * 0.35f), (int)(r * 0.55f),
                new float[]{0f, 1.0f},
                new Color[]{new Color(180, 230, 255, 55), new Color(255, 255, 255, 0)}
        );
        g2.setPaint(specular);
        g2.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
    }

    private void drawWaves(Graphics2D g2, int cx, int cy, int r) {
        double amp = 0.12 + smoothedLevel * 0.22;
        boolean isActive = engineState == JarvisEngine.EngineState.LISTENING
                || engineState == JarvisEngine.EngineState.SPEAKING;

        // Three wave layers
        drawSingleWave(g2, cx, cy, r, WAVE_1, amp * 1.0, 0.0, 0.0, 2.2f);
        drawSingleWave(g2, cx, cy, r, WAVE_2, amp * 0.75, Math.PI * 0.6, 0.3, 1.6f);
        drawSingleWave(g2, cx, cy, r, WAVE_3, amp * 0.55, Math.PI * 1.2, -0.2, 1.2f);

        if (isActive) {
            drawSingleWave(g2, cx, cy, r, new Color(120, 220, 255, 80), amp * 0.4, Math.PI * 1.8, 0.5, 1.0f);
        }
    }

    private void drawSingleWave(Graphics2D g2, int cx, int cy, int r,
                                 Color color, double amplitude, double phaseOff, double speedMult, float strokeW) {
        int pts = 160;
        int[] xs = new int[pts + 2];
        int[] ys = new int[pts + 2];

        double waveY = cy + r * 0.15; // wave center slightly below orb center

        for (int i = 0; i < pts; i++) {
            double progress = (double) i / (pts - 1);
            double x = (cx - r + 4) + progress * (r - 4) * 2;
            double wave = Math.sin(progress * Math.PI * 4 + tick * (1.2 + speedMult) + phaseOff)
                        * Math.sin(progress * Math.PI + phaseOff * 0.5)
                        * amplitude * r;
            xs[i] = (int) x;
            ys[i] = (int) (waveY + wave);
        }

        g2.setColor(color);
        g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawPolyline(xs, ys, pts);
    }

    private void drawInnerRim(Graphics2D g2, int cx, int cy, int r) {
        // Glowing rim
        g2.setStroke(new BasicStroke(2.0f));
        GradientPaint rimPaint = new GradientPaint(
                cx - r, cy, new Color(0, 160, 220, 30),
                cx + r, cy, new Color(0, 220, 255, 100)
        );
        g2.setPaint(rimPaint);
        g2.draw(new Ellipse2D.Double(cx - r + 1, cy - r + 1, (r - 1) * 2, (r - 1) * 2));

        // Bright arc at top
        g2.setColor(new Color(100, 210, 255, 120));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new Arc2D.Double(cx - r + 2, cy - r + 2, (r - 2) * 2, (r - 2) * 2, 30, 120, Arc2D.OPEN));
    }

    private void drawCenterText(Graphics2D g2, int cx, int cy) {
        // "JARVIS"
        g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
        g2.setColor(new Color(220, 240, 255, 230));
        FontMetrics fm = g2.getFontMetrics();
        String title = "JARVIS";
        g2.drawString(title, cx - fm.stringWidth(title) / 2, cy - 8);

        // Status text
        String status = getStatusText();
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2.setColor(CYAN_BRIGHT);
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(status, cx - fm2.stringWidth(status) / 2, cy + 14);
    }

    private String getStatusText() {
        return switch (engineState) {
            case LISTENING -> "LISTENING...";
            case PROCESSING -> "PROCESSING...";
            case SPEAKING -> "SPEAKING...";
            case ERROR -> "ERROR";
            default -> "READY";
        };
    }
}
