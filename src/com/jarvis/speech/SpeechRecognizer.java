package com.jarvis.speech;

/**
 * Common abstraction interface for all Speech-to-Text recognizers.
 * Demonstrates the core OOP principle of ABSTRACTION by hiding cloud HTTP or
 * native ML transcription details behind a clean API contract.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Farsin (TLY25CS083)
 */
public interface SpeechRecognizer {

    /**
     * Transcribes raw audio WAV byte data into text.
     * 
     * @param audioWavData Byte array containing 16kHz PCM WAV audio
     * @return Transcribed human speech text
     * @throws Exception if network, parsing, or audio errors occur
     */
    String transcribe(byte[] audioWavData) throws Exception;

    /**
     * Checks if this speech recognizer is currently configured and operational.
     */
    boolean isAvailable();

    /**
     * Returns the human-readable name of this speech recognizer engine.
     */
    String getEngineName();
}
