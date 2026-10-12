package com.sportspulse.auth;

/**
 * Strict username validation and canonicalization policy.
 *
 * Requirements:
 * - Length between MIN_LENGTH (3) and MAX_LENGTH (32).
 * - Characters restricted to lowercase alphanumeric, '.', '_', and '-'.
 * - Canonicalized by trimming whitespace and converting to lowercase.
 * - Consistent canonicalization prevents multi-account spoofing (e.g. 'Admin', 'admin', 'ADMIN').
 */
public final class UsernamePolicy {
    public static final int MIN_LENGTH = 3;
    public static final int MAX_LENGTH = 32;
    public static final String ALLOWED_PATTERN = "^[a-z0-9_.-]+$";

    private UsernamePolicy() {}

    /**
     * Canonicalizes a username by trimming whitespace and converting to lowercase.
     */
    public static String canonicalize(String rawUsername) {
        if (rawUsername == null) return "";
        return rawUsername.trim().toLowerCase();
    }

    /**
     * Validates a raw username against length and allowed character policies.
     */
    public static ValidationResult validate(String rawUsername) {
        if (rawUsername == null || rawUsername.trim().isEmpty()) {
            return ValidationResult.error("Username cannot be empty or whitespace.");
        }
        String canonical = canonicalize(rawUsername);
        if (canonical.length() < MIN_LENGTH) {
            return ValidationResult.error(String.format("Username must be at least %d characters long.", MIN_LENGTH));
        }
        if (canonical.length() > MAX_LENGTH) {
            return ValidationResult.error(String.format("Username cannot exceed %d characters.", MAX_LENGTH));
        }
        if (!canonical.matches(ALLOWED_PATTERN)) {
            return ValidationResult.error("Username may only contain letters, numbers, '.', '_', and '-'.");
        }
        return ValidationResult.ok();
    }
}
