package com.sportspulse.util;

import java.util.List;
import java.util.Scanner;

/**
 * Unified console presentation utility for SportsPulse.
 *
 * Backed by:
 * - TerminalRenderer (responsive headers, dividers, status bars, cursor safety)
 * - TerminalTheme (semantic sports colors, accessibility badges)
 * - TerminalTable (responsive width-aware auto-wrapping tables)
 * - ConsoleCapabilities (dynamic environment detection)
 */
public final class Console {

    private Console() {}

    public static void rule(char c, int width) {
        System.out.println(Ansi.paint(TerminalTheme.border(), String.valueOf(c).repeat(Math.max(0, width))));
    }

    public static void header(String title) {
        TerminalRenderer.renderBoxHeader(title, null);
    }

    public static void header(String title, String subtitle) {
        TerminalRenderer.renderBoxHeader(title, subtitle);
    }

    public static void status(String mode, String user, String statusInfo) {
        TerminalRenderer.renderStatusBar(mode, user, statusInfo);
    }

    public static void section(String title) {
        System.out.println();
        int width = ConsoleCapabilities.getTerminalWidth();
        TerminalRenderer.renderDivider(title, width);
    }

    public static void bullet(String text) {
        int dot = text.indexOf('.');
        if (dot > 0 && dot <= 3 && text.substring(0, dot).chars().allMatch(Character::isDigit)) {
            String num = text.substring(0, dot + 1);
            String rest = text.substring(dot + 1);
            System.out.println("  " + Ansi.paint(TerminalTheme.accent() + Ansi.BOLD, "*") + " "
                    + Ansi.paint(TerminalTheme.primary(), num) + rest);
        } else {
            System.out.println("  " + Ansi.paint(TerminalTheme.accent() + Ansi.BOLD, "*") + " " + text);
        }
    }

    public static void kv(String key, Object value) {
        System.out.printf("  %s: %s%n", Ansi.paint(TerminalTheme.secondary(), pad(key, 28)), value);
    }

    /** Green success message paired with accessibility badge. */
    public static void success(String text) {
        System.out.println(TerminalTheme.successBadge(text));
    }

    /** Crimson error message paired with accessibility badge. */
    public static void error(String text) {
        System.out.println(TerminalTheme.errorBadge(text));
    }

    /** Amber warning message paired with accessibility badge. */
    public static void warn(String text) {
        System.out.println(TerminalTheme.warnBadge(text));
    }

    /** Cyan informational message paired with accessibility badge. */
    public static void info(String text) {
        System.out.println(TerminalTheme.infoBadge(text));
    }

    /** Prints an input prompt in bold without a trailing newline. */
    public static void prompt(String text) {
        System.out.print(Ansi.paint(Ansi.BOLD + TerminalTheme.primary(), text));
    }

    /** Responsive width-aware table with box borders. */
    public static void table(List<String> headers, List<List<String>> rows) {
        TerminalTable.of(headers, rows).print();
    }

    private static String pad(String s, int width) {
        if (s == null) s = "";
        return s + " ".repeat(Math.max(0, width - s.length()));
    }

    public static void pause(Scanner sc) {
        System.out.print(Ansi.paint(TerminalTheme.muted(), "\nPress ENTER to continue..."));
        if (sc != null && sc.hasNextLine()) {
            sc.nextLine();
        }
    }
}
