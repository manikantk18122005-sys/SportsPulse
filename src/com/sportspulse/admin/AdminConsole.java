package com.sportspulse.admin;

import com.sportspulse.SelfTest;
import com.sportspulse.auth.AccountStatus;
import com.sportspulse.auth.AuthenticationEvent;
import com.sportspulse.auth.AuthenticationService;
import com.sportspulse.auth.Role;
import com.sportspulse.auth.Session;
import com.sportspulse.auth.User;
import com.sportspulse.benchmark.BenchmarkExporter;
import com.sportspulse.benchmark.BenchmarkResult;
import com.sportspulse.benchmark.BenchmarkRunner;
import com.sportspulse.hashing.CustomHashTable;
import com.sportspulse.util.Ansi;
import com.sportspulse.util.Console;
import com.sportspulse.util.MenuItem;
import com.sportspulse.util.MenuResult;
import com.sportspulse.util.TerminalMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Administrative Algorithm Laboratory & Security Console.
 *
 * Security & Authorization:
 * - Enforces business logic authorization: verifies that the session is active,
 *   unexpired, and possesses the elevated Role.ADMIN permission.
 * - Centralizes security telemetry, credential store statistics, audit logging,
 *   and administrative account management.
 */
public final class AdminConsole {

    @FunctionalInterface
    public interface UserMenuRunner {
        void run();
    }

    private AdminConsole() {}

    /**
     * Entry point for elevated admin operations.
     * Enforces active session validation directly in business logic.
     */
    public static void run(Session adminSession, AuthenticationService authService, Scanner sc, UserMenuRunner userMenuRunner) {
        // Strict Business Logic Authorization Check (Cannot be bypassed by UI tricks)
        if (adminSession == null || !adminSession.isAuthenticated() || !adminSession.hasRole(Role.ADMIN)) {
            throw new SecurityException("Access Denied: Legitimate, unexpired ADMIN session required.");
        }

        boolean active = true;
        while (active) {
            // Check dynamic session timeout at every interaction
            if (adminSession.isExpired() || !adminSession.isActive()) {
                Console.error("\nYour administrative session has expired due to inactivity. Re-authentication required.");
                authService.logout(adminSession);
                break;
            }

            TerminalMenu menu = new TerminalMenu("SPORTSPULSE ADMIN ALGORITHM LAB", "Academic Algorithm Laboratory & Security Telemetry");
            menu.setStatus("ADMIN LAB", adminSession.getUsername(), "FULL PRIVILEGE");
            menu.addItem("1", "Open SportsPulse", "Access standard user analytics experience", "[ANALYTICS]");
            menu.addItem("2", "Hash Table Internals", "Universal hashing, collisions, bucket distribution", "[M7]");
            menu.addItem("3", "Authentication Dashboard", "Account states, login telemetry, table load", "[SECURITY]");
            menu.addItem("4", "Security Audit Event Log", "Real-time immutable security event trail", "[AUDIT]");
            menu.addItem("5", "User & Privilege Mgmt", "Create ADMIN, Reset Passwords, Unlock Accounts", "[AUTH]");
            menu.addItem("6", "Algorithm Self-Test Suite", "Automated regression + Security verification", "[TEST]");
            menu.addItem("7", "Empirical Complexity Lab", "Live runtime measurements & ASCII graphs", "[BENCH]");
            menu.addItem("8", "Algorithm Matrix Mapping", "Theoretical vs practical syllabus review", "[SYLLABUS]");
            menu.addItem("9", "View Benchmark Results", "Session empirical run history", "[HISTORY]");
            menu.addItem("10", "Export Benchmark Results", "Output CSV for academic submission", "[CSV]");
            menu.addItem("11", "Host Portability & Env Check", "Verify JDK, paths, writable runtime", "[PORTABLE]");
            menu.setExitOption("0", "Logout");

            MenuResult res = menu.renderAndSelect(sc);
            if (res == null) continue;
            if (res.isCancelOrExit()) {
                active = false;
                authService.logout(adminSession);
                Console.success("\nLogged out from Admin Algorithm Lab. Session invalidated.");
                break;
            }

            String choice = res.getSelectedId();
            switch (choice) {
                case "1" -> {
                    Console.info("\nSwitching to standard SportsPulse analytics interface...");
                    userMenuRunner.run();
                }
                case "2" -> HashTableDiagnostics.run(authService, sc);
                case "3" -> showAuthStats(authService);
                case "4" -> showSecurityLog(authService);
                case "5" -> manageUsersAndPrivileges(adminSession, authService, sc);
                case "6" -> runSelfTests();
                case "7" -> BenchmarkConsole.run(sc);
                case "8" -> showAlgorithmMatrix();
                case "9" -> viewBenchmarkResults();
                case "10" -> exportBenchmarkResults(sc);
                case "11" -> com.sportspulse.EnvironmentCheck.run();
                default -> Console.error("Invalid admin option.");
            }
            if (active && !choice.equals("1")) Console.pause(sc);
        }
    }

