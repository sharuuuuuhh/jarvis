package com.jarvis.audio;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service capturing microphone input using Java Sound API (TargetDataLine).
 * Encapsulates low-level hardware sound lines, buffers, and worker threads.
 * Demonstrates ENCAPSULATION and the OBSERVER PATTERN (audio level listeners).
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Jobin (TLY25CS065)
 */
public class AudioRecorderService {

    public interface AudioLevelListener {
        void onAudioLevelChanged(double level);
    }

    private TargetDataLine targetLine;
    private Thread captureThread;
    private final AtomicBoolean recording = new AtomicBoolean(false);
    private final ByteArrayOutputStream pcmBuffer = new ByteArrayOutputStream();
    private final CopyOnWriteArrayList<AudioLevelListener> listeners = new CopyOnWriteArrayList<>();

    public void addAudioLevelListener(AudioLevelListener listener) {
        listeners.add(listener);
    }

    public void removeAudioLevelListener(AudioLevelListener listener) {
        listeners.remove(listener);
    }

    public boolean isRecording() {
        return recording.get();
    }

    /**
     * Starts recording audio from the default microphone in a background worker thread.
     */
    public synchronized boolean startRecording() {
        if (recording.get()) {
            return false;
        }

        try {
            AudioFormat format = AudioFormatHelper.getStandardAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("[AudioRecorderService] Microphone line is not supported on this system.");
                return false;
            }

            targetLine = (TargetDataLine) AudioSystem.getLine(info);
            targetLine.open(format);
            targetLine.start();

            pcmBuffer.reset();
            recording.set(true);

            captureThread = new Thread(this::captureLoop, "Jarvis-Audio-Capture-Worker");
            captureThread.setDaemon(true);
            captureThread.start();

            return true;
        } catch (Exception e) {
            System.err.println("[AudioRecorderService] Could not start microphone capture: " + e.getMessage());
            if (targetLine != null) {
                try {
                    targetLine.stop();
                } catch (Exception ignored) {}
                try {
                    targetLine.close();
                } catch (Exception ignored) {}
                targetLine = null;
            }
            recording.set(false);
            return false;
        }
    }

    private void captureLoop() {
        byte[] buffer = new byte[2048];
        while (recording.get() && targetLine != null && targetLine.isOpen()) {
            int bytesRead = targetLine.read(buffer, 0, buffer.length);
            if (bytesRead > 0) {
                pcmBuffer.write(buffer, 0, bytesRead);

                // Notify listeners with calculated RMS volume level
                double level = AudioFormatHelper.calculateRmsVolume(buffer, bytesRead);
                for (AudioLevelListener listener : listeners) {
                    try {
                        listener.onAudioLevelChanged(level);
                    } catch (Exception ignored) {}
                }
            }
        }
        // Reset level to 0 when stopped
        for (AudioLevelListener listener : listeners) {
            try {
                listener.onAudioLevelChanged(0.0);
            } catch (RuntimeException e) {
                System.err.println("[AudioRecorderService] Audio-level listener failed: " + e.getMessage());
            }
        }
    }

    /**
     * Stops audio recording, closes the hardware line, and returns the captured audio as WAV bytes.
     */
    public synchronized byte[] stopRecording() {
        if (!recording.get()) {
            return new byte[0];
        }

        recording.set(false);

        if (targetLine != null) {
            try {
                targetLine.stop();
            } catch (Exception ignored) {}
            try {
                targetLine.close();
            } catch (Exception ignored) {}
        }

        if (captureThread != null) {
            try {
                captureThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        captureThread = null;
        targetLine = null;

        byte[] rawPcm = pcmBuffer.toByteArray();
        try {
            return AudioFormatHelper.convertPcmToWav(rawPcm);
        } catch (IOException e) {
            System.err.println("[AudioRecorderService] Failed to encode WAV: " + e.getMessage());
            return new byte[0];
        }
    }

    /**
     * Helper to test if a microphone device is detected.
     */
    public static boolean isMicrophoneAvailable() {
        try {
            AudioFormat format = AudioFormatHelper.getStandardAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            return AudioSystem.isLineSupported(info);
        } catch (Exception e) {
            return false;
        }
    }
}
