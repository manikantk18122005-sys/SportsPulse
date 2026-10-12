package com.sportspulse.auth;

/**
 * Status lifecycle of a user account in SportsPulse.
 *
 * ACTIVE: Normal authenticated operations permitted.
 * LOCKED: Account temporarily locked due to exceeding consecutive failed login threshold.
 * DISABLED: Administratively deactivated account.
 */
public enum AccountStatus {
    ACTIVE("Active"),
    LOCKED("Temporarily Locked"),
    DISABLED("Disabled / Deactivated");

    private final String display;

    AccountStatus(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }
}
