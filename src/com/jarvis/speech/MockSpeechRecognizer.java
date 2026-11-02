package com.jarvis.speech;

/**
 * Deprecated compatibility stub. A mock must never fabricate commands from real microphone audio.
 * 
 * KTU S3 CSE OOP Mini Project - JARVIS
 */
public class MockSpeechRecognizer implements SpeechRecognizer {

    public void setManualOverrideText(String text) {
        // Retained for source compatibility with older integrations.
    }

    @Override
    public String transcribe(byte[] audioWavData) throws Exception {
        throw new UnsupportedOperationException("Mock speech recognition is disabled because it cannot transcribe microphone audio.");
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public String getEngineName() {
        return "Disabled mock speech engine";
    }
}
