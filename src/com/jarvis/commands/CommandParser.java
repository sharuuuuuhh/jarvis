package com.jarvis.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses raw transcribed natural language text into discrete intent codes and parameters.
 * Demonstrates ENCAPSULATION of NLP parsing rules and regex matching.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Sharon (TLY25CS104)
 */
public class CommandParser {

    public static class ParsedIntent {
        private final String intent;
        private final String argument;
        private final Map<String, Object> parameters;

        public ParsedIntent(String intent, String argument, Map<String, Object> parameters) {
            this.intent = intent;
            this.argument = argument;
            this.parameters = parameters != null ? parameters : new HashMap<>();
        }

        public String getIntent() { return intent; }
        public String getArgument() { return argument; }
        public Map<String, Object> getParameters() { return parameters; }
    }

    public static ParsedIntent parse(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return new ParsedIntent("EMPTY", "", new HashMap<>());
        }

        // Normalize text: lowercase and strip trailing punctuation
        String text = rawText.toLowerCase().replaceAll("[?!,]", "").replaceAll("(?<!\\d)\\.|\\.(?!\\d)", "").trim();
        Map<String, Object> params = new HashMap<>();

        // A single spoken request may contain an app and a follow-up web search.
        if (text.matches(".*\\b(and|then)\\s+(search|google)\\b.*")) {
            String query = text.replaceFirst("^.*?\\b(?:and|then)\\s+(?:search(?:\\s+(?:google|youtube|wikipedia))?|google)\\s+(?:for\\s+)?", "").trim();
            if (!query.isBlank()) {
                params.put("query", query);
                return new ParsedIntent("WEB_SEARCH", query, params);
            }
        }

        // 1. App launching
        if (text.startsWith("open ") || text.startsWith("launch ") || text.startsWith("start ")) {
            String app = text.replaceFirst("^(open|launch|start)\\s+", "").trim();
            params.put("target", app);
            return new ParsedIntent("APP_LAUNCH", app, params);
        }

        // 2. Reminders & Timers
        if (text.startsWith("remind me") || text.startsWith("set reminder") || text.startsWith("timer") ||
            text.equals("list reminders") || text.equals("show reminders") || text.equals("what are my reminders") ||
            text.startsWith("cancel reminder")) {
            return new ParsedIntent("REMINDER", text, params);
        }

        if (text.startsWith("take a note") || text.startsWith("write a note") || text.startsWith("save a note") ||
            text.startsWith("note that") || text.startsWith("remember that") || text.equals("read my notes") ||
            text.equals("list my notes")) {
            return new ParsedIntent("NOTES", text, params);
        }

        if (text.startsWith("copy to clipboard ") || text.startsWith("copy this ")) {
            return new ParsedIntent("CLIPBOARD_COPY", text, params);
        }

        // 3. System Telemetry
        if (text.contains("system info") || text.contains("check cpu") || text.contains("ram usage") ||
            text.contains("memory usage") || text.contains("system status")) {
            return new ParsedIntent("SYSTEM_INFO", text, params);
        }

        // 4. Web Search
        if (text.startsWith("search ") || text.startsWith("google ")) {
            String query = text.replaceFirst("^search\\s+(?:on\\s+)?(?:google|youtube|wikipedia)\\s+(?:for|about)\\s+", "")
                    .replaceFirst("^search\\s+(?:for|about)\\s+", "")
                    .replaceFirst("^search\\s+(?:on\\s+)?(?:google|youtube|wikipedia)\\s+", "")
                    .replaceFirst("^google\\s+(?:for|about)\\s+", "")
                    .replaceFirst("^(search|google)\\s+", "").trim();
            params.put("query", query);
            return new ParsedIntent("WEB_SEARCH", query, params);
        }

        // 5. Media & Volume Control
        if (text.contains("mute") || text.contains("volume up") || text.contains("volume down") ||
            text.contains("increase volume") || text.contains("decrease volume") || text.contains("lower volume") ||
            text.contains("play music") || text.contains("pause music") || text.contains("resume music") ||
            text.contains("next track") || text.contains("previous track") || text.contains("skip song")) {
            return new ParsedIntent("MEDIA_CONTROL", text, params);
        }

        // 6. Help
        if (text.equals("help") || text.contains("what can you do") || text.contains("commands") || text.contains("manual")) {
            return new ParsedIntent("HELP", text, params);
        }

        // 7. General Knowledge / Greetings / Math
        if (text.contains("time") || text.contains("date") || text.contains("who are you") ||
            text.contains("who made you") || text.contains("calculate") || text.contains("joke") ||
            text.equals("hello") || text.equals("hi") || text.startsWith("hey jarvis") || text.contains("weather") ||
            text.matches(".*\\d+(?:\\.\\d+)?\\s*(?:[+\\-*/xX]|plus|minus|times|multiplied by|divided by|over)\\s*\\d+(?:\\.\\d+)?.*")) {
            return new ParsedIntent("GENERAL_QUERY", text, params);
        }

        // Route open-ended requests to the conversational AI handler.
        return new ParsedIntent("AI_CHAT", text, params);
    }
}
