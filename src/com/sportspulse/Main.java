package com.sportspulse;

import com.sportspulse.admin.AdminConsole;
import com.sportspulse.auth.AuthenticationService;
import com.sportspulse.auth.Role;
import com.sportspulse.auth.Session;
import com.sportspulse.cli.*;
import com.sportspulse.data.MockData;
import com.sportspulse.util.Ansi;
import com.sportspulse.util.Console;
import com.sportspulse.util.MenuItem;
import com.sportspulse.util.MenuResult;
import com.sportspulse.util.TerminalMenu;

import java.util.Scanner;

/**
 * Main entry point for SportsPulse.
 *
 * Implements two-tier defensive architecture:
 * 1. Strong Authentication Flow via PBKDF2WithHmacSHA256 & Hand-Rolled Universal Hash Table.
 * 2. USER Mode: Complete SportsPulse Analytics platform (Search, Reports, Squad, Facility, Selection, Live Match).
 * 3. ADMIN Mode: Full Analytics platform + Academic Algorithm Laboratory (Hash Diagnostics, Self-Tests, Empirical Complexity Lab, CSV Export, Security Dashboard).
 */
public class Main {

    public static void main(String[] args) {
        if (args != null && args.length > 0) {
            if ("--test".equalsIgnoreCase(args[0]) || "-t".equalsIgnoreCase(args[0])) {
                SelfTest.main(args);
                return;
            } else if ("--env-check".equalsIgnoreCase(args[0]) || "-e".equalsIgnoreCase(args[0])) {
                EnvironmentCheck.main(args);
                return;
            } else if ("--help".equalsIgnoreCase(args[0]) || "-h".equalsIgnoreCase(args[0])) {
                System.out.println("SportsPulse - Sports Analytics & Performance Platform");
                System.out.println("Usage:");
                System.out.println("  java -jar dist/SportsPulse.jar             (Interactive TUI)");
                System.out.println("  java -jar dist/SportsPulse.jar --test      (Run 173-Test Verification Suite)");
                System.out.println("  java -jar dist/SportsPulse.jar --env-check (Run Host Environment Diagnostic)");
                System.out.println("  java -jar dist/SportsPulse.jar --help      (Display this help)");
                return;
            }
        }

        MockData data = new MockData();
        Scanner sc = new Scanner(System.in);
        AuthenticationService authService = new AuthenticationService();

        SearchMenu searchMenu = new SearchMenu(data, sc);
        ReportsMenu reportsMenu = new ReportsMenu(data, sc);
        SquadBuilderMenu squadMenu = new SquadBuilderMenu(data, sc);
        FacilityMenu facilityMenu = new FacilityMenu(data, sc);
        SelectionAdvisorMenu selectionMenu = new SelectionAdvisorMenu(data, sc);
        LiveMatchMenu liveMenu = new LiveMatchMenu(data, sc);

        boolean running = true;
        while (running) {
            TerminalMenu menu = new TerminalMenu("SPORTSPULSE", "Sports Analytics & Performance Platform");
            menu.setStatus("PORTAL", "PUBLIC", "READY");
            menu.addItem("1", "Login", "Access existing account");
            menu.addItem("2", "Register", "Create new USER account");
            menu.setExitOption("0", "Exit");

            MenuResult result = menu.renderAndSelect(sc);
            if (result == null) continue;
            if (result.isCancelOrExit()) {
                running = false;
                Console.success("Thank you for using SportsPulse. Goodbye!");
                break;
            }

            switch (result.getSelectedId()) {
                case "1" -> handleLogin(authService, data, sc, searchMenu, reportsMenu, squadMenu, facilityMenu, selectionMenu, liveMenu);
                case "2" -> handleRegister(authService, sc);
                default -> Console.error("Invalid choice, please select 1, 2, or 0.");
            }
        }
        sc.close();
    }

    private static void handleLogin(AuthenticationService authService, MockData data, Scanner sc,
                                    SearchMenu searchMenu, ReportsMenu reportsMenu,
                                    SquadBuilderMenu squadMenu, FacilityMenu facilityMenu,
                                    SelectionAdvisorMenu selectionMenu, LiveMatchMenu liveMenu) {
        Console.section("User Login");
        Console.prompt("Enter username: ");
        if (!sc.hasNextLine()) return;
        String username = sc.nextLine().trim();

        Console.prompt("Enter password: ");
        if (!sc.hasNextLine()) return;
        String password = sc.nextLine().trim();

        Session session = authService.login(username, password);
        if (session == null) {
            // Defensive principle: Uniform error message prevents username enumeration
            Console.error("Authentication failed: Invalid username or password.");
            Console.warn("Tip: Default accounts are 'admin' / 'Admin@SportsPulse2026!' (ADMIN) or 'coach' / 'Coach@SportsPulse2026!' (USER).");
            return;
        }

        Console.success(String.format("Authentication successful! Welcome, %s [%s]",
                session.getUsername(), session.getRole().name()));

        if (session.hasRole(Role.ADMIN)) {
            // ADMIN Flow: Open Admin Algorithm Lab (with option to open normal app)
            AdminConsole.run(session, authService, sc, () ->
                    runAnalyticsMenu(session, authService, sc, searchMenu, reportsMenu, squadMenu, facilityMenu, selectionMenu, liveMenu, true));
        } else {
            // USER Flow: Direct to standard analytics platform
            runAnalyticsMenu(session, authService, sc, searchMenu, reportsMenu, squadMenu, facilityMenu, selectionMenu, liveMenu, false);
            authService.logout(session);
        }
    }

