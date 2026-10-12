package com.sportspulse.util;

/**
 * Centralized Semantic Color Theme & Accessibility Palette for SportsPulse.
 *
 * Implements a unified sports-themed palette:
 * - Stadium Cyan / Azure (Primary sports identity)
 * - Slate Blue (Secondary structural hierarchy)
 * - Vibrant Gold / Amber (Accents & Highlights)
 * - Turf Emerald Green (Success, Verified States, Active Users)
 * - Alert Crimson (Failures, Lockouts, Exceptions)
 * - Royal Purple (Admin Elevation & High Privilege)
 * - Slate Grey (Borders, Rulers, Dim Metadata)
 *
 * Accessibility First:
 * Every semantic state pairs color with a distinct textual/glyph badge ([✓ OK], [✗ FAIL], [! WARN], [ℹ INFO])
 * so colorblind users and plain-text terminals maintain complete clarity.
 */
public final class TerminalTheme {

    private TerminalTheme() {}

    // Semantic TrueColor Foreground Codes (Gracefully degraded via Ansi.rgb)
    public static String primary()    { return Ansi.rgb(0, 168, 232); }   // Stadium Electric Cyan
    public static String secondary()  { return Ansi.rgb(72, 149, 239); }  // Slate Blue
    public static String accent()     { return Ansi.rgb(255, 183, 3); }   // Golden Amber
    public static String success()    { return Ansi.rgb(46, 204, 113); }  // Turf Emerald Green
    public static String warning()    { return Ansi.rgb(243, 156, 18); }  // Warning Amber
    public static String error()      { return Ansi.rgb(231, 76, 60); }   // Crimson Red
    public static String info()       { return Ansi.rgb(52, 152, 219); }  // Sky Blue
    public static String admin()      { return Ansi.rgb(155, 89, 182); }  // Royal Purple
    public static String user()       { return Ansi.rgb(46, 204, 113); }  // Active Green
    public static String border()     { return Ansi.rgb(108, 117, 125); } // Slate Grey
    public static String muted()      { return Ansi.rgb(140, 150, 165); } // Subtle Text
    public static String metric()     { return Ansi.BOLD + Ansi.rgb(241, 250, 238); } // Crisp Metric

    // Semantic Background Colors
    public static String headerBg()    { return Ansi.bgRgb(24, 32, 54); }   // Deep Stadium Navy
    public static String highlightBg() { return Ansi.bgRgb(38, 70, 83); }   // Selected Item Background
    public static String alertBg()     { return Ansi.bgRgb(90, 24, 24); }   // Dark Crimson Alert

    // Accessibility Badges
    public static String successBadge(String message) {
        String tag = ConsoleCapabilities.isUnicodeSupported() ? "[✓ OK]" : "[OK]";
        return Ansi.paint(Ansi.BOLD + success(), tag) + " " + message;
    }

    public static String errorBadge(String message) {
        String tag = ConsoleCapabilities.isUnicodeSupported() ? "[✗ FAIL]" : "[FAIL]";
        return Ansi.paint(Ansi.BOLD + error(), tag) + " " + message;
    }

    public static String warnBadge(String message) {
        String tag = ConsoleCapabilities.isUnicodeSupported() ? "[! WARN]" : "[WARN]";
        return Ansi.paint(Ansi.BOLD + warning(), tag) + " " + message;
    }

    public static String infoBadge(String message) {
        String tag = ConsoleCapabilities.isUnicodeSupported() ? "[ℹ INFO]" : "[INFO]";
        return Ansi.paint(Ansi.BOLD + info(), tag) + " " + message;
    }

    public static String adminBadge() {
        return Ansi.paint(Ansi.BOLD + admin(), "[ADMIN]");
    }

    public static String userBadge() {
        return Ansi.paint(Ansi.BOLD + user(), "[USER]");
    }

    public static String securityBadge(String status) {
        String color = "ACTIVE".equalsIgnoreCase(status) ? success() :
                      ("LOCKED".equalsIgnoreCase(status) ? error() : warning());
        return Ansi.paint(Ansi.BOLD + color, "[" + status.toUpperCase() + "]");
    }
}
