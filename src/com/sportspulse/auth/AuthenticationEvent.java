package com.sportspulse.auth;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Immutable security audit event log.
 *
 * Security Principle:
 * NEVER records plaintext passwords, password hashes, salts, or credentials.
 * Captures only actors, timestamps, action categories, and diagnostic outcomes.
 */
public class AuthenticationEvent {

    public enum EventType {
        LOGIN_SUCCESS("LOGIN_SUCCESS"),
        LOGIN_FAILURE("LOGIN_FAILURE"),
        ACCOUNT_LOCKED("ACCOUNT_LOCKED"),
        ACCOUNT_UNLOCKED("ACCOUNT_UNLOCKED"),
        REGISTER_SUCCESS("REGISTER_SUCCESS"),
        PASSWORD_CHANGED("PASSWORD_CHANGED"),
        LOGOUT("LOGOUT");

        private final String label;

        EventType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private final long timestampEpochMs;
    private final String username;
    private final EventType eventType;
    private final boolean success;
    private final String details;

    public AuthenticationEvent(String username, EventType eventType, boolean success, String details) {
        this(System.currentTimeMillis(), username, eventType, success, details);
    }

    public AuthenticationEvent(long timestampEpochMs, String username, EventType eventType, boolean success, String details) {
        this.timestampEpochMs = timestampEpochMs;
        this.username = (username == null || username.trim().isEmpty()) ? "anonymous" : username.trim().toLowerCase();
        this.eventType = eventType;
        this.success = success;
        this.details = details == null ? "" : details;
    }

    public long getTimestampEpochMs() {
        return timestampEpochMs;
    }

    public String getUsername() {
        return username;
    }

    public EventType getEventType() {
        return eventType;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getDetails() {
        return details;
    }

    public String getFormattedTimestamp() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(timestampEpochMs));
    }
}
