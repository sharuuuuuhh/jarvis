package com.jarvis.commands;

import com.jarvis.core.CommandContext;
import com.jarvis.core.CommandResult;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Command automating web searches in the default system browser.
 * Demonstrates INHERITANCE and Java Desktop browser integration.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Sharon (TLY25CS104)
 */
public class WebSearchCommand extends AbstractCommand {

    public WebSearchCommand() {
        super("Web Search", "Executes queries on Google, YouTube, or Wikipedia in your web browser.",
                new String[]{"search google for Java OOP", "search youtube for music", "search for KTU CSE syllabus"});
    }

    @Override
    public boolean canHandle(String intent, String rawInput) {
        if ("WEB_SEARCH".equalsIgnoreCase(intent)) return true;
        String lower = rawInput.toLowerCase();
        return lower.startsWith("search ") || lower.startsWith("google ") || lower.contains("search for ");
    }

    @Override
    protected CommandResult doExecute(CommandContext context) throws Exception {
        String input = context.getRawInput().toLowerCase().trim();
        String query = context.getArgument();

        String engine = "Google";
        String searchUrl;

        if (input.contains("youtube")) {
            engine = "YouTube";
            query = cleanQuery(input, "youtube");
            searchUrl = "https://www.youtube.com/results?search_query=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
        } else if (input.contains("wikipedia")) {
            engine = "Wikipedia";
            query = cleanQuery(input, "wikipedia");
            searchUrl = "https://en.wikipedia.org/wiki/Special:Search?search=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
        } else {
            engine = "Google";
            query = cleanQuery(input, "google");
            searchUrl = "https://www.google.com/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
        }

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(searchUrl));
            String display = String.format("🌐 Searching %s for: \"%s\"\nURL: %s", engine, query, searchUrl);
            String speech = "Searching " + engine + " for " + query;
            return CommandResult.success(display, speech);
        } else {
            // Fallback via Windows cmd start
            new ProcessBuilder("cmd", "/c", "start", searchUrl).start();
            return CommandResult.success("🌐 Opened browser search for: \"" + query + "\"", "Searching for " + query);
        }
    }

    private String cleanQuery(String input, String engineName) {
        String q = input;
        for (String pattern : new String[]{
                "search " + engineName + " for ",
                "search " + engineName + " ",
                "search for ",
                "search ",
                engineName + " for ",
                engineName + " "
        }) {
            if (q.contains(pattern)) {
                q = q.replace(pattern, "");
            }
        }
        return q.trim().isEmpty() ? "Java Programming" : q.trim();
    }
}
