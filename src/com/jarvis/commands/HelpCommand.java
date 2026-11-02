package com.jarvis.commands;

import com.jarvis.core.CommandContext;
import com.jarvis.core.CommandResult;

import java.util.List;

/**
 * Command providing a comprehensive manual of voice triggers and capabilities.
 * Demonstrates INHERITANCE and system discoverability.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Sharon (TLY25CS104)
 */
public class HelpCommand extends AbstractCommand {

    private final CommandRegistry registry;

    public HelpCommand(CommandRegistry registry) {
        super("Help Manual", "Lists all supported voice commands, examples, and capabilities.",
                new String[]{"help", "what can you do", "commands list", "manual"});
        this.registry = registry;
    }

    @Override
    public boolean canHandle(String intent, String rawInput) {
        if ("HELP".equalsIgnoreCase(intent)) return true;
        String lower = rawInput.toLowerCase().trim();
        return lower.equals("help") || lower.contains("what can you do") ||
               lower.contains("commands") || lower.contains("manual") || lower.contains("capabilities");
    }

    @Override
    protected CommandResult doExecute(CommandContext context) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("📋 JARVIS VOICE & TEXT COMMANDS MANUAL:\n\n");

        List<Command> commands = registry.getAllCommands();
        for (Command cmd : commands) {
            sb.append("🔹 ").append(cmd.getName()).append(":\n");
            sb.append("   • Purpose: ").append(cmd.getDescription()).append("\n");
            sb.append("   • Examples: ");
            String[] examples = cmd.getExampleTriggers();
            for (int i = 0; i < examples.length; i++) {
                sb.append("\"").append(examples[i]).append("\"");
                if (i < examples.length - 1) sb.append(", ");
            }
            sb.append("\n\n");
        }

        sb.append("💡 Assistant features:\n");
        sb.append("• Push-to-Talk: Click the Arc Reactor or Mic button to speak.\n");
        sb.append("• Keyboard Input: Type in the prompt box and hit Enter.\n");
        sb.append("• AI chat: Ask open-ended questions; requires an online Groq API key.\n");
        sb.append("• Notes: ‘take a note …’ saves locally; ‘read my notes’ lists recent notes.\n");
        sb.append("• Clipboard: ‘copy to clipboard …’ copies text you provide; JARVIS never reads clipboard contents.\n");
        sb.append("• Reminders: list with ‘list reminders’ and cancel with ‘cancel reminder [ID]’.\n");
        sb.append("• Weather: Ask ‘what's the weather in [city]?’ for current conditions.\n");
        sb.append("• App launches use supported shortcuts only; unrecognized app names are never run as shell commands.");

        String speech = "JARVIS can answer open questions, save notes, check current weather, launch supported apps, set reminders and timers, search the web, control media, and report system information. Say help for examples.";
        return CommandResult.success(sb.toString(), speech);
    }
}
