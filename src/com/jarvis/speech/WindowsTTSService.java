package com.jarvis.speech;

import com.jarvis.core.ConfigManager;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Native Windows Text-to-Speech implementation using Windows SAPI (System.Speech.Synthesis).
 * Invoked via background PowerShell process without requiring external native DLLs.
 * Demonstrates POLYMORPHISM and MULTITHREADING.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Sharon (TLY25CS104)
 */
public class WindowsTTSService implements TextToSpeechService {

    private final ExecutorService speechPool = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Jarvis-TTS-Worker");
        t.setDaemon(true);
        return t;
    });

    private final AtomicBoolean speaking = new AtomicBoolean(false);
    private Process currentSpeechProcess = null;
    private int rate = 1;

    public WindowsTTSService() {
        this.rate = ConfigManager.getInstance().getTtsRate();
    }

    @Override
    public void speak(String text) {
        if (text == null || text.isBlank()) return;

        // Clean text: strip markdown, urls, special characters
        final String sanitized = sanitizeTextForSpeech(text);
        if (sanitized.isBlank()) return;

        stop(); // Cancel previous speech if still running

        speechPool.submit(() -> {
            speaking.set(true);
            try {
                // Escape single quotes for PowerShell
                String escaped = sanitized.replace("'", "''");

                // PowerShell command executing System.Speech.Synthesis
                String psCommand = String.format(
                        "Add-Type -AssemblyName System.Speech; " +
                        "$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                        "$synth.Rate = %d; " +
                        "$synth.Speak('%s');",
                        rate, escaped
                );

                ProcessBuilder pb = new ProcessBuilder("powershell", "-NoProfile", "-NonInteractive", "-Command", psCommand);
                currentSpeechProcess = pb.start();
                currentSpeechProcess.waitFor();
            } catch (IOException | InterruptedException ignored) {
                // Speech interrupted or cancelled
            } finally {
                speaking.set(false);
                currentSpeechProcess = null;
            }
        });
    }

    @Override
    public synchronized void stop() {
        if (currentSpeechProcess != null && currentSpeechProcess.isAlive()) {
            currentSpeechProcess.destroyForcibly();
            currentSpeechProcess = null;
        }
        speaking.set(false);
    }

    @Override
    public boolean isSpeaking() {
        return speaking.get();
    }

    @Override
    public void setRate(int rate) {
        this.rate = Math.max(-10, Math.min(10, rate));
        ConfigManager.getInstance().setTtsRate(this.rate);
    }

    private String sanitizeTextForSpeech(String text) {
        return text.replaceAll("https?://\\S+", "web link")
                   .replaceAll("[*#_`~]", "")
                   .replaceAll("\\[([^\\]]+)\\]\\([^\\)]+\\)", "$1")
                   .replaceAll("\\s+", " ")
                   .trim();
    }
}
