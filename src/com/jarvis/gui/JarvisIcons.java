package com.jarvis.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/** Small, consistent line icons used across every JARVIS page. */
public final class JarvisIcons {
    private JarvisIcons() {}

    public static Icon of(String name, Color color, int size) {
        return new LineIcon(name, color, size);
    }

    private static final class LineIcon implements Icon {
        private final String name;
        private final Color color;
        private final int size;
        private LineIcon(String name, Color color, int size) {
            this.name = name;
            this.color = color;
            this.size = size;
        }
        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }

        @Override public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.translate(x, y);
            g.scale(size / 24.0, size / 24.0);
            g.setColor(color);
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (name) {
                case "home" -> home(g);
                case "activity" -> activity(g);
                case "reminders" -> reminders(g);
                case "apps" -> apps(g);
                case "settings" -> settings(g);
                case "voice" -> voice(g);
                case "assistant" -> assistant(g);
                case "notepad" -> notepad(g);
                case "calculator" -> calculator(g);
                case "system" -> system(g);
                case "web" -> web(g);
                case "time" -> time(g);
                case "help" -> help(g);
                default -> assistant(g);
            }
            g.dispose();
        }

        private void home(Graphics2D g) {
            Path2D roof = new Path2D.Double();
            roof.moveTo(3.5, 11); roof.lineTo(12, 4); roof.lineTo(20.5, 11);
            g.draw(roof);
            g.drawRoundRect(6, 10, 12, 10, 2, 2);
            g.drawLine(10, 20, 10, 14); g.drawLine(14, 20, 14, 14);
        }
        private void activity(Graphics2D g) {
            g.draw(new Ellipse2D.Double(3.5, 3.5, 17, 17));
            g.drawLine(12, 7, 12, 12); g.drawLine(12, 12, 16, 14);
            g.drawArc(1, 8, 5, 8, 55, 250);
        }
        private void reminders(Graphics2D g) {
            g.drawRoundRect(4, 5, 16, 15, 2, 2);
            g.drawLine(8, 3, 8, 7); g.drawLine(16, 3, 16, 7);
            g.drawLine(4, 9, 20, 9); g.drawLine(8, 13, 10, 15); g.drawLine(10, 15, 15, 12);
        }
        private void apps(Graphics2D g) {
            g.drawRoundRect(4, 4, 7, 7, 2, 2); g.drawRoundRect(13, 4, 7, 7, 2, 2);
            g.drawRoundRect(4, 13, 7, 7, 2, 2); g.drawRoundRect(13, 13, 7, 7, 2, 2);
        }
        private void settings(Graphics2D g) {
            g.draw(new Ellipse2D.Double(8, 8, 8, 8));
            for (int i = 0; i < 8; i++) {
                double a = i * Math.PI / 4;
                int x1 = (int) Math.round(12 + Math.cos(a) * 5.2), y1 = (int) Math.round(12 + Math.sin(a) * 5.2);
                int x2 = (int) Math.round(12 + Math.cos(a) * 8), y2 = (int) Math.round(12 + Math.sin(a) * 8);
                g.drawLine(x1, y1, x2, y2);
            }
        }
        private void voice(Graphics2D g) {
            g.drawRoundRect(9, 3, 6, 12, 3, 3);
            g.drawArc(5, 8, 14, 11, 180, 180);
            g.drawLine(12, 19, 12, 22); g.drawLine(8, 22, 16, 22);
        }
        private void assistant(Graphics2D g) {
            g.drawRoundRect(3, 5, 18, 14, 3, 3);
            Path2D tail = new Path2D.Double();
            tail.moveTo(8, 19); tail.lineTo(6, 22); tail.lineTo(13, 19);
            g.draw(tail);
            g.drawLine(7, 10, 17, 10); g.drawLine(7, 14, 14, 14);
        }
        private void notepad(Graphics2D g) {
            g.drawRoundRect(5, 3, 14, 18, 2, 2);
            g.drawLine(8, 8, 16, 8); g.drawLine(8, 12, 16, 12); g.drawLine(8, 16, 14, 16);
        }
        private void calculator(Graphics2D g) {
            g.drawRoundRect(5, 3, 14, 18, 2, 2); g.drawRoundRect(8, 6, 8, 4, 1, 1);
            g.drawLine(8, 13, 9, 13); g.drawLine(12, 13, 13, 13); g.drawLine(16, 13, 16, 13);
            g.drawLine(8, 17, 9, 17); g.drawLine(12, 17, 13, 17); g.drawLine(16, 17, 16, 17);
        }
        private void system(Graphics2D g) {
            g.drawRoundRect(3, 4, 18, 13, 2, 2);
            g.drawLine(8, 21, 16, 21); g.drawLine(12, 17, 12, 21);
            g.drawLine(7, 13, 10, 10); g.drawLine(10, 10, 13, 12); g.drawLine(13, 12, 17, 8);
        }
        private void web(Graphics2D g) {
            g.draw(new Ellipse2D.Double(3, 3, 14, 14));
            g.drawLine(16, 16, 21, 21);
            g.draw(new Ellipse2D.Double(7, 3, 6, 14));
            g.drawArc(3, 7, 14, 6, 0, 180); g.drawArc(3, 7, 14, 6, 180, 180);
        }
        private void time(Graphics2D g) {
            g.draw(new Ellipse2D.Double(3.5, 3.5, 17, 17));
            g.drawLine(12, 7, 12, 12); g.drawLine(12, 12, 16, 12);
        }
        private void help(Graphics2D g) {
            g.draw(new Ellipse2D.Double(3.5, 3.5, 17, 17));
            g.drawArc(8, 7, 8, 7, 0, 180); g.drawLine(12, 14, 12, 16);
            g.fill(new Ellipse2D.Double(11.2, 18, 1.6, 1.6));
        }
    }
}
