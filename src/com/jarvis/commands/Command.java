package com.jarvis.commands;

import com.jarvis.core.CommandContext;
import com.jarvis.core.CommandResult;

/**
 * Fundamental Command interface.
 * Demonstrates POLYMORPHISM by allowing the central engine to invoke any command
 * through a unified method signature regardless of the concrete implementation.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Sharon (TLY25CS104)
 */
public interface Command {

    /**
     * Unique identifier for this command category.
     */
    String getName();

    /**
     * Human-readable description of what this command accomplishes.
     */
    String getDescription();

    /**
     * Example spoken utterance triggers.
     */
    String[] getExampleTriggers();

    /**
     * Determines whether this command can handle the given intent and utterance.
     */
    boolean canHandle(String intent, String rawInput);

    /**
     * Polymorphic execution method.
     * 
     * @param context Immutable execution context containing intent, arguments, and timestamp
     * @return Execution result with display text, voice feedback, and status code
     */
    CommandResult execute(CommandContext context);
}
