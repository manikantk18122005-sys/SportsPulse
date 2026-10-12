package com.sportspulse.auth;

import com.sportspulse.hashing.CustomHashTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Defensive Authentication and Role-Based Authorization Service for SportsPulse.
 *
 * Separation of Concerns:
 * 1. PASSWORD SECURITY: PBKDF2WithHmacSHA256, unique salts, constant-time compare, dummy work.
 * 2. HASH TABLE INDEXING: Hand-rolled Carter-Wegman universal hashing for O(1) expected lookup.
 * 3. AUTHENTICATION: Strict username/password policies, account status checking, lockout.
 * 4. AUTHORIZATION: Session-based Role-Based Access Control (USER vs ADMIN).
 * 5. SESSION MANAGEMENT: Expiring, invalidatable, cryptographically identified sessions.
 * 6. AUDITING: Structured event logging without sensitive data leaks.
 */
public class AuthenticationService {

    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final long DEFAULT_LOCKOUT_DURATION_MS = 15 * 60 * 1000L; // 15 minutes
    public static final long SESSION_TIMEOUT_MS = 30 * 60 * 1000L;          // 30 minutes

    private final CustomHashTable credentialTable;
    private final List<AuthenticationEvent> auditLog;
    private final List<Session> activeSessions;

    private int loginSuccessCount;
    private int loginFailureCount;
    private int registrationCount;

    public AuthenticationService() {
        this(new CustomHashTable(17));
    }

    public AuthenticationService(CustomHashTable table) {
        this.credentialTable = table;
        this.auditLog = new ArrayList<>();
        this.activeSessions = new ArrayList<>();
        this.loginSuccessCount = 0;
        this.loginFailureCount = 0;
        this.registrationCount = 0;
        seedDefaultAccounts();
    }

    /**
     * Seeds initial default system accounts using strong passwords meeting the 10+ char policy.
     */
    private void seedDefaultAccounts() {
        registerDirect("admin", "Admin@SportsPulse2026!", Role.ADMIN);
        registerDirect("coach", "Coach@SportsPulse2026!", Role.USER);
        registerDirect("analyst", "Analyst@SportsPulse2026!", Role.USER);
    }

    /**
     * Internal direct account registration (bypasses confirmation checks for system setup).
     */
    public synchronized void registerDirect(String rawUsername, String plaintextPassword, Role role) {
        String canonical = UsernamePolicy.canonicalize(rawUsername);
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword(plaintextPassword, salt);
        User user = new User(canonical, hash, salt, role);
        credentialTable.insert(canonical, user);
        registrationCount++;
        auditLog.add(new AuthenticationEvent(canonical,
                AuthenticationEvent.EventType.REGISTER_SUCCESS, true,
                "Direct registration completed with role " + role.name()));
    }

    /**
     * Public user registration.
     * Enforces:
     * 1. Strict username validation & canonicalization.
     * 2. Defensive password policy & confirmation matching.
     * 3. Duplicate username prevention.
     * 4. Enforces Role.USER only — public registration CANNOT create ADMIN accounts.
     */
    public synchronized AuthResult register(String rawUsername, String password, String confirmPassword) {
        ValidationResult userVal = UsernamePolicy.validate(rawUsername);
        if (!userVal.isValid()) {
            return AuthResult.failure(userVal.getMessage());
        }

        ValidationResult passVal = PasswordPolicy.validateConfirmation(password, confirmPassword);
        if (!passVal.isValid()) {
            return AuthResult.failure(passVal.getMessage());
        }

        String canonical = UsernamePolicy.canonicalize(rawUsername);
        if (credentialTable.contains(canonical)) {
            return AuthResult.failure("Username '" + canonical + "' is already registered.");
        }

        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword(password, salt);
        User user = new User(canonical, hash, salt, Role.USER);
        credentialTable.insert(canonical, user);
        registrationCount++;

        auditLog.add(new AuthenticationEvent(canonical,
                AuthenticationEvent.EventType.REGISTER_SUCCESS, true,
                "Public user registered with default role USER"));

        return AuthResult.success(user, "User '" + canonical + "' successfully registered as USER.");
    }

