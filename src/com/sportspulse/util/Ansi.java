package com.sportspulse.util;

import java.util.regex.Pattern;

/**
 * High-Fidelity ANSI & TrueColor Presentation Engine for SportsPulse.
 *
 * Implements:
 * 1. 24-bit TrueColor (16.7M colors) via ESC[38;2;r;g;bm and ESC[48;2;r;g;bm.
 * 2. Automatic Euclidean RGB fallback to 4-bit ANSI-16 when TrueColor is unavailable.
 * 3. Graceful fallback to monochrome when coloring is disabled or NO_COLOR is set.
 * 4. Cursor position, line clearing, screen clearing, and cursor visibility controls.
 * 5. Regex-based ANSI stripping and true visible-length calculations for precise table layouts.
 *
 * Fully backward-compatible with all existing SportsPulse ANSI constants and callers.
 */
public final class Ansi {

    private Ansi() {}

    public static final boolean ENABLED = detectEnabled();

    // Regex pattern matching standard ANSI and VT escape sequences
    private static final Pattern ANSI_PATTERN = Pattern.compile("\u001B\\[[;?0-9]*[a-zA-Z]");

    // Text Attributes
    public static final String RESET      = ENABLED ? "\u001B[0m" : "";
    public static final String BOLD       = ENABLED ? "\u001B[1m" : "";
    public static final String DIM        = ENABLED ? "\u001B[2m" : "";
    public static final String ITALIC     = ENABLED ? "\u001B[3m" : "";
    public static final String UNDERLINE  = ENABLED ? "\u001B[4m" : "";
    public static final String BLINK      = ENABLED ? "\u001B[5m" : "";
    public static final String INVERT     = ENABLED ? "\u001B[7m" : "";

    // Standard 4-bit Foreground Colors
    public static final String RED     = ENABLED ? "\u001B[31m" : "";
    public static final String GREEN   = ENABLED ? "\u001B[32m" : "";
    public static final String YELLOW  = ENABLED ? "\u001B[33m" : "";
    public static final String BLUE    = ENABLED ? "\u001B[34m" : "";
    public static final String MAGENTA = ENABLED ? "\u001B[35m" : "";
    public static final String CYAN    = ENABLED ? "\u001B[36m" : "";
    public static final String WHITE   = ENABLED ? "\u001B[37m" : "";

    // High-Intensity Foreground Colors
    public static final String BRIGHT_BLACK   = ENABLED ? "\u001B[90m" : "";
    public static final String BRIGHT_RED     = ENABLED ? "\u001B[91m" : "";
    public static final String BRIGHT_GREEN   = ENABLED ? "\u001B[92m" : "";
    public static final String BRIGHT_YELLOW  = ENABLED ? "\u001B[93m" : "";
    public static final String BRIGHT_BLUE    = ENABLED ? "\u001B[94m" : "";
    public static final String BRIGHT_MAGENTA = ENABLED ? "\u001B[95m" : "";
    public static final String BRIGHT_CYAN    = ENABLED ? "\u001B[96m" : "";
    public static final String BRIGHT_WHITE   = ENABLED ? "\u001B[97m" : "";

    // Standard 4-bit Background Colors
    public static final String BG_BLACK   = ENABLED ? "\u001B[40m" : "";
    public static final String BG_RED     = ENABLED ? "\u001B[41m" : "";
    public static final String BG_GREEN   = ENABLED ? "\u001B[42m" : "";
    public static final String BG_YELLOW  = ENABLED ? "\u001B[43m" : "";
    public static final String BG_BLUE    = ENABLED ? "\u001B[44m" : "";
    public static final String BG_MAGENTA = ENABLED ? "\u001B[45m" : "";
    public static final String BG_CYAN    = ENABLED ? "\u001B[46m" : "";
    public static final String BG_WHITE   = ENABLED ? "\u001B[47m" : "";

    // High-Intensity Background Colors
    public static final String BG_BRIGHT_BLACK = ENABLED ? "\u001B[100m" : "";
    public static final String BG_BRIGHT_BLUE  = ENABLED ? "\u001B[104m" : "";
    public static final String BG_BRIGHT_CYAN  = ENABLED ? "\u001B[106m" : "";

    // Cursor and Screen Management Sequences
    public static final String CURSOR_HIDE   = ENABLED ? "\u001B[?25l" : "";
    public static final String CURSOR_SHOW   = ENABLED ? "\u001B[?25h" : "";
    public static final String CURSOR_HOME   = ENABLED ? "\u001B[H" : "";
    public static final String CLEAR_SCREEN  = ENABLED ? "\u001B[2J" : "";
    public static final String CLEAR_LINE    = ENABLED ? "\u001B[2K" : "";
    public static final String CLEAR_TO_END  = ENABLED ? "\u001B[0J" : "";

