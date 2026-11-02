package com.jarvis.commands;

import com.jarvis.core.CommandContext;
import com.jarvis.core.CommandResult;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * Command inspecting real-time host operating system telemetry and hardware metrics.
 * Demonstrates INHERITANCE and native Java Management runtime inspection.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Sharon (TLY25CS104)
 */
public class SystemInfoCommand extends AbstractCommand {

    public SystemInfoCommand() {
        super("System Telemetry", "Inspects CPU load, RAM utilization, OS version, and disk health.",
                new String[]{"system info", "check cpu usage", "check ram", "battery status"});
    }

    @Override
    public boolean canHandle(String intent, String rawInput) {
        if ("SYSTEM_INFO".equalsIgnoreCase(intent)) return true;
        String lower = rawInput.toLowerCase();
        return lower.contains("system info") || lower.contains("system status") ||
               lower.contains("check cpu") || lower.contains("ram usage") ||
               lower.contains("memory usage") || lower.contains("disk space");
    }

    @Override
    protected CommandResult doExecute(CommandContext context) throws Exception {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        Runtime runtime = Runtime.getRuntime();

        // Memory calculations
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory() / (1024 * 1024);

        // CPU cores
        int availableProcessors = runtime.availableProcessors();
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        String javaVersion = System.getProperty("java.version");

        // Primary drive disk space
        File root = new File("C:\\");
        long totalDisk = root.getTotalSpace() / (1024 * 1024 * 1024);
        long freeDisk = root.getFreeSpace() / (1024 * 1024 * 1024);
        long usedDisk = totalDisk - freeDisk;

        StringBuilder sb = new StringBuilder();
        sb.append("🖥️ SYSTEM TELEMETRY REPORT:\n");
        sb.append(String.format("• Operating System: %s (%s)\n", osName, osArch));
        sb.append(String.format("• CPU Architecture: %d Logical Processor Cores\n", availableProcessors));
        sb.append(String.format("• JVM Memory: %d MB Used / %d MB Allocated (Max %d MB)\n", usedMemory, totalMemory, maxMemory));
        sb.append(String.format("• Primary Drive (C:): %d GB Free of %d GB (Used: %d GB)\n", freeDisk, totalDisk, usedDisk));
        sb.append(String.format("• Java Runtime: OpenJDK %s\n", javaVersion));

        String speech = String.format("System is nominal. Running on %s with %d processor cores. RAM usage is currently %d megabytes.",
                osName, availableProcessors, usedMemory);

        return CommandResult.success(sb.toString(), speech);
    }
}
