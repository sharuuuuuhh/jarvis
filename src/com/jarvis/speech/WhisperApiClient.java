package com.jarvis.speech;

import com.jarvis.core.ConfigManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

/**
 * Speech recognizer implementation utilizing Groq's cloud Whisper API (OpenAI-compatible).
 * Uses Java 11+ java.net.http.HttpClient for multipart/form-data binary streaming.
 * Demonstrates ABSTRACTION and POLYMORPHISM by implementing SpeechRecognizer.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Farsin (TLY25CS083)
 */
public class WhisperApiClient implements SpeechRecognizer {

    private static final String WHISPER_API_ENDPOINT = "https://api.groq.com/openai/v1/audio/transcriptions";
    private final HttpClient httpClient;

    public WhisperApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Override
    public String transcribe(byte[] audioWavData) throws Exception {
        if (audioWavData == null || audioWavData.length < 100) {
            throw new IllegalArgumentException("Audio data is empty or too short to transcribe.");
        }

        System.out.println("[GroqWhisper] Audio WAV size: " + audioWavData.length + " bytes");

        String apiKey = ConfigManager.getInstance().getOpenAiApiKey();
        if (apiKey.isBlank()) {
            throw new IllegalStateException("Groq API Key is missing. Please configure it in Settings or set the API key.");
        }

        String model = ConfigManager.getInstance().get("openai.whisper.model", "whisper-large-v3");
        System.out.println("[GroqWhisper] Sending to Groq with model=" + model + " language=en");

        String boundary = "----JarvisWhisperBoundary" + UUID.randomUUID().toString().replace("-", "");

        // Build multipart/form-data body
        byte[] multipartBody = buildMultipartBody(audioWavData, boundary, model);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(WHISPER_API_ENDPOINT))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofByteArray(multipartBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        int statusCode = response.statusCode();
        String responseBody = response.body();

        System.out.println("[GroqWhisper] HTTP Status: " + statusCode);
        if (statusCode >= 200 && statusCode < 300) {
            String transcript = extractTextFromJson(responseBody);
            return transcript;
        } else {
            throw new IOException("Whisper API Error (HTTP " + statusCode + "): " + responseBody);
        }
    }

    @Override
    public boolean isAvailable() {
        String key = ConfigManager.getInstance().getOpenAiApiKey();
        return key != null && !key.isBlank() && !ConfigManager.getInstance().isOfflineMode();
    }

    @Override
    public String getEngineName() {
        return "Groq Whisper Cloud API (" + ConfigManager.getInstance().get("openai.whisper.model", "whisper-large-v3") + ")";
    }

    private byte[] buildMultipartBody(byte[] audioData, String boundary, String model) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 1. Model field
        baos.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        baos.write("Content-Disposition: form-data; name=\"model\"\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        baos.write((model + "\r\n").getBytes(StandardCharsets.UTF_8));

        // 2. Language field - forces English, prevents hallucinations on short audio
        baos.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        baos.write("Content-Disposition: form-data; name=\"language\"\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        baos.write("en\r\n".getBytes(StandardCharsets.UTF_8));

        // 3. Prompt field - biases Whisper toward JARVIS command vocabulary for better accuracy
        String prompt = "open calculator, open notepad, what time is it, system info, search, " +
                        "set reminder, volume up, volume down, play music, tell me a joke, " +
                        "open browser, open chrome, launch, start, help, what is today's date";
        baos.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        baos.write("Content-Disposition: form-data; name=\"prompt\"\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        baos.write((prompt + "\r\n").getBytes(StandardCharsets.UTF_8));

        // 3. Audio file field
        baos.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        baos.write("Content-Disposition: form-data; name=\"file\"; filename=\"audio.wav\"\r\n".getBytes(StandardCharsets.UTF_8));
        baos.write("Content-Type: audio/wav\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        baos.write(audioData);
        baos.write("\r\n".getBytes(StandardCharsets.UTF_8));

        // 4. Response format field
        baos.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        baos.write("Content-Disposition: form-data; name=\"response_format\"\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        baos.write("json\r\n".getBytes(StandardCharsets.UTF_8));

        // Final boundary
        baos.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

        return baos.toByteArray();
    }

    /**
     * Parses the "text" property from Groq's JSON response: {"text": "..."}
     * Standalone regex parser to avoid mandatory external JSON dependencies.
     */
    public static String extractTextFromJson(String json) {
        if (json == null) return "";
        int key = json.indexOf("\"text\"");
        if (key < 0) return "";
        int cursor = key + 6;
        while (cursor < json.length() && Character.isWhitespace(json.charAt(cursor))) cursor++;
        if (cursor >= json.length() || json.charAt(cursor++) != ':') return "";
        while (cursor < json.length() && Character.isWhitespace(json.charAt(cursor))) cursor++;
        if (cursor >= json.length() || json.charAt(cursor++) != '"') return "";

        StringBuilder decoded = new StringBuilder();
        while (cursor < json.length()) {
            char current = json.charAt(cursor++);
            if (current == '"') return decoded.toString().trim();
            if (current != '\\') {
                decoded.append(current);
                continue;
            }
            if (cursor >= json.length()) return "";
            char escape = json.charAt(cursor++);
            switch (escape) {
                case '"' -> decoded.append('"');
                case '\\' -> decoded.append('\\');
                case '/' -> decoded.append('/');
                case 'b' -> decoded.append('\b');
                case 'f' -> decoded.append('\f');
                case 'n' -> decoded.append('\n');
                case 'r' -> decoded.append('\r');
                case 't' -> decoded.append('\t');
                case 'u' -> {
                    if (cursor + 4 > json.length()) return "";
                    try {
                        decoded.append((char) Integer.parseInt(json.substring(cursor, cursor + 4), 16));
                        cursor += 4;
                    } catch (NumberFormatException e) {
                        return "";
                    }
                }
                default -> { return ""; }
            }
        }
        return "";
    }
}
