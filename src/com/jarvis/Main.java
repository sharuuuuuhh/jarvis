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
        // Configure high-DPI scaling and Dark Look & Feel
        System.setProperty("sun.java2d.uiScale.enabled", "true");

        try {
            // Use Nimbus or System Look and Feel
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    // Apply dark tweaks to Nimbus defaults
                    UIManager.put("control", new Color(13, 20, 38));
                    UIManager.put("info", new Color(20, 30, 52));
                    UIManager.put("nimbusBase", new Color(10, 15, 30));
                    UIManager.put("nimbusBlueGrey", new Color(25, 38, 65));
                    UIManager.put("text", new Color(230, 240, 255));
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
