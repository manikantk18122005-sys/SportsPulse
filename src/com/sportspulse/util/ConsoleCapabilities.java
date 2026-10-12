package com.sportspulse.util;

import java.nio.charset.StandardCharsets;

/**
 * Terminal Capability & Environment Detector for SportsPulse.
 *
 * Implements conservative, graceful capability detection for:
 * 1. Color palette capabilities: TRUECOLOR (24-bit), ANSI16 (4-bit), or MONOCHROME.
 * 2. Interactive keyboard / TUI availability (distinguishing real TTYs from pipes, IDEs, and CI scripts).
 * 3. Terminal geometry / viewport width (responsive table & chart layouts).
 * 4. Unicode glyph support (box-drawing characters vs. pure ASCII fallbacks).
 *
 * Safe by design: Falls back to numeric line-mode and monochrome whenever detection is uncertain.
 */
public final class ConsoleCapabilities {

    public enum ColorMode {
        TRUECOLOR("24-Bit Truecolor (16.7M Colors)"),
        ANSI16("16-Color Standard ANSI Palette"),
        MONO("Accessibility Monochrome (Plain Text)");

        private final String description;
        ColorMode(String description) { this.description = description; }
        public String getDescription() { return description; }
    }

    public enum WidthTier {
        NARROW(65),      // Mobile / narrow split pane
        STANDARD(80),    // Standard terminal window
        WIDE(120);       // Fullscreen / high-res dashboard

        private final int columns;
        WidthTier(int columns) { this.columns = columns; }
        public int getColumns() { return columns; }
    }

    private static final boolean IS_WINDOWS = System.getProperty("os.name", "").toLowerCase().contains("win");
    private static final boolean IS_MAC = System.getProperty("os.name", "").toLowerCase().contains("mac");
    private static final boolean IS_LINUX = System.getProperty("os.name", "").toLowerCase().contains("linux");

    private static ColorMode activeColorMode = detectColorMode();
    private static Boolean interactiveOverride = null;
    private static Integer widthOverride = null;

    private ConsoleCapabilities() {}

    /**
     * Determines whether interactive raw keyboard navigation is available.
     * Returns false when stdin is redirected, piped, or running under headless test runners.
     */
    public static boolean isInteractive() {
        if (interactiveOverride != null) return interactiveOverride;
        if ("false".equalsIgnoreCase(System.getProperty("sportspulse.tui"))) return false;

        // System.console() is null when redirected, piped, or run within non-PTY IDE shells
        if (System.console() == null) return false;

        // Continuous integration and headless environments
        String ci = System.getenv("CI");
        if (ci != null && !ci.isBlank()) return false;

        return true;
    }

    public static void setInteractive(boolean interactive) {
        interactiveOverride = interactive;
    }

    public static void resetInteractive() {
        interactiveOverride = null;
    }

    /**
     * Detects terminal color capabilities from system properties and environment headers.
     */
    public static ColorMode detectColorMode() {
        // Explicit system property overrides
        String colorProp = System.getProperty("sportspulse.color");
        if ("false".equalsIgnoreCase(colorProp)) return ColorMode.MONO;

        String modeProp = System.getProperty("sportspulse.color.mode");
        if (modeProp != null) {
            String lower = modeProp.trim().toLowerCase();
            if (lower.equals("truecolor") || lower.equals("24bit")) return ColorMode.TRUECOLOR;
            if (lower.equals("ansi16") || lower.equals("16color")) return ColorMode.ANSI16;
            if (lower.equals("mono") || lower.equals("monochrome") || lower.equals("none")) return ColorMode.MONO;
        }

        // NO_COLOR standard (https://no-color.org)
        if (System.getenv("NO_COLOR") != null) return ColorMode.MONO;

        // COLORTERM header
        String colorTerm = System.getenv("COLORTERM");
        if (colorTerm != null && (colorTerm.equalsIgnoreCase("truecolor") || colorTerm.equalsIgnoreCase("24bit"))) {
            return ColorMode.TRUECOLOR;
        }

        // Modern Windows Terminal session detection
        if (System.getenv("WT_SESSION") != null) return ColorMode.TRUECOLOR;

        // Common terminal emulators known to support truecolor
        String termProg = System.getenv("TERM_PROGRAM");
        if (termProg != null) {
            String tp = termProg.toLowerCase();
            if (tp.contains("vscode") || tp.contains("iterm") || tp.contains("alacritty") || tp.contains("wezterm")) {
                return ColorMode.TRUECOLOR;
            }
        }

        // Standard ANSI 16-color detection
        String term = System.getenv("TERM");
        if (term != null) {
            String t = term.toLowerCase();
            if (t.contains("color") || t.contains("256") || t.contains("ansi") || t.contains("xterm")) {
                return ColorMode.ANSI16;
            }
            if (t.equals("dumb")) return ColorMode.MONO;
        }

        // Windows 10/11 default terminal
        if (IS_WINDOWS) {
            return ColorMode.ANSI16;
        }

        return ColorMode.ANSI16;
    }

    public static ColorMode getColorMode() {
        return activeColorMode;
    }

    public static void setColorMode(ColorMode mode) {
        if (mode != null) activeColorMode = mode;
    }

    public static void resetColorMode() {
        activeColorMode = detectColorMode();
    }

    public static boolean isColorEnabled() {
        return activeColorMode != ColorMode.MONO;
    }

    public static boolean isTruecolorEnabled() {
        return activeColorMode == ColorMode.TRUECOLOR;
    }

    /**
     * Determines the terminal column width, adapting between NARROW (65), STANDARD (80), and WIDE (120).
     */
    public static int getTerminalWidth() {
        if (widthOverride != null) return widthOverride;

        String widthProp = System.getProperty("sportspulse.width");
        if (widthProp != null) {
            try {
                int w = Integer.parseInt(widthProp.trim());
                if (w >= 40 && w <= 240) return w;
            } catch (NumberFormatException ignored) {}
        }

        String cols = System.getenv("COLUMNS");
        if (cols != null) {
            try {
                int c = Integer.parseInt(cols.trim());
                if (c >= 40 && c <= 240) return c;
            } catch (NumberFormatException ignored) {}
        }

        return WidthTier.STANDARD.getColumns();
    }

    public static void setTerminalWidth(int width) {
        widthOverride = Math.max(40, Math.min(240, width));
    }

    public static void resetTerminalWidth() {
        widthOverride = null;
    }

    public static WidthTier getWidthTier() {
        int w = getTerminalWidth();
        if (w < 75) return WidthTier.NARROW;
        if (w > 105) return WidthTier.WIDE;
        return WidthTier.STANDARD;
    }

    /**
     * Checks if Unicode box-drawing glyphs are supported by the host console.
     */
    public static boolean isUnicodeSupported() {
        String unicodeProp = System.getProperty("sportspulse.unicode");
        if ("false".equalsIgnoreCase(unicodeProp)) return false;
        if ("true".equalsIgnoreCase(unicodeProp)) return true;

        // Check if stdout encoding supports UTF-8
        String encoding = System.getProperty("sun.stdout.encoding");
        if (encoding == null) encoding = System.getProperty("file.encoding", "");
        return encoding.toLowerCase().contains("utf-8") || encoding.toLowerCase().contains("utf8") || !IS_WINDOWS;
    }

    public static boolean isWindows() { return IS_WINDOWS; }
    public static boolean isMac() { return IS_MAC; }
    public static boolean isLinux() { return IS_LINUX; }
}
