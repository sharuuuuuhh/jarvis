package com.jarvis.audio;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Utility class encapsulating standard audio formats and WAV conversions.
 * Configured specifically for OpenAI Whisper requirements: 16kHz, 16-bit, Mono PCM.
 * Demonstrates ENCAPSULATION of complex audio hardware formats.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Jobin (TLY25CS065)
 */
public final class AudioFormatHelper {
    public static final float SAMPLE_RATE = 16000.0f;
    public static final int SAMPLE_SIZE_IN_BITS = 16;
    public static final int CHANNELS = 1; // Mono
    public static final boolean SIGNED = true;
    public static final boolean BIG_ENDIAN = false; // Little-endian for WAV

    private AudioFormatHelper() {
        // Utility class
    }

    public static AudioFormat getStandardAudioFormat() {
        return new AudioFormat(
                SAMPLE_RATE,
                SAMPLE_SIZE_IN_BITS,
                CHANNELS,
                SIGNED,
                BIG_ENDIAN
        );
    }

    /**
     * Converts raw PCM bytes into a standard RIFF/WAV byte array with proper headers.
     */
    public static byte[] convertPcmToWav(byte[] pcmData) throws IOException {
        AudioFormat format = getStandardAudioFormat();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(pcmData);
             AudioInputStream ais = new AudioInputStream(bais, format, pcmData.length / format.getFrameSize());
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, baos);
            return baos.toByteArray();
        }
    }

    /**
     * Calculates the RMS (Root Mean Square) volume level from 16-bit audio samples (0.0 to 1.0).
     * Used for real-time visualizer animations and Voice Activity Detection (VAD).
     */
    public static double calculateRmsVolume(byte[] audioBytes, int bytesRead) {
        if (bytesRead < 2) return 0.0;
        long sum = 0;
        int sampleCount = bytesRead / 2;
        for (int i = 0; i < bytesRead - 1; i += 2) {
            // Little-endian 16-bit sample
            short sample = (short) ((audioBytes[i + 1] << 8) | (audioBytes[i] & 0xFF));
            sum += (long) sample * sample;
        }
        double rms = Math.sqrt((double) sum / sampleCount);
        // Normalize to roughly 0.0 - 1.0 range
        return Math.min(1.0, rms / 15000.0);
    }
}
