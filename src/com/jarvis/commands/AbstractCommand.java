package com.jarvis.commands;

import com.jarvis.core.CommandContext;
import com.jarvis.core.CommandResult;
import com.jarvis.database.InteractionLogDAO;

/**
 * Base abstract class providing shared execution lifecycle, error handling,
 * and database audit logging for all concrete command types.
 * Demonstrates the core OOP principle of INHERITANCE and the TEMPLATE METHOD PATTERN.
 * 
 * KTU S3 CSE OOP Mini Project - Lead: Sharon (TLY25CS104)
 */
public abstract class AbstractCommand implements Command {

    protected final String name;
    protected final String description;
    protected final String[] exampleTriggers;
    protected final InteractionLogDAO logDAO;

    public AbstractCommand(String name, String description, String[] exampleTriggers) {
        this.name = name;
        this.description = description;
        this.exampleTriggers = exampleTriggers != null ? exampleTriggers : new String[0];
        this.logDAO = new InteractionLogDAO();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String[] getExampleTriggers() {
        return exampleTriggers;
    }

    /**
     * Template method: guarantees execution lifecycle, error interception,
     * and persistent relational database audit logging across all commands.
     */
    @Override
    public final CommandResult execute(CommandContext context) {
        long startTime = System.currentTimeMillis();
        CommandResult result;

        try {
            result = doExecute(context);
        } catch (Exception e) {
            String errorMsg = "Error executing " + name + ": " + e.getMessage();
            result = CommandResult.failure(errorMsg, "I encountered an error while trying to " + name.toLowerCase() + ".");
        }

        long elapsed = System.currentTimeMillis() - startTime;

        // Persist interaction audit log into SQLite tbl_interaction_logs
        try {
            logDAO.logInteraction(
                    context.getRawInput(),
                    context.getIntent(),
                    result.getStatus().name(),
                    result.getDisplayMessage()
            );
        } catch (Exception ignored) {}

        return result;
    }

    /**
     * Primitive method to be implemented by specialized concrete subclasses.
     * Demonstrates dynamic POLYMORPHISM at runtime.
     */
    protected abstract CommandResult doExecute(CommandContext context) throws Exception;
}
