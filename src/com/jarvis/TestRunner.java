package com.jarvis;

import com.jarvis.audio.AudioFormatHelper;
import com.jarvis.commands.Command;
import com.jarvis.commands.CommandFactory;
import com.jarvis.commands.CommandParser;
import com.jarvis.commands.CommandRegistry;
import com.jarvis.core.CommandContext;
import com.jarvis.core.CommandResult;
import com.jarvis.core.ConfigManager;
import com.jarvis.database.InteractionLogDAO;
import com.jarvis.database.ReminderDAO;
import com.jarvis.speech.WhisperApiClient;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Automated Verification Test Suite for KTU S3 CSE OOP Mini Project (CSL 203).
 * Validates Encapsulation, Inheritance, Polymorphism, Abstraction, and Database DAOs.
 */
public class TestRunner {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   JARVIS AUTOMATED VERIFICATION TEST SUITE");
        System.out.println("   KTU S3 CSE OOP MINI PROJECT (CSL 203)");
        System.out.println("=================================================");

        testConfigSingleton();
        testAudioFormatHelper();
        testWhisperJsonExtraction();
        testCommandParser();
        testPolymorphicCommandExecution();
        testReminderDAO();
        testInteractionLogDAO();

        System.out.println("-------------------------------------------------");
        System.out.printf("TEST SUMMARY: %d Passed, %d Failed.\n", passed, failed);
        System.out.println("=================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            passed++;
        } else {
            System.err.println("  [FAIL] " + testName);
            failed++;
        }
    }

    private static void testConfigSingleton() {
        System.out.println("\n[1] Testing ConfigManager (Singleton Pattern & Encapsulation):");
        ConfigManager c1 = ConfigManager.getInstance();
        ConfigManager c2 = ConfigManager.getInstance();
        assertTrue("ConfigManager maintains single instance (c1 == c2)", c1 == c2);
        assertTrue("ConfigManager returns valid default user name", c1.getUserName() != null && !c1.getUserName().isEmpty());
    }

    private static void testAudioFormatHelper() {
        System.out.println("\n[2] Testing AudioFormatHelper (Java Sound API & WAV Encoding):");
        try {
            byte[] dummyPcm = new byte[3200]; // 0.1s of silence
            byte[] wav = AudioFormatHelper.convertPcmToWav(dummyPcm);
            assertTrue("WAV byte buffer generated successfully", wav != null && wav.length > 44);
            // Check RIFF header 'R','I','F','F'
            assertTrue("WAV starts with RIFF magic bytes", wav[0] == 'R' && wav[1] == 'I' && wav[2] == 'F' && wav[3] == 'F');
        } catch (Exception e) {
            assertTrue("AudioFormatHelper exception: " + e.getMessage(), false);
        }
    }

    private static void testWhisperJsonExtraction() {
        System.out.println("\n[3] Testing WhisperApiClient JSON Parsing (OpenAI Whisper):");
        String sampleJson = "{\"text\": \"Hello Jarvis please open notepad\"}";
        String extracted = WhisperApiClient.extractTextFromJson(sampleJson);
        assertTrue("Extracted text from Whisper API JSON payload", "Hello Jarvis please open notepad".equals(extracted));
    }

    private static void testCommandParser() {
        System.out.println("\n[4] Testing CommandParser (NLP Intent Classification):");
        CommandParser.ParsedIntent p1 = CommandParser.parse("open notepad");
        assertTrue("App launch intent parsed correctly", "APP_LAUNCH".equals(p1.getIntent()));

        CommandParser.ParsedIntent p2 = CommandParser.parse("remind me to study in 20 seconds");
        assertTrue("Reminder intent parsed correctly", "REMINDER".equals(p2.getIntent()));

        CommandParser.ParsedIntent p3 = CommandParser.parse("system info");
        assertTrue("System info intent parsed correctly", "SYSTEM_INFO".equals(p3.getIntent()));

        CommandParser.ParsedIntent p4 = CommandParser.parse("search google for Java OOP");
        assertTrue("Web search intent parsed correctly", "WEB_SEARCH".equals(p4.getIntent()));

        CommandParser.ParsedIntent p5 = CommandParser.parse("what time is it");
        assertTrue("Time query intent parsed correctly", "GENERAL_QUERY".equals(p5.getIntent()));
    }

    private static void testPolymorphicCommandExecution() {
        System.out.println("\n[5] Testing Polymorphism & Factory Pattern (Command Execution):");
        CommandRegistry registry = new CommandRegistry();
        CommandFactory factory = new CommandFactory(registry);

        // General Query test
        Command cmdTime = factory.getCommand("GENERAL_QUERY", "what time is it");
        CommandResult resTime = cmdTime.execute(new CommandContext("what time is it", "GENERAL_QUERY", ""));
        assertTrue("Polymorphic time execution returns success", resTime.isSuccess());
        assertTrue("Time response contains current time", resTime.getDisplayMessage().contains("Current Time"));

        // Calculation test
        Command cmdMath = factory.getCommand("GENERAL_QUERY", "calculate 25 * 4");
        CommandResult resMath = cmdMath.execute(new CommandContext("calculate 25 * 4", "GENERAL_QUERY", ""));
        assertTrue("Math calculation returned 100", resMath.getDisplayMessage().contains("100"));

        // Help test
        Command cmdHelp = factory.getCommand("HELP", "help");
        CommandResult resHelp = cmdHelp.execute(new CommandContext("help", "HELP", ""));
        assertTrue("Help command displays documentation", resHelp.getDisplayMessage().contains("MANUAL"));

        // System info test
        Command cmdSys = factory.getCommand("SYSTEM_INFO", "system info");
        CommandResult resSys = cmdSys.execute(new CommandContext("system info", "SYSTEM_INFO", ""));
        assertTrue("System info inspects CPU cores", resSys.getDisplayMessage().contains("Processor Cores"));
    }

    private static void testReminderDAO() {
        System.out.println("\n[6] Testing ReminderDAO (Relational Persistence & DAO Pattern):");
        ReminderDAO dao = new ReminderDAO();
        LocalDateTime past = LocalDateTime.now().minusSeconds(1);
        boolean saved = dao.createReminder("Unit Test Verification Alarm", past, "HIGH");
        assertTrue("Reminder persisted successfully into database", saved);

        List<ReminderDAO.ReminderItem> due = dao.getPendingReminders();
        assertTrue("Pending reminder retrieved from database", !due.isEmpty());

        int id = due.get(0).getId();
        boolean completed = dao.markCompleted(id);
        assertTrue("Reminder marked as COMPLETED", completed);
    }

    private static void testInteractionLogDAO() {
        System.out.println("\n[7] Testing InteractionLogDAO (Audit Logging):");
        InteractionLogDAO logDAO = new InteractionLogDAO();
        boolean logged = logDAO.logInteraction("system info", "SYSTEM_INFO", "SUCCESS", "Telemetry inspection verified.");
        assertTrue("Interaction logged to tbl_interaction_logs", logged);
    }
}
