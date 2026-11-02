package com.jarvis.commands;

import com.jarvis.core.CommandContext;
import com.jarvis.core.CommandResult;
import com.jarvis.core.ConfigManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;

/** Conversational fallback for natural-language requests the local command set does not handle. */
public class AiChatCommand extends AbstractCommand {
    private static final String ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final Deque<Turn> history = new ArrayDeque<>();

    public AiChatCommand() {
        super("AI Assistant", "Answers open-ended questions and helps with writing, explanations, and brainstorming.",
                new String[]{"explain photosynthesis", "help me write an email", "give me ideas for a project"});
    }

    @Override public boolean canHandle(String intent, String rawInput) {
        return "AI_CHAT".equalsIgnoreCase(intent);
    }

    @Override protected synchronized CommandResult doExecute(CommandContext context) {
        ConfigManager config = ConfigManager.getInstance();
        String key = config.getOpenAiApiKey();
        if (key.isBlank() || config.isOfflineMode()) {
            return CommandResult.failure("Open-ended AI chat needs online mode and a Groq API key. Add the key in Settings and turn off Offline mode.",
                    "Please configure a Groq API key and turn off offline mode to use AI chat.");
        }

        String userText = context.getRawInput().trim();
        if (userText.isEmpty()) return CommandResult.failure("Please ask me something.", "What would you like to know?");
        try {
            String model = config.get("openai.chat.model", "openai/gpt-oss-120b");
            StringBuilder messages = new StringBuilder("[{").append("\"role\":\"system\",\"content\":")
                    .append(json("You are JARVIS, a helpful desktop assistant. Be clear, concise, and honest when you are unsure. You cannot see the user's screen or perform computer actions unless the app exposes a dedicated command." )).append('}');
            for (Turn turn : history) {
                messages.append(",{").append("\"role\":").append(json(turn.role)).append(",\"content\":").append(json(turn.text)).append('}');
            }
            messages.append(",{").append("\"role\":\"user\",\"content\":").append(json(userText)).append("}]");
            String body = "{\"model\":" + json(model) + ",\"messages\":" + messages + ",\"max_completion_tokens\":700,\"temperature\":0.5}";

            HttpRequest request = HttpRequest.newBuilder(URI.create(ENDPOINT))
                    .timeout(Duration.ofSeconds(45))
                    .header("Authorization", "Bearer " + key)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return CommandResult.failure("AI service returned HTTP " + response.statusCode() + ". Check the key, connection, and chat model in Settings.",
                        "I couldn't reach the AI service. Please check the API key and internet connection.");
            }
            String answer = extractContent(response.body());
            if (answer.isBlank()) return CommandResult.failure("The AI service returned an empty response. Please try again.", "I didn't receive an answer. Please try again.");
            history.addLast(new Turn("user", userText));
            history.addLast(new Turn("assistant", answer));
            while (history.size() > 12) history.removeFirst();
            return CommandResult.success(answer, answer);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CommandResult.failure("The AI request was interrupted.", "The request was interrupted.");
        } catch (IOException | RuntimeException e) {
            return CommandResult.failure("Couldn't connect to the AI service: " + e.getMessage(),
                    "I couldn't connect to the AI service. Please check your internet connection.");
        }
    }

    private static String json(String text) {
        StringBuilder out = new StringBuilder("\"");
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> { if (c < 0x20) out.append(String.format("\\u%04x", (int)c)); else out.append(c); }
            }
        }
        return out.append('"').toString();
    }

    /** Extracts choices[0].message.content while decoding JSON string escapes. */
    static String extractContent(String json) {
        if (json == null) return "";
        int key = json.indexOf("\"content\"");
        if (key < 0) return "";
        int i = json.indexOf(':', key + 9) + 1;
        while (i > 0 && i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
        if (i <= 0 || i >= json.length() || json.charAt(i++) != '"') return "";
        StringBuilder value = new StringBuilder();
        while (i < json.length()) {
            char c = json.charAt(i++);
            if (c == '"') return value.toString().trim();
            if (c != '\\') { value.append(c); continue; }
            if (i >= json.length()) return "";
            char e = json.charAt(i++);
            switch (e) {
                case '"', '\\', '/' -> value.append(e);
                case 'n' -> value.append('\n');
                case 'r' -> value.append('\r');
                case 't' -> value.append('\t');
                case 'b' -> value.append('\b');
                case 'f' -> value.append('\f');
                case 'u' -> {
                    if (i + 4 > json.length()) return "";
                    try { value.append((char) Integer.parseInt(json.substring(i, i + 4), 16)); }
                    catch (NumberFormatException ex) { return ""; }
                    i += 4;
                }
                default -> { return ""; }
            }
        }
        return "";
    }

    private static final class Turn {
        final String role, text;
        Turn(String role, String text) { this.role = role; this.text = text; }
    }
}