    // Standard 16 ANSI Color Centroids in sRGB for Euclidean fallback
    private static final int[][] ANSI16_PALETTE = {
        {0, 0, 0},       // 0: Black (30 / 40)
        {170, 0, 0},     // 1: Red (31 / 41)
        {0, 170, 0},     // 2: Green (32 / 42)
        {170, 170, 0},   // 3: Yellow (33 / 43)
        {0, 0, 170},     // 4: Blue (34 / 44)
        {170, 0, 170},   // 5: Magenta (35 / 45)
        {0, 170, 170},   // 6: Cyan (36 / 46)
        {170, 170, 170}, // 7: White (37 / 47)
        {85, 85, 85},    // 8: Bright Black / Grey (90 / 100)
        {255, 85, 85},   // 9: Bright Red (91 / 101)
        {85, 255, 85},   // 10: Bright Green (92 / 102)
        {255, 255, 85},  // 11: Bright Yellow (93 / 103)
        {85, 85, 255},   // 12: Bright Blue (94 / 104)
        {255, 85, 255},  // 13: Bright Magenta (95 / 105)
        {85, 255, 255},  // 14: Bright Cyan (96 / 106)
        {255, 255, 255}  // 15: Bright White (97 / 107)
    };

    /**
     * Generates a 24-bit TrueColor foreground escape code, or the closest ANSI-16 match.
     * Returns empty string if coloring is disabled.
     */
    public static String rgb(int r, int g, int b) {
        if (!ConsoleCapabilities.isColorEnabled() || !ENABLED) return "";
        r = clamp(r); g = clamp(g); b = clamp(b);
        if (ConsoleCapabilities.isTruecolorEnabled()) {
            return "\u001B[38;2;" + r + ";" + g + ";" + b + "m";
        }
        return mapRgbToAnsi16(r, g, b, false);
    }

    /**
     * Generates a 24-bit TrueColor background escape code, or the closest ANSI-16 match.
     * Returns empty string if coloring is disabled.
     */
    public static String bgRgb(int r, int g, int b) {
        if (!ConsoleCapabilities.isColorEnabled() || !ENABLED) return "";
        r = clamp(r); g = clamp(g); b = clamp(b);
        if (ConsoleCapabilities.isTruecolorEnabled()) {
            return "\u001B[48;2;" + r + ";" + g + ";" + b + "m";
        }
        return mapRgbToAnsi16(r, g, b, true);
    }

    /**
     * Maps an RGB triplet to the closest matching 4-bit ANSI color using Euclidean distance.
     */
    public static String mapRgbToAnsi16(int r, int g, int b, boolean isBackground) {
        int bestIndex = 0;
        int minDistance = Integer.MAX_VALUE;

        for (int i = 0; i < ANSI16_PALETTE.length; i++) {
            int dr = r - ANSI16_PALETTE[i][0];
            int dg = g - ANSI16_PALETTE[i][1];
            int db = b - ANSI16_PALETTE[i][2];
            int dist = dr * dr + dg * dg + db * db;
            if (dist < minDistance) {
                minDistance = dist;
                bestIndex = i;
            }
        }

        int code;
        if (!isBackground) {
            code = (bestIndex < 8) ? (30 + bestIndex) : (90 + (bestIndex - 8));
        } else {
            code = (bestIndex < 8) ? (40 + bestIndex) : (100 + (bestIndex - 8));
        }
        return "\u001B[" + code + "m";
    }

    public static String cursorUp(int n) {
        return (ENABLED && n > 0) ? "\u001B[" + n + "A" : "";
    }

    public static String cursorDown(int n) {
        return (ENABLED && n > 0) ? "\u001B[" + n + "B" : "";
    }

    public static String cursorForward(int n) {
        return (ENABLED && n > 0) ? "\u001B[" + n + "C" : "";
    }

    public static String cursorBack(int n) {
        return (ENABLED && n > 0) ? "\u001B[" + n + "D" : "";
    }

    public static String cursorTo(int row, int col) {
        return ENABLED ? "\u001B[" + Math.max(1, row) + ";" + Math.max(1, col) + "H" : "";
    }

    /** Wraps text in a color/style code and resets afterward. No-op if disabled. */
    public static String paint(String code, Object text) {
        if (!ENABLED || code == null || code.isEmpty()) return String.valueOf(text);
        return code + text + RESET;
    }

    /** Wraps text in a 24-bit TrueColor foreground and resets afterward. */
    public static String rgbPaint(int r, int g, int b, Object text) {
        String code = rgb(r, g, b);
        if (code.isEmpty()) return String.valueOf(text);
        return code + text + RESET;
    }

    /** Wraps text in a 24-bit TrueColor background and resets afterward. */
    public static String bgRgbPaint(int r, int g, int b, Object text) {
        String code = bgRgb(r, g, b);
        if (code.isEmpty()) return String.valueOf(text);
        return code + text + RESET;
    }

    /**
     * Strips all ANSI escape sequences from a string to measure exact printable column width.
     */
    public static String strip(String text) {
        if (text == null) return "";
        return ANSI_PATTERN.matcher(text).replaceAll("");
    }

    /**
     * Returns the visible character count of a string, ignoring invisible ANSI formatting.
     */
    public static int visibleLength(String text) {
        return strip(text).length();
    }

    private static int clamp(int val) {
        return Math.max(0, Math.min(255, val));
    }

    private static boolean detectEnabled() {
        if ("false".equalsIgnoreCase(System.getProperty("sportspulse.color"))) return false;
        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            enableWindowsVirtualTerminal();
        }
        return true;
    }

    private static void enableWindowsVirtualTerminal() {
        try {
            new ProcessBuilder("cmd", "/c", "color").inheritIO().start().waitFor();
        } catch (Exception ignored) {
            // Fall through silently
        }
    }
}
