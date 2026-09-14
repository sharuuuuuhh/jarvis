package com.jarvis.commands;

import com.jarvis.core.CommandContext;
import com.jarvis.core.CommandResult;
import com.jarvis.database.CustomShortcutDAO;

import java.awt.Desktop;
import java.net.URI;

/**
 * Command executing custom user-defined macros persisted in tbl_custom_shortcuts.
 * Demonstrates INHERITANCE and extensible user configuration.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Sharon (TLY25CS104)
 */
public class CustomShortcutCommand extends AbstractCommand {

    private final CustomShortcutDAO shortcutDAO = new CustomShortcutDAO();

    public CustomShortcutCommand() {
        super("Custom Voice Shortcuts", "Executes custom user-registered voice triggers stored in SQLite.",
                new String[]{"open github", "open youtube", "launch code"});
    }

    @Override
    public boolean canHandle(String intent, String rawInput) {
        if ("CUSTOM_SHORTCUT".equalsIgnoreCase(intent)) return true;
        CustomShortcutDAO.ShortcutEntry entry = shortcutDAO.findShortcut(rawInput);
        return entry != null;
    }

    @Override
    protected CommandResult doExecute(CommandContext context) throws Exception {
        String input = context.getRawInput().toLowerCase().trim();
        CustomShortcutDAO.ShortcutEntry entry = shortcutDAO.findShortcut(input);

        if (entry == null) {
            return CommandResult.failure("❌ Custom shortcut not found for phrase: " + input, "I could not find that shortcut.");
        }

        String type = entry.getActionType();
        String target = entry.getTargetPath();

        if ("BROWSER".equalsIgnoreCase(type)) {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(target));
            } else {
                new ProcessBuilder("cmd", "/c", "start", target).start();
            }
            return CommandResult.success("⚡ Triggered custom web shortcut: " + target, "Opening " + entry.getTriggerPhrase());
        } else if ("APP_LAUNCH".equalsIgnoreCase(type) || "SYSTEM_CMD".equalsIgnoreCase(type)) {
            new ProcessBuilder("cmd", "/c", "start", target).start();
            return CommandResult.success("⚡ Executed custom shortcut command: " + target, "Running " + entry.getTriggerPhrase());
        }

        return CommandResult.failure("❌ Unsupported shortcut action type: " + type, "Shortcut action type is not supported.");
    }
}
