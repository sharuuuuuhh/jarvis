package com.jarvis.core;

import com.jarvis.audio.AudioPlayerService;
import com.jarvis.audio.AudioRecorderService;
import com.jarvis.commands.*;
import com.jarvis.database.InteractionLogDAO;
import com.jarvis.database.ReminderDAO;
import com.jarvis.scheduler.ReminderScheduler;
import com.jarvis.speech.TextToSpeechService;
import com.jarvis.speech.WhisperFlowService;
import com.jarvis.speech.WindowsTTSService;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Central orchestrator coordinating audio capture, OpenAI WhisperFlow transcription,
 * polymorphic command routing, database persistence, and voice synthesis.
 * Demonstrates the MEDIATOR DESIGN PATTERN and HIGH COHESION / LOW COUPLING.
 * 
 * KTU S3 CSE OOP Mini Project - JARVIS
 */
public class JarvisEngine {

    public enum EngineState {
        IDLE,
        LISTENING,
        PROCESSING,
        SPEAKING,
        ERROR
    }

    public interface EngineListener {
        void onStateChanged(EngineState state, String statusText);
        void onUserUtterance(String text);
        void onAssistantResponse(CommandResult result, String intent);
        void onAudioLevel(double level);
        void onReminderAlert(ReminderDAO.ReminderItem item);
    }

    private final AudioRecorderService audioRecorder;
    private final WhisperFlowService whisperFlow;
    private final TextToSpeechService ttsService;
    private final CommandRegistry commandRegistry;
    private final CommandFactory commandFactory;
    private final ReminderDAO reminderDAO;
    private final ReminderScheduler reminderScheduler;
    private final InteractionLogDAO logDAO;

    private EngineState currentState = EngineState.IDLE;
    private final CopyOnWriteArrayList<EngineListener> listeners = new CopyOnWriteArrayList<>();

    private final ExecutorService asyncPool = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Jarvis-Engine-Task");
        t.setDaemon(true);
        return t;
    });

    public JarvisEngine() {
        this.audioRecorder = new AudioRecorderService();
        this.whisperFlow = new WhisperFlowService(audioRecorder);
        this.ttsService = new WindowsTTSService();
        this.commandRegistry = new CommandRegistry();
        this.commandFactory = new CommandFactory(commandRegistry);
        this.reminderDAO = new ReminderDAO();
        this.reminderScheduler = new ReminderScheduler(reminderDAO, ttsService);
        this.logDAO = new InteractionLogDAO();

        initListeners();
        reminderScheduler.start();
    }

    private void initListeners() {
        // Forward audio volume levels to visualizer
        audioRecorder.addAudioLevelListener(level -> {
            for (EngineListener l : listeners) {
                try {
                    l.onAudioLevel(level);
                } catch (Exception ignored) {}
            }
        });

        // Handle WhisperFlow transcription events
        whisperFlow.addListener(new WhisperFlowService.WhisperFlowListener() {
            @Override
            public void onStateChanged(WhisperFlowService.FlowState state, String statusMessage) {
                switch (state) {
                    case LISTENING -> setState(EngineState.LISTENING, "WhisperFlow: Listening...");
                    case TRANSCRIBING -> setState(EngineState.PROCESSING, "WhisperFlow: Transcribing via OpenAI...");
                    case ERROR -> setState(EngineState.ERROR, statusMessage);
                    case IDLE -> {
                        if (currentState != EngineState.SPEAKING) {
                            setState(EngineState.IDLE, "Online & Ready");
                        }
                    }
                }
            }

            @Override
            public void onTranscriptReceived(String transcript) {
                processTranscript(transcript);
            }

            @Override
            public void onError(String errorMessage) {
                setState(EngineState.ERROR, "Error: " + errorMessage);
            }
        });

        // Forward reminder alarms
        reminderScheduler.addListener(item -> {
            for (EngineListener l : listeners) {
                try {
                    l.onReminderAlert(item);
                } catch (Exception ignored) {}
            }
        });
    }

    public void addListener(EngineListener listener) {
        listeners.add(listener);
    }

    public void removeListener(EngineListener listener) {
        listeners.remove(listener);
    }

    public EngineState getState() {
        return currentState;
    }

    private synchronized void setState(EngineState state, String statusText) {
        this.currentState = state;
        for (EngineListener l : listeners) {
            try {
                l.onStateChanged(state, statusText);
            } catch (Exception ignored) {}
        }
    }

    /**
     * Toggles Push-to-Talk speech recording.
     */
    public synchronized void toggleVoiceRecording() {
        if (currentState == EngineState.PROCESSING || currentState == EngineState.SPEAKING) {
            return;
        }
        if (whisperFlow.isPushToTalkRecording()) {
            whisperFlow.stopPushToTalkAndTranscribe();
        } else {
            AudioPlayerService.playActivationBeep();
            whisperFlow.startPushToTalk();
        }
    }

    public boolean isRecording() {
        return whisperFlow.isPushToTalkRecording();
    }

    /**
     * Dispatches user text (typed in GUI or transcribed by Whisper) into the command pipeline.
     */
    public void processTextInput(String text) {
        if (text == null || text.isBlank()) return;
        processTranscript(text.trim());
    }

    private void processTranscript(String userText) {
        asyncPool.submit(() -> {
            setState(EngineState.PROCESSING, "Parsing intent...");

            // 1. Notify listeners of user speech
            for (EngineListener l : listeners) {
                try {
                    l.onUserUtterance(userText);
                } catch (Exception ignored) {}
            }

            // 2. Parse text into intent and arguments
            CommandParser.ParsedIntent parsed = CommandParser.parse(userText);
            CommandContext context = new CommandContext(userText, parsed.getIntent(), parsed.getArgument(), parsed.getParameters());

            // 3. Resolve polymorphic Command via CommandFactory
            Command command = commandFactory.getCommand(parsed.getIntent(), userText);

            // 4. Execute Command polymorphically
            CommandResult result = command.execute(context);

            if (result.isSuccess()) {
                AudioPlayerService.playSuccessChime();
            }

            // 5. Notify GUI listeners with the response
            for (EngineListener l : listeners) {
                try {
                    l.onAssistantResponse(result, command.getName());
                } catch (Exception ignored) {}
            }

            // 6. Speak response via Windows TTS
            if (ConfigManager.getInstance().isTtsEnabled() && !result.getSpokenResponse().isBlank()) {
                setState(EngineState.SPEAKING, "JARVIS Speaking...");
                ttsService.speak(result.getSpokenResponse(), () -> {
                    if (!ttsService.isSpeaking()) setState(EngineState.IDLE, "Online & Ready");
                });
            } else {
                setState(EngineState.IDLE, "Online & Ready");
            }
        });
    }

    public WhisperFlowService getWhisperFlow() {
        return whisperFlow;
    }

    public TextToSpeechService getTtsService() {
        return ttsService;
    }

    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    public ReminderDAO getReminderDAO() {
        return reminderDAO;
    }

    public void shutdown() {
        reminderScheduler.stop();
        ttsService.stop();
        asyncPool.shutdownNow();
    }
}
