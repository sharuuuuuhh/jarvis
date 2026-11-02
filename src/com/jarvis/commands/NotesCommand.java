package com.jarvis.commands;

import com.jarvis.core.CommandContext;
import com.jarvis.core.CommandResult;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Lightweight private notes stored locally beside the application. */
public class NotesCommand extends AbstractCommand {
    private static final Path NOTES_FILE = Path.of("jarvis_notes.txt");
    public NotesCommand() {
        super("Notes", "Saves and reads notes locally on this computer.",
                new String[]{"take a note buy milk", "remember that my meeting is at 3", "read my notes"});
    }
    @Override public boolean canHandle(String intent, String rawInput) {
        return "NOTES".equalsIgnoreCase(intent);
    }
    @Override protected CommandResult doExecute(CommandContext context) throws Exception {
        String input = context.getRawInput().trim();
        if (input.equalsIgnoreCase("read my notes") || input.equalsIgnoreCase("list my notes")) {
            if (!Files.exists(NOTES_FILE)) return CommandResult.success("You don't have any saved notes yet.", "You don't have any saved notes yet.");
            List<String> notes = Files.readAllLines(NOTES_FILE, StandardCharsets.UTF_8);
            StringBuilder result = new StringBuilder("YOUR NOTES\n\n");
            int start = Math.max(0, notes.size() - 20);
            for (int i = start; i < notes.size(); i++) result.append("• ").append(notes.get(i)).append('\n');
            return CommandResult.success(result.toString(), notes.size() + " saved notes. I've shown the latest notes.");
        }
        String note = input.replaceFirst("(?i)^(take|write|save)\\s+(?:a\\s+)?note\\s*", "")
                .replaceFirst("(?i)^(note that|remember that)\\s*", "").trim();
        if (note.isBlank()) return CommandResult.failure("Say ‘take a note’ followed by the note text.", "What would you like me to write down?");
        String line = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + " — " + note + System.lineSeparator();
        Files.writeString(NOTES_FILE, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        return CommandResult.success("Note saved on this computer:\n" + note, "I've saved that note on this computer.");
    }
}
