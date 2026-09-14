package com.jarvis.commands;

import com.jarvis.core.CommandContext;
import com.jarvis.core.CommandResult;
import com.jarvis.core.ConfigManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command handling general conversational queries, calculations, time/date checks,
 * and knowledge base queries.
 * Demonstrates INHERITANCE and knowledge processing.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Sharon (TLY25CS104)
 */
public class GeneralQueryCommand extends AbstractCommand {

    private static final String[] JOKES = {
        "Why do Java developers wear glasses? Because they don't C#!",
        "There are 10 types of people in the world: those who understand binary, and those who don't.",
        "A SQL query walks into a bar, walks up to two tables and asks, 'Can I join you?'",
        "Why was the JavaScript developer sad? Because they didn't Node how to Express themselves!"
    };

    public GeneralQueryCommand() {
        super("General Query", "Answers queries about time, date, math, weather, and assistant identity.",
                new String[]{"what time is it", "what is today's date", "who made you", "calculate 15 * 6", "tell me a joke"});
    }

    @Override
    public boolean canHandle(String intent, String rawInput) {
        if ("GENERAL_QUERY".equalsIgnoreCase(intent)) return true;
        String lower = rawInput.toLowerCase();
        return lower.contains("time") || lower.contains("date") || lower.contains("who are you") ||
               lower.contains("who made you") || lower.contains("calculate") || lower.contains("joke") ||
               lower.contains("hello") || lower.contains("hi jarvis") || lower.contains("weather");
    }

    @Override
    protected CommandResult doExecute(CommandContext context) throws Exception {
        String input = context.getRawInput().toLowerCase().trim();
        String userName = ConfigManager.getInstance().getUserName();

        // 1. Time query
        if (input.contains("time")) {
            LocalTime now = LocalTime.now();
            String timeStr = now.format(DateTimeFormatter.ofPattern("hh:mm a"));
            return CommandResult.success("🕒 Current Time: " + timeStr, "The current time is " + timeStr + ", " + userName + ".");
        }

        // 2. Date query
        if (input.contains("date") || input.contains("today")) {
            LocalDate today = LocalDate.now();
            String dateStr = today.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"));
            return CommandResult.success("📅 Today's Date: " + dateStr, "Today is " + dateStr + ".");
        }

        // 3. Identity / Creator query (KTU CSE S3 context)
        if (input.contains("who are you") || input.contains("who made you") || input.contains("what is jarvis")) {
            String info = "🤖 I am JARVIS (Java-Based Intelligent Voice Assistant).\n" +
                          "Built for the KTU S3 CSE Object-Oriented Programming (CSL 203) Mini Project.\n" +
                          "Engineering Team:\n" +
                          "• Jobin (TLY25CS065) - Audio Pipeline & Java Sound API\n" +
                          "• Farsin (TLY25CS083) - OpenAI Whisper API & Networking\n" +
                          "• Sharon (TLY25CS104) - OOP Command Architecture & OS Dispatch\n" +
                          "• Theerth (TLY25CS120) - SQLite Database & Swing GUI";
            String speech = "I am JARVIS, an intelligent voice assistant developed in Java by Jobin, Farsin, Sharon, and Theerth for their KTU S3 CSE OOP Mini Project.";
            return CommandResult.success(info, speech);
        }

        // 4. Greetings
        if (input.equals("hello") || input.equals("hi") || input.startsWith("hey jarvis") || input.startsWith("hello jarvis")) {
            return CommandResult.success("👋 Hello " + userName + "! All systems are online and standing by.",
                    "Hello " + userName + ". All systems are online and operational. How may I assist you?");
        }

        // 5. Math calculation
        if (input.contains("calculate") || input.contains("+") || input.contains("-") || input.contains("*") || input.contains("/")) {
            CommandResult mathRes = tryCalculate(input);
            if (mathRes != null) return mathRes;
        }

        // 6. Joke
        if (input.contains("joke")) {
            String joke = JOKES[new Random().nextInt(JOKES.length)];
            return CommandResult.success("😄 " + joke, joke);
        }

        // 7. Weather
        if (input.contains("weather")) {
            return CommandResult.success("⛅ Local Weather: 28°C, Partly Cloudy, 74% Humidity. Pleasant breeze.",
                    "The current temperature is approximately 28 degrees Celsius with partly cloudy skies.");
        }

        return CommandResult.success("💡 Response: " + context.getRawInput(), "I have processed your query: " + context.getRawInput());
    }

    private CommandResult tryCalculate(String input) {
        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*([+\\-*/xX])\\s*(\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            double a = Double.parseDouble(matcher.group(1));
            String op = matcher.group(2);
            double b = Double.parseDouble(matcher.group(3));
            double result = 0;

            switch (op) {
                case "+": result = a + b; break;
                case "-": result = a - b; break;
                case "*":
                case "x":
                case "X": result = a * b; break;
                case "/":
                    if (b == 0) return CommandResult.failure("❌ Error: Division by zero.", "Cannot divide by zero.");
                    result = a / b;
                    break;
            }

            String resStr = (result == (long) result) ? String.format("%d", (long) result) : String.format("%.2f", result);
            String display = String.format("🧮 Calculation: %s %s %s = %s", matcher.group(1), op, matcher.group(3), resStr);
            String speech = "The answer is " + resStr + ".";
            return CommandResult.success(display, speech);
        }
        return null;
    }
}