    /**
     * Overload for test compatibility: rejects public creation of ADMIN role.
     */
    public synchronized AuthResult register(String rawUsername, String password, Role role) {
        if (role == Role.ADMIN) {
            return AuthResult.failure("Public registration cannot create ADMIN accounts. Administrative authorization required.");
        }
        return register(rawUsername, password, password);
    }

    /**
     * Administrative account creation: Allows an authenticated ADMIN to create another ADMIN or USER account.
     */
    public synchronized AuthResult registerAdmin(Session adminSession, String rawUsername, String password, String confirmPassword, Role targetRole) {
        if (adminSession == null || !adminSession.isAuthenticated() || !adminSession.hasRole(Role.ADMIN)) {
            return AuthResult.failure("Unauthorized: An active ADMIN session is required to create privileged accounts.");
        }

        ValidationResult userVal = UsernamePolicy.validate(rawUsername);
        if (!userVal.isValid()) return AuthResult.failure(userVal.getMessage());

        ValidationResult passVal = PasswordPolicy.validateConfirmation(password, confirmPassword);
        if (!passVal.isValid()) return AuthResult.failure(passVal.getMessage());

        String canonical = UsernamePolicy.canonicalize(rawUsername);
        if (credentialTable.contains(canonical)) {
            return AuthResult.failure("Username '" + canonical + "' is already registered.");
        }

        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword(password, salt);
        User user = new User(canonical, hash, salt, targetRole != null ? targetRole : Role.USER);
        credentialTable.insert(canonical, user);
        registrationCount++;

        auditLog.add(new AuthenticationEvent(canonical,
                AuthenticationEvent.EventType.REGISTER_SUCCESS, true,
                "Privileged account created as " + user.getRole().name() + " by admin '" + adminSession.getUsername() + "'"));

        return AuthResult.success(user, "Account '" + canonical + "' created as " + user.getRole().name() + ".");
    }

    /**
     * Defensive Authentication Pipeline.
     *
     * Steps:
     * 1. Canonicalize input username.
     * 2. Lookup user in CustomHashTable via Carter-Wegman universal hash function.
     * 3. Non-existent username -> Execute dummy PBKDF2 work to neutralize timing attacks; return generic rejection.
     * 4. Account locked -> Check dynamic expiration; if still locked, execute dummy work, return generic rejection.
     * 5. Verify candidate password using constant-time MessageDigest.isEqual().
     * 6. Verification failure -> Increment failed attempts; trigger temporary lockout if threshold exceeded; return generic rejection.
     * 7. Verification success -> Reset failures, instantiate cryptographically indexed Session, return session.
     */
    public synchronized Session login(String rawUsername, String rawPassword) {
        if (rawUsername == null || rawPassword == null) {
            PasswordHasher.performDummyWork();
            loginFailureCount++;
            return null;
        }

        String canonical = UsernamePolicy.canonicalize(rawUsername);
        User user = credentialTable.search(canonical);

        // Defense against username enumeration: perform identical KDF work for missing users
        if (user == null) {
            PasswordHasher.performDummyWork();
            loginFailureCount++;
            auditLog.add(new AuthenticationEvent(canonical,
                    AuthenticationEvent.EventType.LOGIN_FAILURE, false,
                    "Authentication rejected: User not found in credential store"));
            return null;
        }

        // Account status verification
        if (user.getStatus() == AccountStatus.DISABLED) {
            PasswordHasher.performDummyWork();
            loginFailureCount++;
            auditLog.add(new AuthenticationEvent(canonical,
                    AuthenticationEvent.EventType.LOGIN_FAILURE, false,
                    "Authentication rejected: Account is administratively disabled"));
            return null;
        }

        if (user.isLocked()) {
            PasswordHasher.performDummyWork();
            loginFailureCount++;
            auditLog.add(new AuthenticationEvent(canonical,
                    AuthenticationEvent.EventType.LOGIN_FAILURE, false,
                    "Authentication rejected: Account is temporarily locked until " + user.getLockoutUntilEpochMs()));
            return null;
        }

        // Constant-time password verification
        boolean valid = PasswordHasher.verify(rawPassword, user.getSalt(), user.getPasswordHash());

        if (!valid) {
            loginFailureCount++;
            boolean lockedNow = user.recordFailedAttempt(MAX_FAILED_ATTEMPTS, DEFAULT_LOCKOUT_DURATION_MS);
            if (lockedNow) {
                auditLog.add(new AuthenticationEvent(canonical,
                        AuthenticationEvent.EventType.ACCOUNT_LOCKED, false,
                        "Consecutive failure threshold (" + MAX_FAILED_ATTEMPTS + ") reached; account temporarily locked"));
            } else {
                auditLog.add(new AuthenticationEvent(canonical,
                        AuthenticationEvent.EventType.LOGIN_FAILURE, false,
                        "Invalid password attempt (" + user.getFailedAttempts() + "/" + MAX_FAILED_ATTEMPTS + ")"));
            }
            return null;
        }

        // Successful authentication
        user.recordSuccessfulLogin();
        loginSuccessCount++;

        Session session = new Session(canonical, user.getRole(), SESSION_TIMEOUT_MS);
        activeSessions.add(session);

        auditLog.add(new AuthenticationEvent(canonical,
                AuthenticationEvent.EventType.LOGIN_SUCCESS, true,
                "Authentication successful; session token issued"));

        return session;
    }

