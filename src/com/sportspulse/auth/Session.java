package com.sportspulse.auth;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Authenticated user session model.
 *
 * Security Principles:
 * 1. Explicit cryptographically strong session token (UUIDv4) rather than a fragile global flag.
 * 2. Time-bounded validity governed by expiration timestamp.
 * 3. Explicit invalidation upon logout to prevent replay / stale session attacks.
 * 4. Encapsulates Role-Based Access Control (RBAC) authorization checks directly in business logic.
 */
public class Session {
    private final String sessionId;
    private final String username;
    private final Role role;
    private final long loginTimeEpochMs;
    private final long expiryTimeEpochMs;
    private volatile boolean active;

    public Session(String username, Role role, long durationMs) {
        this.sessionId = UUID.randomUUID().toString();
        this.username = username;
        this.role = role;
        this.loginTimeEpochMs = System.currentTimeMillis();
        this.expiryTimeEpochMs = this.loginTimeEpochMs + durationMs;
        this.active = true;
    }

    /**
     * Testing constructor allowing explicit injection of timestamps and session IDs.
     */
    public Session(String sessionId, String username, Role role, long loginTimeEpochMs, long expiryTimeEpochMs) {
        this.sessionId = sessionId;
        this.username = username;
        this.role = role;
        this.loginTimeEpochMs = loginTimeEpochMs;
        this.expiryTimeEpochMs = expiryTimeEpochMs;
        this.active = true;
    }

    public boolean isAuthenticated() {
        return active && !isExpired();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expiryTimeEpochMs;
    }

    public boolean hasRole(Role requiredRole) {
        return isAuthenticated() && this.role == requiredRole;
    }

    public void invalidate() {
        this.active = false;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public long getLoginTimeEpochMs() {
        return loginTimeEpochMs;
    }

    public long getExpiryTimeEpochMs() {
        return expiryTimeEpochMs;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());
        return String.format("Session[id=%s, user=%s, role=%s, active=%s, expires=%s]",
                sessionId.length() > 8 ? sessionId.substring(0, 8) : sessionId,
                username,
                role,
                active,
                fmt.format(Instant.ofEpochMilli(expiryTimeEpochMs)));
    }
}
