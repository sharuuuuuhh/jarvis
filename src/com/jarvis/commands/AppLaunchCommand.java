package com.jarvis.commands;

import com.jarvis.core.CommandContext;
import com.jarvis.core.CommandResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Command launching native operating system desktop applications.
 * Demonstrates INHERITANCE (extends AbstractCommand) and OS Process automation (ProcessBuilder).
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Sharon (TLY25CS104)
 */
public class AppLaunchCommand extends AbstractCommand {

    private static final Map<String, String[]> APP_REGISTRY = new HashMap<>();

    static {
        // App alias to Windows executable command
        APP_REGISTRY.put("notepad", new String[]{"notepad.exe"});
        APP_REGISTRY.put("calculator", new String[]{"calc.exe"});
        APP_REGISTRY.put("calc", new String[]{"calc.exe"});
        APP_REGISTRY.put("paint", new String[]{"mspaint.exe"});
        APP_REGISTRY.put("chrome", new String[]{"cmd", "/c", "start", "chrome"});
        APP_REGISTRY.put("google chrome", new String[]{"cmd", "/c", "start", "chrome"});
        APP_REGISTRY.put("edge", new String[]{"cmd", "/c", "start", "msedge"});
        APP_REGISTRY.put("browser", new String[]{"cmd", "/c", "start", "msedge"});
        APP_REGISTRY.put("explorer", new String[]{"explorer.exe"});
        APP_REGISTRY.put("file explorer", new String[]{"explorer.exe"});
        APP_REGISTRY.put("files", new String[]{"explorer.exe"});
        APP_REGISTRY.put("cmd", new String[]{"cmd.exe", "/c", "start", "cmd"});
        APP_REGISTRY.put("terminal", new String[]{"cmd.exe", "/c", "start", "cmd"});
        APP_REGISTRY.put("powershell", new String[]{"powershell.exe"});
        APP_REGISTRY.put("task manager", new String[]{"taskmgr.exe"});
        APP_REGISTRY.put("code", new String[]{"cmd", "/c", "start", "code"});
        APP_REGISTRY.put("vs code", new String[]{"cmd", "/c", "start", "code"});
        APP_REGISTRY.put("vscode", new String[]{"cmd", "/c", "start", "code"});
        APP_REGISTRY.put("spotify", new String[]{"cmd", "/c", "start", "spotify"});
        APP_REGISTRY.put("wordpad", new String[]{"write.exe"});
    }

    public AppLaunchCommand() {
        super("App Launcher", "Launches installed desktop software and operating system tools.",
                new String[]{"open notepad", "launch calculator", "open chrome", "start terminal"});
    }

    @Override
    public boolean canHandle(String intent, String rawInput) {
        if ("APP_LAUNCH".equalsIgnoreCase(intent)) return true;
        String lower = rawInput.toLowerCase();
        return lower.startsWith("open ") || lower.startsWith("launch ") || lower.startsWith("start ");
    }

    @Override
    protected CommandResult doExecute(CommandContext context) throws Exception {
        String target = context.getArgument();
        if (target.isBlank()) {
            String raw = context.getRawInput().toLowerCase();
            for (String prefix : new String[]{"open ", "launch ", "start "}) {
                if (raw.startsWith(prefix)) {
                    target = raw.substring(prefix.length()).trim();
                    break;
                }
            }
        }

        target = target.toLowerCase().trim();
        String[] cmdToRun = APP_REGISTRY.get(target);

        if (cmdToRun == null) {
            return CommandResult.failure("I don't have a safe app shortcut for ‘" + target + "’. Open the Apps page to see supported apps.",
                    "I don't recognize that app. You can ask me to open Notepad, Calculator, Chrome, Edge, or another supported shortcut.");
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(cmdToRun);
            pb.start();
            String display = "🚀 Successfully launched: " + target.toUpperCase();
            String speech = "Launching " + target + " now.";
            return CommandResult.success(display, speech);
        } catch (IOException e) {
            return CommandResult.failure(
                    "❌ Failed to launch application '" + target + "': " + e.getMessage(),
                    "I was unable to open " + target + ". Please check if it is installed."
            );
        }
    }
}