    private static void handleRegister(AuthenticationService authService, Scanner sc) {
        Console.section("Public Account Registration");
        System.out.println(Ansi.paint(Ansi.DIM, "  Policy: Usernames must be 3-32 characters. Passwords must be at least 10 characters."));

        Console.prompt("Choose username: ");
        if (!sc.hasNextLine()) return;
        String username = sc.nextLine().trim();

        Console.prompt("Choose password (min 10 characters): ");
        if (!sc.hasNextLine()) return;
        String password = sc.nextLine().trim();

        Console.prompt("Confirm password: ");
        if (!sc.hasNextLine()) return;
        String confirmPassword = sc.nextLine().trim();

        // Security requirement: Public registration can NEVER request or choose Role.ADMIN
        AuthenticationService.AuthResult result = authService.register(username, password, confirmPassword);
        if (result.isSuccess()) {
            Console.success(result.getMessage());
        } else {
            Console.error("Registration failed: " + result.getMessage());
        }
    }

    /**
     * Standard SportsPulse Analytics menu available to authenticated users.
     */
    public static void runAnalyticsMenu(Session session, AuthenticationService authService, Scanner sc,
                                       SearchMenu searchMenu, ReportsMenu reportsMenu,
                                       SquadBuilderMenu squadMenu, FacilityMenu facilityMenu,
                                       SelectionAdvisorMenu selectionMenu, LiveMatchMenu liveMenu,
                                       boolean isAdminElevation) {
        if (session == null || !session.isAuthenticated()) {
            Console.error("Access denied: Active session required.");
            return;
        }

        boolean active = true;
        while (active) {
            if (session.isExpired()) {
                Console.error("Session has expired. Please log in again.");
                authService.logout(session);
                break;
            }

            String roleTag = isAdminElevation ? "ADMIN ELEVATION" : "USER MODE";
            TerminalMenu menu = new TerminalMenu("SPORTSPULSE ANALYTICS", "[" + roleTag + "]");
            menu.setStatus(roleTag, session.getUsername(), "SESSION: ACTIVE");
            menu.addItem("1", "Search", "players, teams, event keywords in commentary");
            menu.addItem("2", "Reports Analysis", "indexing, similarity, plagiarism");
            menu.addItem("3", "Squad Builder", "optimal lineup / budget squad selection");
            menu.addItem("4", "Facility Scheduler", "resource flow & assignment");
            menu.addItem("5", "Selection Advisor", "constraint-based compatible lineups");
            menu.addItem("6", "Live Match Center", "real-time event stream analytics");
            menu.addItem("7", "Security Settings", "Change own password with salt rotation");
            menu.setExitOption("0", isAdminElevation ? "Back to Admin Algorithm Lab" : "Logout");

            MenuResult res = menu.renderAndSelect(sc);
            if (res == null) continue;
            if (res.isCancelOrExit()) {
                active = false;
                if (!isAdminElevation) {
                    authService.logout(session);
                    Console.success("\nLogged out successfully.");
                }
                break;
            }

            switch (res.getSelectedId()) {
                case "1" -> searchMenu.run();
                case "2" -> reportsMenu.run();
                case "3" -> squadMenu.run();
                case "4" -> facilityMenu.run();
                case "5" -> selectionMenu.run();
                case "6" -> liveMenu.run();
                case "7" -> handleChangePassword(session, authService, sc);
                default -> Console.error("Invalid option, try again.");
            }
        }
    }

    private static void handleChangePassword(Session session, AuthenticationService authService, Scanner sc) {
        Console.section("CHANGE ACCOUNT PASSWORD");
        Console.prompt("Enter current password: ");
        if (!sc.hasNextLine()) return;
        String curPass = sc.nextLine().trim();

        Console.prompt("Enter new password (min 10 characters): ");
        if (!sc.hasNextLine()) return;
        String newPass = sc.nextLine().trim();

        Console.prompt("Confirm new password: ");
        if (!sc.hasNextLine()) return;
        String confirmPass = sc.nextLine().trim();

        AuthenticationService.AuthResult res = authService.changePassword(session, curPass, newPass, confirmPass);
        if (res.isSuccess()) {
            Console.success(res.getMessage());
        } else {
            Console.error("Password update rejected: " + res.getMessage());
        }
    }
}
