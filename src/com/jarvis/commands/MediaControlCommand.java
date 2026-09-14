package com.jarvis.commands;

import com.jarvis.core.CommandContext;
import com.jarvis.core.CommandResult;

/**
 * Command adjusting audio volume and media playback on the host OS.
 * Demonstrates INHERITANCE and native Windows media keystroke automation.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Sharon (TLY25CS104)
 */
public class MediaControlCommand extends AbstractCommand {

    public MediaControlCommand() {
        super("Media & Volume Control", "Adjusts master system volume and media playback.",
                new String[]{"mute volume", "unmute", "volume up", "volume down", "play music"});
    }

    @Override
    public boolean canHandle(String intent, String rawInput) {
        if ("MEDIA_CONTROL".equalsIgnoreCase(intent)) return true;
        String lower = rawInput.toLowerCase();
        return lower.contains("mute") || lower.contains("unmute") || lower.contains("volume up") ||
               lower.contains("volume down") || lower.contains("play music") || lower.contains("pause music");
    }

    @Override
    protected CommandResult doExecute(CommandContext context) throws Exception {
        String input = context.getRawInput().toLowerCase().trim();

        if (input.contains("mute") || input.contains("unmute")) {
            sendWindowsMediaKey(0xAD); // VK_VOLUME_MUTE
            return CommandResult.success("🔇 Toggled Master Volume Mute", "Mute toggled.");
        } else if (input.contains("volume up") || input.contains("increase volume")) {
            for (int i = 0; i < 5; i++) {
                sendWindowsMediaKey(0xAF); // VK_VOLUME_UP
            }
            return CommandResult.success("🔊 Increased System Volume", "Volume increased.");
        } else if (input.contains("volume down") || input.contains("decrease volume") || input.contains("lower volume")) {
            for (int i = 0; i < 5; i++) {
                sendWindowsMediaKey(0xAE); // VK_VOLUME_DOWN
            }
            return CommandResult.success("🔉 Decreased System Volume", "Volume decreased.");
        } else if (input.contains("pause") || input.contains("play")) {
            sendWindowsMediaKey(0xB3); // VK_MEDIA_PLAY_PAUSE
            return CommandResult.success("⏯️ Toggled Media Play / Pause", "Media playback toggled.");
        }

        return CommandResult.failure("❌ Unknown media command.", "I did not recognize that media instruction.");
    }

    /**
     * Sends virtual key codes via a lightweight PowerShell call to user32.dll keybd_event.
     */
    private void sendWindowsMediaKey(int keyCode) {
        try {
            String ps = String.format(
                    "$wscript = New-Object -ComObject Wscript.Shell; " +
                    "$source = '[DllImport(\"user32.dll\")] public static extern void keybd_event(byte bVk, byte bScan, uint dwFlags, uint dwExtraInfo);'; " +
                    "Add-Type -MemberDefinition $source -Name 'Win32' -Namespace 'Native'; " +
                    "[Native.Win32]::keybd_event(%d, 0, 0, 0); " +
                    "[Native.Win32]::keybd_event(%d, 0, 2, 0);",
                    keyCode, keyCode
            );
            new ProcessBuilder("powershell", "-NoProfile", "-NonInteractive", "-Command", ps).start();
        } catch (Exception ignored) {}
    }
}
