package com.jarvis.commands;

import com.jarvis.core.CommandContext;
import com.jarvis.core.CommandResult;
import com.jarvis.database.ReminderDAO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command parsing and scheduling reminders and timers into the persistent database.
 * Demonstrates INHERITANCE and integration with the Data Access Layer (ReminderDAO).
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Sharon (TLY25CS104)
 */
public class ReminderCommand extends AbstractCommand {

    private final ReminderDAO reminderDAO = new ReminderDAO();
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ReminderCommand() {
        super("Reminder Manager", "Schedules persistent reminders and timer notifications.",
                new String[]{"remind me to study in 10 seconds", "set a reminder for meeting in 5 minutes", "timer 30 seconds"});
    }

    @Override
    public boolean canHandle(String intent, String rawInput) {
        if ("REMINDER".equalsIgnoreCase(intent) || "TIMER".equalsIgnoreCase(intent)) return true;
        String lower = rawInput.toLowerCase();
        return lower.startsWith("remind me") || lower.startsWith("set reminder") || lower.startsWith("timer ");
    }

    @Override
    protected CommandResult doExecute(CommandContext context) throws Exception {
        String input = context.getRawInput().toLowerCase().trim();

        int secondsOffset = 10; // default 10 seconds for demo
        String taskTitle = "Generic Reminder";

        // Regex pattern: "remind me to (.*?) in (\\d+)\\s*(second|seconds|sec|minute|minutes|min|hour|hours)"
        Pattern p1 = Pattern.compile("remind me to (.+?) in (\\d+)\\s*(second|seconds|sec|minute|minutes|min|hour|hours)?");
        Matcher m1 = p1.matcher(input);

        // Regex pattern: "timer (\\d+)\\s*(second|seconds|sec|minute|minutes|min)"
        Pattern p2 = Pattern.compile("timer (?:for )?(\\d+)\\s*(second|seconds|sec|minute|minutes|min)?");
        Matcher m2 = p2.matcher(input);

        // Regex pattern: "set (?:a )?reminder (?:for )?(.+?) in (\\d+)\\s*(second|seconds|sec|minute|minutes|min)?"
        Pattern p3 = Pattern.compile("set (?:a )?reminder (?:for )?(.+?) in (\\d+)\\s*(second|seconds|sec|minute|minutes|min)?");
        Matcher m3 = p3.matcher(input);

        if (m1.find()) {
            taskTitle = m1.group(1).trim();
            int amount = Integer.parseInt(m1.group(2));
            String unit = m1.group(3) != null ? m1.group(3) : "seconds";
            secondsOffset = convertToSeconds(amount, unit);
        } else if (m3.find()) {
            taskTitle = m3.group(1).trim();
            int amount = Integer.parseInt(m3.group(2));
            String unit = m3.group(3) != null ? m3.group(3) : "seconds";
            secondsOffset = convertToSeconds(amount, unit);
        } else if (m2.find()) {
            int amount = Integer.parseInt(m2.group(1));
            String unit = m2.group(2) != null ? m2.group(2) : "seconds";
            secondsOffset = convertToSeconds(amount, unit);
            taskTitle = "Timer finished (" + amount + " " + unit + ")";
        } else {
            // General fallback: check if number exists
            Pattern pNum = Pattern.compile("(\\d+)\\s*(second|seconds|sec|minute|minutes|min)?");
            Matcher mNum = pNum.matcher(input);
            if (mNum.find()) {
                int amount = Integer.parseInt(mNum.group(1));
                String unit = mNum.group(2) != null ? mNum.group(2) : "seconds";
                secondsOffset = convertToSeconds(amount, unit);
                taskTitle = input.replaceFirst("remind me( to)?", "").trim();
            } else {
                taskTitle = "Voice Reminder: " + input;
            }
        }

        LocalDateTime dueTime = LocalDateTime.now().plusSeconds(secondsOffset);
        boolean saved = reminderDAO.createReminder(taskTitle, dueTime, "HIGH");

        if (saved) {
            String display = String.format("⏰ Reminder Scheduled!\n• Task: %s\n• Due At: %s (in %d sec)\n• Status: Saved to SQLite (tbl_reminders)",
                    taskTitle, dueTime.format(TIME_FMT), secondsOffset);
            String speech = String.format("Reminder set for %s in %d seconds.", taskTitle, secondsOffset);
            return CommandResult.success(display, speech);
        } else {
            return CommandResult.failure("❌ Failed to save reminder to database.", "Could not save your reminder.");
        }
    }

    private int convertToSeconds(int amount, String unit) {
        unit = unit.toLowerCase();
        if (unit.startsWith("min")) {
            return amount * 60;
        } else if (unit.startsWith("hour")) {
            return amount * 3600;
        }
        return amount; // default seconds
    }
}
