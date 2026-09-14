package com.jarvis.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registry storing all active polymorphic Command handlers.
 * Demonstrates the REGISTRY PATTERN and POLYMORPHISM.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Sharon (TLY25CS104)
 */
public class CommandRegistry {

    private final List<Command> commandList = new ArrayList<>();

    public CommandRegistry() {
        // Register primary commands
        register(new AppLaunchCommand());
        register(new ReminderCommand());
        register(new SystemInfoCommand());
        register(new WebSearchCommand());
        register(new GeneralQueryCommand());
        register(new MediaControlCommand());
        register(new CustomShortcutCommand());
        // Help command with reference to this registry
        register(new HelpCommand(this));
    }

    public synchronized void register(Command command) {
        commandList.add(command);
    }

    public synchronized List<Command> getAllCommands() {
        return Collections.unmodifiableList(new ArrayList<>(commandList));
    }

    public synchronized Command findMatchingCommand(String intent, String rawInput) {
        for (Command cmd : commandList) {
            if (cmd.canHandle(intent, rawInput)) {
                return cmd;
            }
        }
        return null;
    }
}
