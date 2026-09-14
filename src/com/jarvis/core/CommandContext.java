package com.jarvis.core;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Encapsulates the execution context passed to all polymorphic commands.
 * Demonstrates ENCAPSULATION by shielding internal request parameters.
 * 
 * KTU S3 CSE OOP Mini Project - JARVIS
 */
public class CommandContext {
    private final String rawInput;
    private final String intent;
    private final String argument;
    private final Map<String, Object> parameters;
    private final LocalDateTime timestamp;

    public CommandContext(String rawInput, String intent, String argument, Map<String, Object> parameters) {
        this.rawInput = (rawInput != null) ? rawInput.trim() : "";
        this.intent = (intent != null) ? intent.toUpperCase().trim() : "UNKNOWN";
        this.argument = (argument != null) ? argument.trim() : "";
        this.parameters = (parameters != null) ? Collections.unmodifiableMap(parameters) : Collections.emptyMap();
        this.timestamp = LocalDateTime.now();
    }

    public CommandContext(String rawInput, String intent, String argument) {
        this(rawInput, intent, argument, Collections.emptyMap());
    }

    public String getRawInput() {
        return rawInput;
    }

    public String getIntent() {
        return intent;
    }

    public String getArgument() {
        return argument;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public Object getParameter(String key) {
        return parameters.get(key);
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "CommandContext[intent=" + intent + ", argument='" + argument + "', time=" + timestamp + "]";
    }
}
