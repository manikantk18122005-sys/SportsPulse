package com.sportspulse.auth;

/**
 * Defensive password policy enforcement for SportsPulse.
 *
 * Academic & Security Principles:
 * - Emphasizes length over arbitrary character checklists:
 *   NIST SP 800-63B guidelines recommend minimum length (>= 10 characters) as the most
 *   reliable defense against brute-force entropy exhaustion without encouraging bad mnemonic hacks.
 * - Enforces minimum 10 characters, maximum 128 characters.
 * - Enforces confirmation matching during registration to prevent lockout from typos.
 * - Sensitive passwords are never exposed in error messages or logs.
 */
public final class PasswordPolicy {
    public static final int MIN_LENGTH = 10;
    public static final int MAX_LENGTH = 128;

    private PasswordPolicy() {}

    /**
     * Validates candidate password length and boundary rules.
     */
    public static ValidationResult validate(String password) {
        if (password == null || password.isEmpty()) {
            return ValidationResult.error("Password cannot be empty.");
        }
        if (password.length() < MIN_LENGTH) {
            return ValidationResult.error(String.format("Password must be at least %d characters long.", MIN_LENGTH));
        }
        if (password.length() > MAX_LENGTH) {
            return ValidationResult.error(String.format("Password cannot exceed %d characters.", MAX_LENGTH));
        }
        return ValidationResult.ok();
    }

    /**
     * Validates candidate password and confirms exact equality with confirmation input.
     */
    public static ValidationResult validateConfirmation(String password, String confirmPassword) {
        ValidationResult initial = validate(password);
        if (!initial.isValid()) return initial;
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            return ValidationResult.error("Password and confirmation password do not match.");
        }
        return ValidationResult.ok();
    }
}
