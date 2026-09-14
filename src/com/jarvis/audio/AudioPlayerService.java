package com.jarvis.audio;

import com.jarvis.core.ConfigManager;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service generating futuristic sound effects and alarm tones using programmatic PCM synthesis.
 * Completely self-contained - requires no external audio asset files.
 * Demonstrates ENCAPSULATION of audio playback and background multithreading.
 * 
 * KTU S3 CSE OOP Mini Project - JARVIS
 */
public class AudioPlayerService {
    private static final ExecutorService soundPool = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Jarvis-SoundPlayer-Worker");
        t.setDaemon(true);
        return t;
    });

    /**
     * Plays the JARVIS startup futuristic dual-tone chime.
     */
    public static void playStartupChime() {
        if (!ConfigManager.getInstance().isSoundEffectsEnabled()) return;
        soundPool.submit(() -> {
            playTone(523.25, 120, 0.4); // C5
            playTone(659.25, 120, 0.5); // E5
            playTone(783.99, 140, 0.6); // G5
            playTone(1046.50, 250, 0.7); // C6 (Bright finish)
        });
    }

    /**
     * Plays an activation beep when voice recording starts.
     */
    public static void playActivationBeep() {
        if (!ConfigManager.getInstance().isSoundEffectsEnabled()) return;
        soundPool.submit(() -> {
            playTone(880.0, 90, 0.4); // A5
            playTone(1174.66, 120, 0.5); // D6
        });
    }

    /**
     * Plays a confirmation tone when a command executes successfully.
     */
    public static void playSuccessChime() {
        if (!ConfigManager.getInstance().isSoundEffectsEnabled()) return;
        soundPool.submit(() -> {
            playTone(659.25, 100, 0.4); // E5
            playTone(880.00, 160, 0.5); // A5
        });
    }

    /**
     * Plays an alert alarm for scheduled reminders.
     */
    public static void playReminderAlarm() {
        soundPool.submit(() -> {
            for (int i = 0; i < 3; i++) {
                playTone(987.77, 150, 0.8); // B5
                playTone(1318.51, 200, 0.8); // E6
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
            }
        });
    }

    /**
     * Synthesizes and plays a smooth sine-wave tone with attack and decay envelope.
     */
    public static void playTone(double frequencyHz, int durationMs, double volume) {
        try {
            float sampleRate = 44100f;
            int totalSamples = (int) (sampleRate * (durationMs / 1000.0));
            byte[] buffer = new byte[totalSamples * 2];

            for (int i = 0; i < totalSamples; i++) {
                double time = i / sampleRate;
                double angle = 2.0 * Math.PI * frequencyHz * time;

                // Envelope to eliminate audio clicks (attack & decay)
                double envelope = 1.0;
                int attack = Math.min(1000, totalSamples / 4);
                int decay = totalSamples - attack;
                if (i < attack) {
                    envelope = (double) i / attack;
                } else if (i > decay) {
                    envelope = (double) (totalSamples - i) / (totalSamples - decay);
                }

                short sample = (short) (Math.sin(angle) * 32767.0 * volume * envelope);
                buffer[i * 2] = (byte) (sample & 0xFF);
                buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
            }

            AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) return;

            try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                line.open(format);
                line.start();
                line.write(buffer, 0, buffer.length);
                line.drain();
                line.stop();
            }
        } catch (Exception e) {
            // Audio output device issue or quiet mode
        }
    }
}
