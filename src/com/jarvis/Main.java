package com.jarvis;

import com.jarvis.audio.AudioPlayerService;
import com.jarvis.core.JarvisEngine;
import com.jarvis.gui.JarvisMainFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Application Entry Point for JARVIS Voice Assistant.
 * KTU S3 CSE Object-Oriented Programming (CSL 203) Mini Project
 * 
 * Engineering Team:
 * - Jobin (TLY25CS065) - Audio Pipeline & Java Sound API Lead
 * - Farsin (TLY25CS083) - OpenAI WhisperFlow & Networking Lead
 * - Sharon (TLY25CS104) - OOP Command Architecture & OS Automation Lead
 * - Theerth (TLY25CS120) - SQLite Database & Java Swing GUI Lead
 */
public class Main {

    public static void main(String[] args) {
        // Configure high-DPI scaling and a light neutral look that matches the dashboard.
        System.setProperty("sun.java2d.uiScale.enabled", "true");

        try {
            // Use Nimbus or System Look and Feel
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    UIManager.put("control", new Color(247, 246, 242));
                    UIManager.put("info", new Color(250, 249, 246));
                    UIManager.put("nimbusBase", new Color(225, 224, 215));
                    UIManager.put("nimbusBlueGrey", new Color(236, 235, 227));
                    UIManager.put("text", new Color(24, 30, 31));
                    UIManager.put("nimbusSelectionBackground", new Color(190, 111, 87));
                    break;
                }
            }
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored2) {}
        }

        SwingUtilities.invokeLater(() -> {
            System.out.println("==================================================");
            System.out.println("   JARVIS: A Java-Based Intelligent Voice Assistant");
            System.out.println("   KTU S3 CSE • OOP Mini Project (CSL 203)");
            System.out.println("   Jobin • Farsin • Sharon • Theerth");
            System.out.println("==================================================");

            // Initialize central orchestrator engine
            JarvisEngine engine = new JarvisEngine();

            // Launch Main GUI
            JarvisMainFrame frame = new JarvisMainFrame(engine);
            frame.setVisible(true);

            // Play startup sound
            AudioPlayerService.playStartupChime();

            // Shutdown hook for clean resource disposal
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("[JARVIS] Shutting down services cleanly...");
                engine.shutdown();
            }));
        });
    }
}