    private static void showAuthStats(AuthenticationService authService) {
        Console.section("AUTHENTICATION & CREDENTIAL STORE STATISTICS");
        CustomHashTable table = authService.getCredentialTable();
        List<User> users = authService.getAllUsers();

        System.out.println(Ansi.paint(Ansi.BOLD + Ansi.BRIGHT_WHITE, "  [Account States & User Metrics]"));
        Console.kv("Total Registered Accounts", users.size());
        Console.kv("Active Accounts", authService.getActiveUsersCount());
        Console.kv("Temporarily Locked Accounts", authService.getLockedUsersCount());
        Console.kv("Disabled Accounts", authService.getDisabledUsersCount());

        System.out.println();
        System.out.println(Ansi.paint(Ansi.BOLD + Ansi.BRIGHT_WHITE, "  [Authentication Telemetry]"));
        Console.kv("Successful Logins (Session)", authService.getLoginSuccessCount());
        Console.kv("Failed Login Attempts (Session)", authService.getLoginFailureCount());
        Console.kv("Total Registrations (Session)", authService.getRegistrationCount());
        Console.kv("Current Active Sessions", authService.getActiveSessionsCount());

        System.out.println();
        System.out.println(Ansi.paint(Ansi.BOLD + Ansi.BRIGHT_WHITE, "  [Credential Hash Table Internals]"));
        Console.kv("Table Capacity (m)", table.getCapacity());
        Console.kv("Table Size (n)", table.getSize());
        Console.kv("Current Load Factor", String.format("%.4f", table.getLoadFactor()));
        Console.kv("Total Collisions Recorded", table.getCollisionCount());
        Console.kv("Universal Formula", table.getHashFunction().getFormula());

        System.out.println();
        System.out.println(Ansi.paint(Ansi.BOLD + Ansi.BRIGHT_WHITE, "  Registered Accounts in Hand-Rolled Table:"));
        List<String> headers = List.of("Username", "Role", "Status", "Failed Tries");
        List<List<String>> rows = new ArrayList<>();
        for (User u : users) {
            String statusStr = u.isLocked() ? Ansi.paint(Ansi.BRIGHT_RED, "LOCKED") :
                    (u.getStatus() == AccountStatus.ACTIVE ? Ansi.paint(Ansi.BRIGHT_GREEN, "ACTIVE") : Ansi.paint(Ansi.DIM, "DISABLED"));
            String roleStr = u.isAdmin() ? Ansi.paint(Ansi.BRIGHT_RED + Ansi.BOLD, "ADMIN") : Ansi.paint(Ansi.BRIGHT_CYAN, "USER");
            rows.add(List.of(
                    u.getUsername(),
                    roleStr,
                    statusStr,
                    String.valueOf(u.getFailedAttempts())
            ));
        }
        Console.table(headers, rows);
    }

    private static void showSecurityLog(AuthenticationService authService) {
        Console.section("AUTHENTICATION SECURITY AUDIT LOG");
        List<AuthenticationEvent> events = authService.getAuditLog();
        if (events.isEmpty()) {
            Console.warn("No authentication events recorded yet in this session.");
            return;
        }

        List<String> headers = List.of("Timestamp", "User", "Event", "Status", "Details");
        List<List<String>> rows = new ArrayList<>();
        for (AuthenticationEvent ev : events) {
            String statusColor = ev.isSuccess() ? Ansi.paint(Ansi.BRIGHT_GREEN, "SUCCESS") : Ansi.paint(Ansi.BRIGHT_RED, "REJECTED");
            rows.add(List.of(
                    ev.getFormattedTimestamp(),
                    ev.getUsername(),
                    ev.getEventType().getLabel(),
                    statusColor,
                    ev.getDetails()
            ));
        }
        Console.table(headers, rows);
    }

