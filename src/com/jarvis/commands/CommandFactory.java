package com.jarvis.commands;

import com.jarvis.core.CommandContext;
import com.jarvis.core.CommandResult;

/**
 * Factory class creating and resolving polymorphic Command instances based on intent.
 * Demonstrates the FACTORY DESIGN PATTERN and dynamic runtime POLYMORPHISM.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Sharon (TLY25CS104)
 */
public class CommandFactory {

    private final CommandRegistry registry;

    public CommandFactory(CommandRegistry registry) {
        this.registry = registry;
    }

    /**
     * Resolves a Command instance matching the parsed intent or user utterance.
     * Returns a fallback Null-Object Command if no handler matches.
     */
    public Command getCommand(String intent, String rawInput) {
        Command matched = registry.findMatchingCommand(intent, rawInput);
        if (matched != null) {
            return matched;
        }

        // Return fallback command adhering to Null Object pattern
        return new AbstractCommand("Unknown Command", "Handles unrecognized commands gracefully.", new String[]{}) {
            @Override
            public boolean canHandle(String intent, String rawInput) {
                return true;
            }

            @Override
            protected CommandResult doExecute(CommandContext context) throws Exception {
                return CommandResult.unknown(context.getRawInput());
            }
        };
    }
}
