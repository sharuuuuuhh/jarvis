package com.jarvis.speech;

import com.jarvis.core.ConfigManager;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
    private final java.util.concurrent.atomic.AtomicInteger pendingSpeech = new java.util.concurrent.atomic.AtomicInteger();
    private volatile Process currentSpeechProcess = null;
    private int rate = 1;

    public WindowsTTSService() {
        this.rate = ConfigManager.getInstance().getTtsRate();
    }

    @Override
    public void speak(String text) {
        speak(text, null);
    }

    @Override
    public void speak(String text, Runnable onComplete) {
        if (text == null || text.isBlank()) {
            if (onComplete != null) onComplete.run();
            return;
        }

        // Clean text: strip markdown, urls, special characters
        final String sanitized = sanitizeTextForSpeech(text);
        if (sanitized.isBlank()) {
            if (onComplete != null) onComplete.run();
            return;
        }

        pendingSpeech.incrementAndGet();
        speaking.set(true);
        speechPool.submit(() -> {
            try {
                String psCommand = String.format(
                        "Add-Type -AssemblyName System.Speech; " +
                        "$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                        "$synth.Rate = %d; " +
                        "$synth.Speak(%s); $synth.Dispose();",
                        rate, quoteForPowerShell(sanitized)
                );
                String encodedCommand = Base64.getEncoder().encodeToString(psCommand.getBytes(StandardCharsets.UTF_16LE));
                ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-EncodedCommand", encodedCommand);
                pb.redirectErrorStream(true);
                currentSpeechProcess = pb.start();
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                currentSpeechProcess.getInputStream().transferTo(output);
                int exit = currentSpeechProcess.waitFor();
                if (exit != 0) {
                    System.err.println("[JARVIS TTS] PowerShell speech failed: " + output.toString(StandardCharsets.UTF_8).trim());
                }
            } catch (IOException e) {
                System.err.println("[JARVIS TTS] Could not start Windows speech: " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                boolean stillSpeaking = pendingSpeech.decrementAndGet() > 0;
                speaking.set(stillSpeaking);
                currentSpeechProcess = null;
                if (onComplete != null) onComplete.run();
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

    private String quoteForPowerShell(String value) {
        return "'" + value.replace("'", "''") + "'";
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