    private static void manageUsersAndPrivileges(Session adminSession, AuthenticationService authService, Scanner sc) {
        TerminalMenu menu = new TerminalMenu("USER & PRIVILEGE MANAGEMENT", "Administrative Role & Credential Administration");
        menu.setStatus("ADMIN", adminSession.getUsername(), "ELEVATED");
        menu.addItem("1", "Create New ADMIN Account", "Elevated Privilege Provisioning", "[ELEVATE]");
        menu.addItem("2", "Administratively Reset User Password", "Force credential rotation", "[RESET]");
        menu.addItem("3", "Unlock Temporarily Locked Account", "Clear lockout counter", "[UNLOCK]");
        menu.setExitOption("0", "Return to Admin Lab");

        MenuResult menuRes = menu.renderAndSelect(sc);
        if (menuRes == null || menuRes.isCancelOrExit()) return;
        String action = menuRes.getSelectedId();

        switch (action) {
            case "1" -> {
                Console.warn("\nCreating an ADMIN account grants full access to the Academic Algorithm Lab.");
                Console.prompt("Are you sure you want to create an elevated ADMIN account? (YES/NO): ");
                if (!sc.hasNextLine()) return;
                String confirmation = sc.nextLine().trim();
                if (!confirmation.equalsIgnoreCase("YES")) {
                    Console.info("ADMIN account creation aborted.");
                    return;
                }

                Console.prompt("Enter new administrator username: ");
                if (!sc.hasNextLine()) return;
                String uname = sc.nextLine().trim();

                Console.prompt("Enter administrator password (min 10 chars): ");
                if (!sc.hasNextLine()) return;
                String pass = sc.nextLine().trim();

                Console.prompt("Confirm administrator password: ");
                if (!sc.hasNextLine()) return;
                String confirmPass = sc.nextLine().trim();

                AuthenticationService.AuthResult res = authService.registerAdmin(adminSession, uname, pass, confirmPass, Role.ADMIN);
                if (res.isSuccess()) {
                    Console.success(res.getMessage());
                } else {
                    Console.error("Failed to create ADMIN account: " + res.getMessage());
                }
            }
            case "2" -> {
                Console.prompt("Enter username of account to reset: ");
                if (!sc.hasNextLine()) return;
                String target = sc.nextLine().trim();

                Console.prompt("Enter new password (min 10 chars): ");
                if (!sc.hasNextLine()) return;
                String newPass = sc.nextLine().trim();

                Console.prompt("Confirm new password: ");
                if (!sc.hasNextLine()) return;
                String confirmPass = sc.nextLine().trim();

                AuthenticationService.AuthResult res = authService.adminResetPassword(adminSession, target, newPass, confirmPass);
                if (res.isSuccess()) {
                    Console.success(res.getMessage());
                } else {
                    Console.error("Password reset failed: " + res.getMessage());
                }
            }
            case "3" -> {
                Console.prompt("Enter username to unlock: ");
                if (!sc.hasNextLine()) return;
                String target = sc.nextLine().trim();

                AuthenticationService.AuthResult res = authService.adminUnlockAccount(adminSession, target);
                if (res.isSuccess()) {
                    Console.success(res.getMessage());
                } else {
                    Console.error("Unlock failed: " + res.getMessage());
                }
            }
            case "0" -> Console.info("Returning to Admin Lab.");
            default -> Console.error("Invalid selection.");
        }
    }

    private static void runSelfTests() {
        Console.section("Executing Automated Self-Test Harness");
        System.out.println(Ansi.paint(Ansi.DIM, "Running all SportsPulse algorithm and defensive authentication tests...\n"));
        SelfTest.runAllTests(false);
    }