    /**
     * Terminates an active session and registers an audit event.
     */
    public synchronized void logout(Session session) {
        if (session != null) {
            session.invalidate();
            activeSessions.remove(session);
            auditLog.add(new AuthenticationEvent(session.getUsername(),
                    AuthenticationEvent.EventType.LOGOUT, true,
                    "User initiated session termination"));
        }
    }

    /**
     * Allows an authenticated user to change their password.
     */
    public synchronized AuthResult changePassword(Session session, String currentPassword, String newPassword, String confirmNewPassword) {
        if (session == null || !session.isAuthenticated()) {
            return AuthResult.failure("Authentication required to change password.");
        }

        ValidationResult passVal = PasswordPolicy.validateConfirmation(newPassword, confirmNewPassword);
        if (!passVal.isValid()) {
            return AuthResult.failure(passVal.getMessage());
        }

        User user = credentialTable.search(session.getUsername());
        if (user == null) {
            return AuthResult.failure("User account not found.");
        }

        if (!PasswordHasher.verify(currentPassword, user.getSalt(), user.getPasswordHash())) {
            return AuthResult.failure("Current password does not match.");
        }

        // Generate a FRESH salt and FRESH hash (never reuse salt)
        String newSalt = PasswordHasher.generateSalt();
        String newHash = PasswordHasher.hashPassword(newPassword, newSalt);
        user.updateCredentials(newHash, newSalt);

        auditLog.add(new AuthenticationEvent(session.getUsername(),
                AuthenticationEvent.EventType.PASSWORD_CHANGED, true,
                "User successfully updated password with fresh salt rotation"));

        return AuthResult.success(user, "Password updated successfully.");
    }

