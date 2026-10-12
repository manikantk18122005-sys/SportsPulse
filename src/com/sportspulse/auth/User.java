package com.sportspulse.auth;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * User credential record stored within the hand-rolled CustomHashTable.
 *
 * ACADEMIC & SECURITY ARCHITECTURE:
 * 1. Plaintext passwords are NEVER stored in this entity.
 * 2. Only the salted PBKDF2WithHmacSHA256 password digest and unique salt are retained.
 * 3. The universal hash function is strictly used for hash table bucket indexing,
 *    NOT as a password hashing algorithm.
 * 4. Tracks account lifecycle state (ACTIVE, LOCKED, DISABLED), failed attempts,
 *    and temporary lockout duration.
 */
public class User {
    private final String username;
    private String passwordHash;
    private String salt;
    private final Role role;
    private AccountStatus status;
    private int failedAttempts;
    private long lockoutUntilEpochMs;
    private final long createdAtEpochMs;
    private long lastLoginEpochMs;
    private long lastPasswordChangeEpochMs;

    public User(String username, String passwordHash, String salt, Role role) {
        this(username, passwordHash, salt, role, AccountStatus.ACTIVE, 0, 0L, System.currentTimeMillis(), 0L, System.currentTimeMillis());
    }

    public User(String username, String passwordHash, String salt, Role role, long createdAtEpochMs) {
        this(username, passwordHash, salt, role, AccountStatus.ACTIVE, 0, 0L, createdAtEpochMs, 0L, createdAtEpochMs);
    }

    public User(String username, String passwordHash, String salt, Role role,
                AccountStatus status, int failedAttempts, long lockoutUntilEpochMs,
                long createdAtEpochMs, long lastLoginEpochMs, long lastPasswordChangeEpochMs) {
        this.username = username != null ? username.trim().toLowerCase() : "";
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role != null ? role : Role.USER;
        this.status = status != null ? status : AccountStatus.ACTIVE;
        this.failedAttempts = Math.max(0, failedAttempts);
        this.lockoutUntilEpochMs = Math.max(0L, lockoutUntilEpochMs);
        this.createdAtEpochMs = createdAtEpochMs;
        this.lastLoginEpochMs = lastLoginEpochMs;
        this.lastPasswordChangeEpochMs = lastPasswordChangeEpochMs;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public Role getRole() {
        return role;
    }

    public synchronized AccountStatus getStatus() {
        // Evaluate dynamic expiration of temporary lockout
        if (status == AccountStatus.LOCKED && System.currentTimeMillis() >= lockoutUntilEpochMs) {
            status = AccountStatus.ACTIVE;
            failedAttempts = 0;
            lockoutUntilEpochMs = 0L;
        }
        return status;
    }

    public synchronized int getFailedAttempts() {
        return failedAttempts;
    }

    public synchronized long getLockoutUntilEpochMs() {
        return lockoutUntilEpochMs;
    }

    public long getCreatedAtEpochMs() {
        return createdAtEpochMs;
    }

    public synchronized long getLastLoginEpochMs() {
        return lastLoginEpochMs;
    }

    public synchronized long getLastPasswordChangeEpochMs() {
        return lastPasswordChangeEpochMs;
    }

    public boolean isAdmin() {
        return role != null && role.isAdmin();
    }

    /**
     * Checks if the account is currently locked.
     * Evaluates dynamic expiration: if lockout window elapsed, auto-transitions back to ACTIVE.
     */
    public synchronized boolean isLocked() {
        if (status == AccountStatus.LOCKED) {
            if (System.currentTimeMillis() >= lockoutUntilEpochMs) {
                status = AccountStatus.ACTIVE;
                failedAttempts = 0;
                lockoutUntilEpochMs = 0L;
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Records a failed login attempt and temporarily locks the account if threshold exceeded.
     *
     * @param maxAttempts Allowed failures before lockout (e.g. 5)
     * @param lockoutDurationMs Temporary lockout window in milliseconds
     * @return true if this failure triggered a state transition to LOCKED, false otherwise
     */
    public synchronized boolean recordFailedAttempt(int maxAttempts, long lockoutDurationMs) {
        if (status == AccountStatus.DISABLED) return false;
        failedAttempts++;
        if (failedAttempts >= maxAttempts) {
            status = AccountStatus.LOCKED;
            lockoutUntilEpochMs = System.currentTimeMillis() + lockoutDurationMs;
            return true;
        }
        return false;
    }

    /**
     * Resets failed attempts and records successful authentication.
     */
    public synchronized void recordSuccessfulLogin() {
        failedAttempts = 0;
        lockoutUntilEpochMs = 0L;
        lastLoginEpochMs = System.currentTimeMillis();
    }

    /**
     * Replaces credentials with a freshly generated salt and PBKDF2 hash.
     * Never reuses old salts.
     */
    public synchronized void updateCredentials(String newHash, String newSalt) {
        this.passwordHash = newHash;
        this.salt = newSalt;
        this.failedAttempts = 0;
        this.lockoutUntilEpochMs = 0L;
        this.lastPasswordChangeEpochMs = System.currentTimeMillis();
    }

    /**
     * Administratively unlocks a locked account.
     */
    public synchronized void unlock() {
        if (status == AccountStatus.LOCKED) {
            status = AccountStatus.ACTIVE;
        }
        failedAttempts = 0;
        lockoutUntilEpochMs = 0L;
    }

    public synchronized void setStatus(AccountStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        String created = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(createdAtEpochMs));
        // Security requirement: Passwords and cryptographic salts are strictly omitted
        return String.format("User{username='%s', role=%s, status=%s, created=%s}",
                username, role, getStatus(), created);
    }
}
