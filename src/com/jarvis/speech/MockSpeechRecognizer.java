package com.jarvis.speech;

import java.util.Random;

/**
 * Offline simulated speech recognizer used when an OpenAI API key is unavailable,
 * network is disconnected, or offline evaluation mode is active.
 * Demonstrates POLYMORPHISM by swapping with WhisperApiClient seamlessly.
 * 
 * KTU S3 CSE OOP Mini Project - JARVIS
 */
public class MockSpeechRecognizer implements SpeechRecognizer {

    private static final String[] SAMPLE_COMMANDS = {
        "open notepad",
        "open calculator",
        "system info",
        "what time is it",
        "remind me to take notes in 10 seconds",
        "search google for KTU CSE syllabus",
        "help"
    };

    private final Random random = new Random();
    private String manualOverrideText = null;

    public void setManualOverrideText(String text) {
        this.manualOverrideText = text;
    }

    @Override
    public String transcribe(byte[] audioWavData) throws Exception {
        // Simulate speech recognition latency
        Thread.sleep(600);

        if (manualOverrideText != null && !manualOverrideText.isBlank()) {
            String text = manualOverrideText;
            manualOverrideText = null;
            return text;
        }

        // Return a realistic sample command based on audio presence
        int idx = random.nextInt(SAMPLE_COMMANDS.length);
        return SAMPLE_COMMANDS[idx];
    }

    @Override
    public boolean isAvailable() {
        return true; // Always available as offline fallback
    }

    @Override
    public String getEngineName() {
        return "Offline Simulated Speech Engine (Lab Mode)";
    }
}