    /**
     * Allows an authenticated ADMIN to administratively reset a user's password.
     */
    public synchronized AuthResult adminResetPassword(Session adminSession, String targetUsername, String newPassword, String confirmNewPassword) {
        if (adminSession == null || !adminSession.isAuthenticated() || !adminSession.hasRole(Role.ADMIN)) {
            return AuthResult.failure("Unauthorized: Admin privileges required.");
        }

        ValidationResult passVal = PasswordPolicy.validateConfirmation(newPassword, confirmNewPassword);
        if (!passVal.isValid()) {
            return AuthResult.failure(passVal.getMessage());
        }

        String canonical = UsernamePolicy.canonicalize(targetUsername);
        User user = credentialTable.search(canonical);
        if (user == null) {
            return AuthResult.failure("User '" + canonical + "' does not exist.");
        }

        String newSalt = PasswordHasher.generateSalt();
        String newHash = PasswordHasher.hashPassword(newPassword, newSalt);
        user.updateCredentials(newHash, newSalt);
        user.unlock(); // Auto-unlock if locked

        auditLog.add(new AuthenticationEvent(canonical,
                AuthenticationEvent.EventType.PASSWORD_CHANGED, true,
                "Password administratively reset by admin '" + adminSession.getUsername() + "'"));

        return AuthResult.success(user, "Password for '" + canonical + "' reset successfully.");
    }

    /**
     * Allows an authenticated ADMIN to unlock a locked user account.
     */
    public synchronized AuthResult adminUnlockAccount(Session adminSession, String targetUsername) {
        if (adminSession == null || !adminSession.isAuthenticated() || !adminSession.hasRole(Role.ADMIN)) {
            return AuthResult.failure("Unauthorized: Admin privileges required.");
        }

        String canonical = UsernamePolicy.canonicalize(targetUsername);
        User user = credentialTable.search(canonical);
        if (user == null) {
            return AuthResult.failure("User '" + canonical + "' not found.");
        }

        user.unlock();
        auditLog.add(new AuthenticationEvent(canonical,
                AuthenticationEvent.EventType.ACCOUNT_UNLOCKED, true,
                "Account unlocked by admin '" + adminSession.getUsername() + "'"));

        return AuthResult.success(user, "Account '" + canonical + "' unlocked successfully.");
    }

    /**
     * Seeds demonstration accounts to induce and inspect hash collisions in the custom table.
     */
    public synchronized int seedDemonstrationUsers() {
        String[] demoNames = {"alice", "bob", "charan", "rahul", "zack", "elena", "marcus", "sophia"};
        int added = 0;
        for (String name : demoNames) {
            if (!credentialTable.contains(name)) {
                registerDirect(name, "DemoPassword#2026", Role.USER);
                added++;
            }
        }
        return added;
    }

    // =========================================================================
    // AUDITING & METRICS ACCESSORS
    // =========================================================================

    public CustomHashTable getCredentialTable() { return credentialTable; }
    public List<AuthenticationEvent> getAuditLog() { return Collections.unmodifiableList(auditLog); }
    public int getLoginSuccessCount() { return loginSuccessCount; }
    public int getLoginFailureCount() { return loginFailureCount; }
    public int getRegistrationCount() { return registrationCount; }
    public List<User> getAllUsers() { return credentialTable.getAllUsers(); }

    public synchronized long getActiveUsersCount() {
        return getAllUsers().stream().filter(u -> u.getStatus() == AccountStatus.ACTIVE).count();
    }

    public synchronized long getLockedUsersCount() {
        return getAllUsers().stream().filter(User::isLocked).count();
    }

    public synchronized long getDisabledUsersCount() {
        return getAllUsers().stream().filter(u -> u.getStatus() == AccountStatus.DISABLED).count();
    }

    public synchronized int getActiveSessionsCount() {
        activeSessions.removeIf(Session::isExpired);
        return (int) activeSessions.stream().filter(Session::isAuthenticated).count();
    }

    /**
     * Result wrapper for user-facing authentication and registration actions.
     */
    public static class AuthResult {
        private final boolean success;
        private final User user;
        private final String message;

        private AuthResult(boolean success, User user, String message) {
            this.success = success;
            this.user = user;
            this.message = message;
        }

        public static AuthResult success(User user, String message) {
            return new AuthResult(true, user, message);
        }

        public static AuthResult failure(String message) {
            return new AuthResult(false, null, message);
        }

        public boolean isSuccess() { return success; }
        public User getUser() { return user; }
        public String getMessage() { return message; }
    }
}
