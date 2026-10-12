package com.sportspulse.auth;

/**
 * Authorization roles supported by SportsPulse.
 *
 * USER: Standard sports analyst / viewer access to the 6 core analytics modules.
 * ADMIN: Elevated system role with full access to SportsPulse plus the
 *        Empirical Algorithm Laboratory, Hash Table Internals, and Self-Test harness.
 */
public enum Role {
    USER("Sports Analyst / User", "Access to core sports analytics and performance modules"),
    ADMIN("System Administrator", "Full access to SportsPulse + Algorithm Diagnostics Lab");

    private final String title;
    private final String description;

    Role(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
