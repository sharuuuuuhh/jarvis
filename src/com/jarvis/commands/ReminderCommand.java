package com.jarvis.commands;

import com.jarvis.core.CommandContext;
import com.jarvis.core.CommandResult;
import com.jarvis.database.ReminderDAO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Creates, lists, and cancels reminders and timers. */
public class ReminderCommand extends AbstractCommand {
    private final ReminderDAO reminderDAO = new ReminderDAO();
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("EEE, MMM d 'at' h:mm a");
    private static final Pattern CANCEL = Pattern.compile("^cancel reminder\\s+#?(\\d+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern TIMER = Pattern.compile("^timer(?:\\s+for)?\\s+(\\d+)\\s*(seconds?|secs?|minutes?|mins?|hours?|hrs?)?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern RELATIVE = Pattern.compile("^(?:remind me to|set (?:a )?reminder (?:for )?)(.+?)\\s+in\\s+(\\d+)\\s*(seconds?|secs?|minutes?|mins?|hours?|hrs?)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOCK = Pattern.compile("^remind me to (.+?)\\s+at\\s+(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)$", Pattern.CASE_INSENSITIVE);

    public ReminderCommand() {
        super("Reminder Manager", "Schedules, lists, and cancels persistent reminders and timers.",
                new String[]{"remind me to study in 10 seconds", "remind me to call mom at 5 pm", "timer for 30 minutes", "list reminders", "cancel reminder 3"});
    }

    @Override public boolean canHandle(String intent, String rawInput) {
        if ("REMINDER".equalsIgnoreCase(intent) || "TIMER".equalsIgnoreCase(intent)) return true;
        String lower = rawInput.toLowerCase().trim();
        return lower.startsWith("remind me") || lower.startsWith("set reminder") || lower.startsWith("timer ") ||
                lower.matches("^(list|show) reminders$") || lower.equals("what are my reminders") || lower.startsWith("cancel reminder ");
    }

    @Override protected CommandResult doExecute(CommandContext context) {
        String input = context.getRawInput().trim();
        String lower = input.toLowerCase();

        if (lower.equals("list reminders") || lower.equals("show reminders") || lower.equals("what are my reminders")) {
            List<ReminderDAO.ReminderItem> pending = reminderDAO.getAllReminders().stream()
                    .filter(item -> "PENDING".equalsIgnoreCase(item.getStatus())).toList();
            if (pending.isEmpty()) return CommandResult.success("You have no pending reminders.", "You have no pending reminders.");
            StringBuilder display = new StringBuilder("UPCOMING REMINDERS\n\n");
            for (ReminderDAO.ReminderItem item : pending.stream().limit(20).toList()) {
                display.append('#').append(item.getId()).append("  ").append(item.getTitle()).append(" — ")
                        .append(item.getReminderTime().format(TIME_FMT)).append('\n');
            }
            return CommandResult.success(display.toString(), pending.size() + " pending reminders. I've listed the upcoming reminders.");
        }

        Matcher cancel = CANCEL.matcher(input);
        if (cancel.matches()) {
            int id = Integer.parseInt(cancel.group(1));
            if (reminderDAO.markCancelled(id)) return CommandResult.success("Reminder #" + id + " cancelled.", "Reminder " + id + " has been cancelled.");
            return CommandResult.failure("No pending reminder found with ID #" + id + ".", "I couldn't find a pending reminder with that number.");
        }

        String title;
        LocalDateTime due;
        Matcher clock = CLOCK.matcher(input);
        Matcher relative = RELATIVE.matcher(input);
        Matcher timer = TIMER.matcher(input);
        try {
            if (clock.matches()) {
                title = clock.group(1).trim();
                int hour = Integer.parseInt(clock.group(2));
                int minute = clock.group(3) == null ? 0 : Integer.parseInt(clock.group(3));
                if (hour < 1 || hour > 12 || minute > 59) throw new IllegalArgumentException("Use a clock time such as 5 pm or 5:30 pm.");
                if ("pm".equalsIgnoreCase(clock.group(4)) && hour < 12) hour += 12;
                if ("am".equalsIgnoreCase(clock.group(4)) && hour == 12) hour = 0;
                due = LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, minute));
                if (!due.isAfter(LocalDateTime.now())) due = due.plusDays(1);
            } else if (relative.matches()) {
                title = relative.group(1).trim();
                long seconds = durationSeconds(relative.group(2), relative.group(3));
                due = LocalDateTime.now().plusSeconds(seconds);
            } else if (timer.matches()) {
                String amount = timer.group(1);
                String unit = timer.group(2) == null ? "seconds" : timer.group(2);
                long seconds = durationSeconds(amount, unit);
                title = "Timer finished (" + amount + " " + unit + ")";
                due = LocalDateTime.now().plusSeconds(seconds);
            } else {
                return CommandResult.failure("I couldn't find a time for that reminder. Say ‘remind me to call Mom in 10 minutes’ or ‘at 5 pm’.",
                        "When should I remind you? Include a duration or a time like 5 pm.");
            }
        } catch (NumberFormatException | ArithmeticException e) {
            return CommandResult.failure("That reminder time is too large. Please use a shorter duration.", "That duration is too long. Please try a shorter one.");
        } catch (IllegalArgumentException e) {
            return CommandResult.failure(e.getMessage(), e.getMessage());
        }

        if (title.isBlank()) return CommandResult.failure("Please include what I should remind you about.", "What should I remind you about?");
        boolean saved = reminderDAO.createReminder(title, due, "NORMAL");
        if (!saved) return CommandResult.failure("Couldn't save the reminder.", "I couldn't save that reminder.");
        long seconds = Math.max(1, java.time.Duration.between(LocalDateTime.now(), due).getSeconds());
        String when = due.format(TIME_FMT);
        return CommandResult.success("Reminder set: " + title + "\nDue " + when,
                "Okay, I'll remind you to " + title + " at " + when + ".");
    }

    private long durationSeconds(String amountText, String unit) {
        long amount = Long.parseLong(amountText);
        if (amount < 1) throw new IllegalArgumentException("Use a duration greater than zero.");
        String normalized = unit.toLowerCase();
        long multiplier = normalized.startsWith("h") ? 3600 : normalized.startsWith("m") ? 60 : 1;
        long seconds = Math.multiplyExact(amount, multiplier);
        if (seconds > 365L * 24 * 60 * 60) throw new IllegalArgumentException("Please set reminders less than one year ahead.");
        return seconds;
    }
}
