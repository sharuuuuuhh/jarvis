package com.jarvis.core;

/**
 * Immutable result container returned by all Command execution methods.
 * Demonstrates ENCAPSULATION of execution status, user messages, and speech responses.
 * 
 * KTU S3 CSE OOP Mini Project - JARVIS
 */
public class CommandResult {
    public enum Status {
        SUCCESS,
        FAILED,
        PARTIAL_SUCCESS,
        UNKNOWN_COMMAND
    }

    private final Status status;
    private final String displayMessage;
    private final String spokenResponse;
    private final Object payload;

    public CommandResult(Status status, String displayMessage, String spokenResponse, Object payload) {
        this.status = status;
        this.displayMessage = displayMessage != null ? displayMessage : "";
        this.spokenResponse = spokenResponse != null ? spokenResponse : "";
        this.payload = payload;
    }

    public static CommandResult success(String displayMessage, String spokenResponse) {
        return new CommandResult(Status.SUCCESS, displayMessage, spokenResponse, null);
    }

    public static CommandResult success(String displayMessage, String spokenResponse, Object payload) {
        return new CommandResult(Status.SUCCESS, displayMessage, spokenResponse, payload);
    }

    public static CommandResult failure(String displayMessage, String spokenResponse) {
        return new CommandResult(Status.FAILED, displayMessage, spokenResponse, null);
    }

    public static CommandResult unknown(String rawInput) {
        String msg = "I didn't quite catch that: \"" + rawInput + "\". Say \"help\" for supported commands.";
        return new CommandResult(Status.UNKNOWN_COMMAND, msg, "I am sorry, I do not know how to handle that command yet.", null);
    }

    public Status getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS || status == Status.PARTIAL_SUCCESS;
    }

    public String getDisplayMessage() {
        return displayMessage;
    }

    public String getSpokenResponse() {
        return spokenResponse;
    }

    public Object getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "CommandResult[status=" + status + ", message=" + displayMessage + "]";
    }
}