    private static void showAlgorithmMatrix() {
        Console.header("SPORTSPULSE ALGORITHM COMPARISON & SYLLABUS MAPPING");
        List<String> headers = List.of("Module", "Algorithm", "Theoretical Big-O", "Empirical Character", "SportsPulse Integration");
        List<List<String>> rows = List.of(
                List.of("Module 1: Strings", "KMP", "O(n + m)", "Guaranteed linear", "SearchMenu: match report indexing"),
                List.of("Module 1: Strings", "Z-Function", "O(n)", "Linear streaming", "SearchMenu: commentary phrase scan"),
                List.of("Module 1: Strings", "Rabin-Karp", "O(n + m) avg", "Hash-based roll", "SearchMenu: batch player ID lookup"),
                List.of("Module 1: Strings", "Aho-Corasick", "O(n + sum(m) + z)", "Automaton trie", "SearchMenu: multi-keyword match alerts"),
                List.of("Module 2: Suffix", "Suffix Array + Kasai", "O(n log n) / O(n)", "Lexicographical array", "ReportsMenu: commentary plagiarism"),
                List.of("Module 2: Suffix", "Suffix Automaton", "O(n) time & space", "Directed acyclic DAG", "ReportsMenu: minimal unique commentary"),
                List.of("Module 3: DP", "Matrix-Chain Mult", "O(n^3)", "Cubic optimal parens", "SquadBuilder: tactical formation chains"),
                List.of("Module 3: DP", "Bitmask DP", "O(n^2 * 2^n)", "Exponential subset", "SquadBuilder: optimal lineup synergy"),
                List.of("Module 3: DP", "Edit Distance", "O(n * m)", "Quadratic table", "SearchMenu: fuzzy player auto-correct"),
                List.of("Module 4: Flows", "Ford-Fulkerson", "O(E * |f|)", "DFS augmenting paths", "FacilityMenu: sports complex capacity"),
                List.of("Module 4: Flows", "Edmonds-Karp", "O(V * E^2)", "BFS shortest paths", "FacilityMenu: bottleneck min-cut"),
                List.of("Module 4: Flows", "Dinic's Algorithm", "O(V^2 * E)", "Level graph + blocks", "FacilityMenu: scalable multi-facility"),
                List.of("Module 4: Flows", "Hopcroft-Karp", "O(E * sqrt(V))", "Alternating BFS/DFS", "FacilityMenu: referee-pitch matching"),
                List.of("Module 5: NPC", "SAT / 3-SAT", "Exponential / Backtracking", "Pruning heuristic", "SelectionAdvisor: player roster rules"),
                List.of("Module 5: NPC", "3-SAT -> Clique", "Polynomial reduction", "Karp reduction gadgets", "SelectionAdvisor: compatible lineups"),
                List.of("Module 6: Rand", "Universal Hashing", "O(1) expected lookup", "Carter-Wegman family", "Authentication: hand-rolled credential table"),
                List.of("Module 6: Rand", "Randomized QuickSort", "O(n log n) expected", "Uniform random pivot", "LiveMatch: live player leaderboard sort"),
                List.of("Module 6: Rand", "Reservoir Sampling", "O(n) time, O(k) space", "Single-pass stream", "LiveMatch: commentary event sample"),
                List.of("Module 6: Rand", "Blelloch Parallel Scan", "O(n) work, O(log n) span", "Up-sweep / down-sweep", "LiveMatch: parallel cumulative scores")
        );
        Console.table(headers, rows);
    }

    private static void viewBenchmarkResults() {
        Console.section("Session Benchmark Results");
        List<BenchmarkResult> results = BenchmarkRunner.getHistory();
        if (results.isEmpty()) {
            Console.warn("No benchmark experiments have been run yet in this session.");
            return;
        }

        List<String> headers = List.of("Algorithm", "Category", "Input (n)", "Edges", "Avg Runtime", "Min Runtime", "Agreement");
        List<List<String>> rows = new ArrayList<>();
        for (BenchmarkResult r : results) {
            rows.add(List.of(
                    r.getAlgorithmName(),
                    r.getCategory(),
                    String.format("%,d", r.getInputSize()),
                    r.getEdgeCount() > 0 ? String.valueOf(r.getEdgeCount()) : "-",
                    BenchmarkResult.formatNs(r.getAverageTimeNs()),
                    BenchmarkResult.formatNs(r.getMinTimeNs()),
                    r.isVerifiedCorrect() ? Ansi.paint(Ansi.BRIGHT_GREEN, "OK") : Ansi.paint(Ansi.BRIGHT_RED, "FAIL")
            ));
        }
        Console.table(headers, rows);
    }

    private static void exportBenchmarkResults(Scanner sc) {
        Console.section("Export Benchmark Results");
        Console.prompt("Enter output CSV filename [default: benchmark_results.csv]: ");
        String name = sc.hasNextLine() ? sc.nextLine().trim() : "";
        if (name.isEmpty()) name = BenchmarkExporter.DEFAULT_CSV_PATH;
        BenchmarkExporter.exportToCsv(name);
    }
}
