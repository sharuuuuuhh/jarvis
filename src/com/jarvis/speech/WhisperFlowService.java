package com.jarvis.speech;

import com.jarvis.audio.AudioFormatHelper;
import com.jarvis.audio.AudioRecorderService;
import com.jarvis.core.ConfigManager;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * OpenAI WhisperFlow Pipeline Service.
 * Implements continuous and push-to-talk voice stream ingestion, Voice Activity Detection (VAD),
 * audio buffer chunking, and dispatching to the OpenAI Whisper API.
 * Demonstrates ENCAPSULATION, OBSERVER PATTERN, and MULTITHREADING.
 * 
 * KTU S3 CSE OOP Mini Project - JARVIS WhisperFlow Engine
 */
public class WhisperFlowService {

    public enum FlowMode {
        PUSH_TO_TALK,
        CONTINUOUS_FLOW
    }

    public enum FlowState {
        IDLE,
        LISTENING,
        TRANSCRIBING,
        ERROR
    }

    public interface WhisperFlowListener {
        void onStateChanged(FlowState state, String statusMessage);
        void onTranscriptReceived(String transcript);
        void onError(String errorMessage);
    }

    private final AudioRecorderService audioRecorder;
    private final WhisperApiClient whisperApiClient;
    private final CopyOnWriteArrayList<WhisperFlowListener> listeners = new CopyOnWriteArrayList<>();

    private FlowMode currentMode = FlowMode.PUSH_TO_TALK;
    private FlowState currentState = FlowState.IDLE;
    private final AtomicBoolean active = new AtomicBoolean(false);

    private final ExecutorService workerPool = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "Jarvis-WhisperFlow-Worker");
        t.setDaemon(true);
        return t;
    });

    public WhisperFlowService(AudioRecorderService audioRecorder) {
        this.audioRecorder = audioRecorder;
        this.whisperApiClient = new WhisperApiClient();

        // Check configured flow mode
        if (ConfigManager.getInstance().isContinuousWhisperFlow()) {
            this.currentMode = FlowMode.CONTINUOUS_FLOW;
        }
    }

    public void addListener(WhisperFlowListener listener) {
        listeners.add(listener);
    }

    public void removeListener(WhisperFlowListener listener) {
        listeners.remove(listener);
    }

    public FlowMode getMode() {
        return currentMode;
    }

    public void setMode(FlowMode mode) {
        this.currentMode = mode;
        ConfigManager.getInstance().setContinuousWhisperFlow(mode == FlowMode.CONTINUOUS_FLOW);
    }

    public FlowState getState() {
        return currentState;
    }

    private void setState(FlowState state, String message) {
        this.currentState = state;
        for (WhisperFlowListener l : listeners) {
            try {
                l.onStateChanged(state, message);
            } catch (Exception ignored) {}
        }
    }

    /**
     * Starts listening in Push-To-Talk mode.
     */
    public synchronized boolean startPushToTalk() {
        if (active.get()) return false;
        boolean started = audioRecorder.startRecording();
        if (started) {
            active.set(true);
            setState(FlowState.LISTENING, "WhisperFlow: Listening to microphone...");
        } else {
            setState(FlowState.ERROR, "Failed to initialize microphone hardware.");
        }
        return started;
    }

    /**
     * Stops listening in Push-To-Talk mode and triggers OpenAI Whisper transcription.
     */
    public synchronized void stopPushToTalkAndTranscribe() {
        if (!active.get()) return;
        active.set(false);

        setState(FlowState.TRANSCRIBING, "WhisperFlow: Transcribing audio stream...");

        workerPool.submit(() -> {
            try {
                byte[] wavData = audioRecorder.stopRecording();
                if (wavData.length < 500) {
                    setState(FlowState.IDLE, "Ready");
                    return;
                }

                String transcript = performTranscription(wavData);
                setState(FlowState.IDLE, "Ready");

                if (transcript != null && !transcript.isBlank()) {
                    for (WhisperFlowListener l : listeners) {
                        try {
                            l.onTranscriptReceived(transcript.trim());
                        } catch (Exception ignored) {}
                    }
                }
            } catch (Exception e) {
                setState(FlowState.ERROR, "Transcription Error: " + e.getMessage());
                for (WhisperFlowListener l : listeners) {
                    try {
                        l.onError(e.getMessage());
                    } catch (Exception ignored) {}
                }
            }
        });
    }

    /** Dispatches audio only to real transcription; never invents commands for microphone input. */
    private String performTranscription(byte[] wavData) throws Exception {
        if (!whisperApiClient.isAvailable()) {
            throw new IllegalStateException("Voice recognition is unavailable. Add a Groq API key in Settings and turn off Offline lab simulation mode.");
        }
        System.out.println("[WhisperFlow] Sending microphone audio to " + whisperApiClient.getEngineName() + "...");
        return whisperApiClient.transcribe(wavData);
    }

    /**
     * Direct transcription of arbitrary WAV data.
     */
    public String transcribeDirect(byte[] wavData) throws Exception {
        return performTranscription(wavData);
    }

    public boolean isPushToTalkRecording() {
        return active.get() && audioRecorder.isRecording();
    }
}
