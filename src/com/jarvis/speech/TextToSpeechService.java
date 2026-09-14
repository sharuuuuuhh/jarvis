package com.jarvis.speech;

/**
 * Common abstraction interface for voice synthesis (Text-to-Speech).
 * Demonstrates ABSTRACTION by decoupling voice generation from operating system APIs.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Sharon (TLY25CS104)
 */
public interface TextToSpeechService {

    /**
     * Synthesizes and speaks the given text out loud asynchronously.
     * 
     * @param text The sentence or response for JARVIS to vocalize
     */
    void speak(String text);

    /**
     * Immediately halts any ongoing speech playback.
     */
    void stop();

    /**
     * Checks if the synthesizer is actively speaking.
     */
    boolean isSpeaking();

    /**
     * Sets the voice speech rate / speed (-10 to 10).
     */
    void setRate(int rate);
}
