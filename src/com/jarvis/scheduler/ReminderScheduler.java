package com.jarvis.scheduler;

import com.jarvis.audio.AudioPlayerService;
import com.jarvis.database.ReminderDAO;
import com.jarvis.speech.TextToSpeechService;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Background daemon thread continuously polling the SQLite database for due reminders.
 * Rings synthesized alarm tones and triggers Text-to-Speech verbal alerts.
 * Demonstrates MULTITHREADING, DAEMON THREADS, and the OBSERVER PATTERN.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Theerth (TLY25CS120)
 */
public class ReminderScheduler {

    public interface ReminderTriggerListener {
        void onReminderDue(ReminderDAO.ReminderItem reminder);
    }

    private final ReminderDAO reminderDAO;
    private final TextToSpeechService ttsService;
    private final CopyOnWriteArrayList<ReminderTriggerListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread workerThread;

    public ReminderScheduler(ReminderDAO reminderDAO, TextToSpeechService ttsService) {
        this.reminderDAO = reminderDAO;
        this.ttsService = ttsService;
    }

    public void addListener(ReminderTriggerListener listener) {
        listeners.add(listener);
    }

    public synchronized void start() {
        if (running.get()) return;
        running.set(true);

        workerThread = new Thread(this::pollLoop, "Jarvis-Reminder-Scheduler-Daemon");
        workerThread.setDaemon(true);
        workerThread.start();
        System.out.println("[ReminderScheduler] Background polling daemon started.");
    }

    public synchronized void stop() {
        running.set(false);
        if (workerThread != null) {
            workerThread.interrupt();
        }
    }

    private void pollLoop() {
        while (running.get()) {
            try {
                List<ReminderDAO.ReminderItem> due = reminderDAO.getPendingReminders();
                for (ReminderDAO.ReminderItem item : due) {
                    // Mark as completed immediately to prevent re-triggering
                    reminderDAO.markCompleted(item.getId());

                    // 1. Play alert chime
                    AudioPlayerService.playReminderAlarm();

                    // 2. Vocalize reminder alert via TTS
                    String alertSpeech = "Attention! Reminder alert: " + item.getTitle();
                    if (ttsService != null) {
                        ttsService.speak(alertSpeech);
                    }

                    // 3. Notify GUI listeners
                    for (ReminderTriggerListener l : listeners) {
                        try {
                            l.onReminderDue(item);
                        } catch (Exception ignored) {}
                    }
                }

                Thread.sleep(1000); // Check every 1 second
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                System.err.println("[ReminderScheduler] Error during polling: " + e.getMessage());
            }
        }
    }
}
