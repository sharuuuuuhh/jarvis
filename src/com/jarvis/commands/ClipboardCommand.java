package com.jarvis.commands;

import com.jarvis.core.CommandContext;
import com.jarvis.core.CommandResult;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

/** Copies text only when the user explicitly asks; clipboard contents are never read. */
public class ClipboardCommand extends AbstractCommand {
    public ClipboardCommand() {
        super("Clipboard", "Copies explicitly provided text into the system clipboard.",
                new String[]{"copy to clipboard meeting at 3 pm", "copy this hello world"});
    }
    @Override public boolean canHandle(String intent, String rawInput) {
        return "CLIPBOARD_COPY".equalsIgnoreCase(intent);
    }
    @Override protected CommandResult doExecute(CommandContext context) {
        String text = context.getRawInput().replaceFirst("(?i)^copy (?:to clipboard|this)\\s*", "").trim();
        if (text.isBlank()) return CommandResult.failure("Say ‘copy to clipboard’ followed by the text.", "What text should I copy?");
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
            return CommandResult.success("Copied to clipboard:\n" + text, "Copied that to your clipboard.");
        } catch (HeadlessException | IllegalStateException e) {
            return CommandResult.failure("Clipboard is currently unavailable.", "I can't access the clipboard right now.");
        }
    }
}
