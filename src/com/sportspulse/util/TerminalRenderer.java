package com.sportspulse.util;

/**
 * Screen rendering primitives, box headers, status bars, and terminal safety for SportsPulse.
 *
 * Terminal Safety:
 * Registers a JVM shutdown hook to unconditionally restore the terminal cursor and
 * reset ANSI escape codes in case of unhandled termination, SIGINT / Ctrl+C, or abnormal exit.
 */
public final class TerminalRenderer {

    private static volatile boolean shutdownHookInstalled = false;

    static {
        installSafetyShutdownHook();
    }

    private TerminalRenderer() {}

    /**
     * Installs an unconditional JVM shutdown hook ensuring the cursor is never left
     * hidden and text styles are reset even on sudden termination (Ctrl+C / SIGINT).
     */
    public static void installSafetyShutdownHook() {
        if (!shutdownHookInstalled) {
            synchronized (TerminalRenderer.class) {
                if (!shutdownHookInstalled) {
                    try {
                        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                            System.out.print(Ansi.CURSOR_SHOW + Ansi.RESET);
                            System.out.flush();
                        }, "SportsPulse-TerminalSafetyHook"));
                        shutdownHookInstalled = true;
                    } catch (IllegalStateException ignored) {
                        // JVM already shutting down
                    }
                }
            }
        }
    }

    public static boolean isShutdownHookInstalled() {
        return shutdownHookInstalled;
    }

    /**
     * Clears the terminal screen and homes the cursor if running in an interactive ANSI terminal.
     * Safely falls back to newline spacing in pipes or automated test runs.
     */
    public static void clearScreen() {
        if (ConsoleCapabilities.isInteractive() && Ansi.ENABLED) {
            System.out.print(Ansi.CLEAR_SCREEN + Ansi.CURSOR_HOME);
            System.out.flush();
        } else {
            System.out.println();
        }
    }

    /**
     * Toggles cursor visibility. Must only be used when interactive.
     */
    public static void setCursorVisible(boolean visible) {
        if (ConsoleCapabilities.isInteractive() && Ansi.ENABLED) {
            System.out.print(visible ? Ansi.CURSOR_SHOW : Ansi.CURSOR_HIDE);
            System.out.flush();
        }
    }

    /**
     * Renders a responsive double-bordered box header with centered title and optional subtitle.
     */
    public static void renderBoxHeader(String title, String subtitle) {
        int width = ConsoleCapabilities.getTerminalWidth();
        boolean unicode = ConsoleCapabilities.isUnicodeSupported();

        char topLeft  = unicode ? '╔' : '+';
        char topRight = unicode ? '╗' : '+';
        char botLeft  = unicode ? '╚' : '+';
        char botRight = unicode ? '╝' : '+';
        char horiz    = unicode ? '═' : '=';
        char vert     = unicode ? '║' : '|';
        char midLeft  = unicode ? '╠' : '+';
        char midRight = unicode ? '╣' : '+';
        char midHoriz = unicode ? '─' : '-';

        String topBorder = topLeft + String.valueOf(horiz).repeat(Math.max(0, width - 2)) + topRight;
        String botBorder = botLeft + String.valueOf(horiz).repeat(Math.max(0, width - 2)) + botRight;

        System.out.println(Ansi.paint(TerminalTheme.border(), topBorder));

        // Title row
        String centerTitle = centerText(title, width - 4);
        System.out.println(Ansi.paint(TerminalTheme.border(), String.valueOf(vert)) + " "
                + Ansi.paint(Ansi.BOLD + TerminalTheme.primary(), centerTitle) + " "
                + Ansi.paint(TerminalTheme.border(), String.valueOf(vert)));

        if (subtitle != null && !subtitle.isBlank()) {
            String midSep = midLeft + String.valueOf(midHoriz).repeat(Math.max(0, width - 2)) + midRight;
            System.out.println(Ansi.paint(TerminalTheme.border(), midSep));
            String centerSub = centerText(subtitle, width - 4);
            System.out.println(Ansi.paint(TerminalTheme.border(), String.valueOf(vert)) + " "
                    + Ansi.paint(TerminalTheme.muted(), centerSub) + " "
                    + Ansi.paint(TerminalTheme.border(), String.valueOf(vert)));
        }

        System.out.println(Ansi.paint(TerminalTheme.border(), botBorder));
    }

    /**
     * Renders a high-density status bar across the terminal width.
     */
    public static void renderStatusBar(String mode, String user, String statusInfo) {
        int width = ConsoleCapabilities.getTerminalWidth();
        boolean unicode = ConsoleCapabilities.isUnicodeSupported();
        String onlineDot = unicode ? "● " : "* ";

        String left = " " + onlineDot + "MODE: " + mode + " | USER: " + user;
        String right = statusInfo + " | " + width + "C ";

        int pad = width - Ansi.visibleLength(left) - Ansi.visibleLength(right);
        if (pad < 1) pad = 1;

        String barContent = left + " ".repeat(pad) + right;
        String bar = Ansi.paint(Ansi.BOLD + TerminalTheme.headerBg() + TerminalTheme.accent(), barContent);
        System.out.println(bar);
    }

    public static void renderDivider(String label, int width) {
        boolean unicode = ConsoleCapabilities.isUnicodeSupported();
        char ch = unicode ? '─' : '-';
        if (label == null || label.isBlank()) {
            System.out.println(Ansi.paint(TerminalTheme.border(), String.valueOf(ch).repeat(Math.max(0, width))));
        } else {
            String prefix = String.valueOf(ch).repeat(3) + " " + label + " ";
            int remaining = Math.max(0, width - prefix.length());
            System.out.println(Ansi.paint(TerminalTheme.border(), prefix + String.valueOf(ch).repeat(remaining)));
        }
    }

    public static String centerText(String text, int width) {
        if (text == null) text = "";
        int visibleLen = Ansi.visibleLength(text);
        if (visibleLen >= width) return text;
        int leftPad = (width - visibleLen) / 2;
        int rightPad = width - visibleLen - leftPad;
        return " ".repeat(Math.max(0, leftPad)) + text + " ".repeat(Math.max(0, rightPad));
    }
}
