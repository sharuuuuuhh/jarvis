package com.jarvis.core;

import java.io.*;
import java.nio.file.*;
import java.util.Locale;
import java.util.Properties;

/**
 * Singleton configuration manager encapsulating persistent application settings.
 * Demonstrates ENCAPSULATION and the SINGLETON DESIGN PATTERN.
 * 
 * KTU S3 CSE OOP Mini Project - JARVIS
 */
public class ConfigManager {
    private static volatile ConfigManager instance;

    private static final String CONFIG_FILE = "jarvis_config.properties";
    private final Properties properties = new Properties();

    // Default configuration keys
    public static final String KEY_OPENAI_API_KEY = "openai.api.key";
    public static final String KEY_WHISPER_MODEL = "openai.whisper.model";
    public static final String KEY_OFFLINE_MODE = "jarvis.offline.mode";
    public static final String KEY_TTS_VOICE = "jarvis.tts.voice";
    public static final String KEY_TTS_RATE = "jarvis.tts.rate";
    public static final String KEY_TTS_ENABLED = "jarvis.tts.enabled";
    public static final String KEY_SOUND_EFFECTS = "jarvis.sound.effects";
    public static final String KEY_CONTINUOUS_FLOW = "jarvis.whisperflow.continuous";
    public static final String KEY_USER_NAME = "jarvis.user.name";

    private ConfigManager() {
        loadDefaults();
        loadFromFile();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }

    private void loadDefaults() {
        // Check both GROQ_API_KEY and legacy OPENAI_API_KEY env vars
        String envKey = System.getenv("GROQ_API_KEY");
        if (envKey == null || envKey.isBlank()) {
            envKey = System.getenv().getOrDefault("OPENAI_API_KEY", "");
        }
        properties.setProperty(KEY_OPENAI_API_KEY, envKey);
        properties.setProperty(KEY_WHISPER_MODEL, "whisper-large-v3");
        properties.setProperty(KEY_OFFLINE_MODE, "false");
        properties.setProperty(KEY_TTS_VOICE, "Default");
        properties.setProperty(KEY_TTS_RATE, "1");
        properties.setProperty(KEY_TTS_ENABLED, "true");
        properties.setProperty(KEY_SOUND_EFFECTS, "true");
        properties.setProperty(KEY_CONTINUOUS_FLOW, "false");
        properties.setProperty(KEY_USER_NAME, "Boss");
    }

    public synchronized void loadFromFile() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                properties.load(in);
            } catch (IOException e) {
                System.err.println("[ConfigManager] Could not load config file: " + e.getMessage());
            }
        }
    }

    public synchronized void saveToFile() {
        try (OutputStream out = new FileOutputStream(CONFIG_FILE)) {
            properties.store(out, "JARVIS Configuration Properties - KTU S3 CSE OOP Project");
        } catch (IOException e) {
            System.err.println("[ConfigManager] Could not save config file: " + e.getMessage());
        }
    }

    public synchronized String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public synchronized void set(String key, String value) {
        if (value == null) {
            properties.remove(key);
        } else {
            properties.setProperty(key, value);
        }
        saveToFile();
    }

    public String getOpenAiApiKey() {
        String key = properties.getProperty(KEY_OPENAI_API_KEY, "");
        if (isPlaceholderApiKey(key)) {
            // Try GROQ_API_KEY first, then legacy OPENAI_API_KEY
            key = System.getenv("GROQ_API_KEY");
            if (key == null || key.isBlank()) {
                key = System.getenv().getOrDefault("OPENAI_API_KEY", "");
            }
        }
        key = key != null ? key.trim() : "";
        return isPlaceholderApiKey(key) ? "" : key;
    }

    private static boolean isPlaceholderApiKey(String key) {
        if (key == null || key.isBlank()) return true;
        String normalized = key.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("your_") || normalized.startsWith("your-")
                || normalized.contains("placeholder") || normalized.equals("changeme")
                || normalized.equals("replace_me") || normalized.equals("replace-me");
    }

    public void setOpenAiApiKey(String apiKey) {
        set(KEY_OPENAI_API_KEY, apiKey != null ? apiKey.trim() : "");
    }

    public boolean isOfflineMode() {
        return Boolean.parseBoolean(properties.getProperty(KEY_OFFLINE_MODE, "false"));
    }

    public void setOfflineMode(boolean offline) {
        set(KEY_OFFLINE_MODE, String.valueOf(offline));
    }

    public boolean isSoundEffectsEnabled() {
        return Boolean.parseBoolean(properties.getProperty(KEY_SOUND_EFFECTS, "true"));
    }

    public void setSoundEffectsEnabled(boolean enabled) {
        set(KEY_SOUND_EFFECTS, String.valueOf(enabled));
    }

    public boolean isContinuousWhisperFlow() {
        return Boolean.parseBoolean(properties.getProperty(KEY_CONTINUOUS_FLOW, "false"));
    }

    public void setContinuousWhisperFlow(boolean continuous) {
        set(KEY_CONTINUOUS_FLOW, String.valueOf(continuous));
    }

    public int getTtsRate() {
        try {
            return Integer.parseInt(properties.getProperty(KEY_TTS_RATE, "1"));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public void setTtsRate(int rate) {
        set(KEY_TTS_RATE, String.valueOf(rate));
    }

    public boolean isTtsEnabled() {
        return Boolean.parseBoolean(properties.getProperty(KEY_TTS_ENABLED, "true"));
    }

    public void setTtsEnabled(boolean enabled) {
        set(KEY_TTS_ENABLED, String.valueOf(enabled));
    }

    public String getUserName() {
        return properties.getProperty(KEY_USER_NAME, "Boss");
    }

    public void setUserName(String name) {
        set(KEY_USER_NAME, name);
    }
}
