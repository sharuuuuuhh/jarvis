package com.jarvis.commands;

import com.jarvis.core.CommandContext;
import com.jarvis.core.CommandResult;
import com.jarvis.core.ConfigManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
        if (input.contains("calculate") || input.matches(".*\\d+.*")) {
            String arithmetic = input.replaceAll("\\bmultiplied by\\b", "*")
                    .replaceAll("\\bdivided by\\b|\\bover\\b", "/")
                    .replaceAll("\\bplus\\b", "+").replaceAll("\\bminus\\b", "-")
                    .replaceAll("\\btimes\\b", "*");
            CommandResult mathRes = tryCalculate(arithmetic);
            if (mathRes != null) return mathRes;
        }

        // 6. Joke
        if (input.contains("joke")) {
            String joke = JOKES[new Random().nextInt(JOKES.length)];
            return CommandResult.success("😄 " + joke, joke);
        }

        // 7. Current weather from Open-Meteo (city is requested explicitly; no location is inferred).
        if (input.contains("weather")) {
            return weather(input);
        }

        return CommandResult.success("💡 Response: " + context.getRawInput(), "I have processed your query: " + context.getRawInput());
    }

    private CommandResult weather(String input) {
        Matcher cityMatch = Pattern.compile("(?:weather|forecast)(?:\\s+(?:in|for))\\s+(.+)$").matcher(input);
        if (!cityMatch.find()) {
            return CommandResult.failure("Tell me the city, for example: ‘what's the weather in Kochi?’", "Which city should I check the weather for?");
        }
        String city = cityMatch.group(1).replaceAll("[?.!,]", "").trim();
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
            String geocodeUrl = "https://geocoding-api.open-meteo.com/v1/search?count=1&language=en&format=json&name=" +
                    URLEncoder.encode(city, StandardCharsets.UTF_8);
            String location = get(client, geocodeUrl);
            String lat = jsonValue(location, "latitude"), lon = jsonValue(location, "longitude");
            String name = jsonString(location, "name");
            String country = jsonString(location, "country");
            if (lat.isBlank() || lon.isBlank()) return CommandResult.failure("I couldn't find ‘" + city + "’. Try adding the country name.", "I couldn't find that city. Please try another name.");

            String forecastUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon +
                    "&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m&timezone=auto";
            String forecast = get(client, forecastUrl);
            String temperature = jsonValue(forecast, "temperature_2m");
            String feels = jsonValue(forecast, "apparent_temperature");
            String humidity = jsonValue(forecast, "relative_humidity_2m");
            String wind = jsonValue(forecast, "wind_speed_10m");
            String code = jsonValue(forecast, "weather_code");
            if (temperature.isBlank()) return CommandResult.failure("Weather data wasn't available for " + city + ".", "I couldn't get weather data for that city.");
            String condition = weatherDescription(code);
            String place = name + (country.isBlank() ? "" : ", " + country);
            String display = String.format("Current weather in %s\n%s · %s°C (feels like %s°C)\nHumidity %s%% · Wind %s km/h",
                    place, condition, temperature, feels.isBlank() ? temperature : feels,
                    humidity.isBlank() ? "—" : humidity, wind.isBlank() ? "—" : wind);
            return CommandResult.success(display, "In " + name + ", it's " + condition.toLowerCase() + " and " + temperature + " degrees Celsius.");
        } catch (Exception e) {
            return CommandResult.failure("Couldn't retrieve weather right now. Check your internet connection and try again.", "I couldn't retrieve the weather right now.");
        }
    }

    private static String get(HttpClient client, String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(12))
                .header("User-Agent", "JARVIS-Desktop/1.0").GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) throw new java.io.IOException("Weather service returned HTTP " + response.statusCode());
        return response.body();
    }

    private static String jsonValue(String json, String key) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)").matcher(json);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static String jsonString(String json, String key) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"").matcher(json);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static String weatherDescription(String code) {
        try {
            int wmo = Integer.parseInt(code);
            if (wmo == 0) return "Clear";
            if (wmo <= 3) return "Partly cloudy";
            if (wmo <= 48) return "Foggy";
            if (wmo <= 57) return "Drizzle";
            if (wmo <= 67) return "Rain";
            if (wmo <= 77) return "Snow";
            if (wmo <= 82) return "Rain showers";
            if (wmo <= 86) return "Snow showers";
            return "Thunderstorms";
        } catch (NumberFormatException e) { return "Current conditions"; }
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
            String display = String.format("%s %s %s equals %s.", matcher.group(1), op, matcher.group(3), resStr);
            String speech = matcher.group(1) + " " + spokenOperator(op) + " " + matcher.group(3) + " equals " + resStr + ".";
            return CommandResult.success(display, speech);
        }
        return null;
    }

    private String spokenOperator(String operator) {
        return switch (operator) {
            case "+" -> "plus";
            case "-" -> "minus";
            case "*", "x", "X" -> "times";
            case "/" -> "divided by";
            default -> operator;
        };
    }
}
