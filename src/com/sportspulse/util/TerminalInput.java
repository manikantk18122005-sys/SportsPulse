package com.sportspulse.util;

import java.util.Scanner;

/**
 * Robust keyboard input parsing for both interactive ANSI navigation and automated line-mode fallback.
 *
 * Supports:
 * - Arrow Keys: ↑ (\u001B[A) and ↓ (\u001B[B)
 * - Vim/Gamer Keys: 'k' / 'w' (Up) and 'j' / 's' (Down)
 * - Enter Key: Selects currently highlighted item
 * - Direct Digits: '0'-'9', '10', etc. immediately jumps to or selects menu item
 * - Back/Exit: 'q', 'Q', 'exit', 'back'
 * - Non-interactive/Piped: Seamless line reading from automated scripts and tests without hanging.
 */
public final class TerminalInput {

    public enum KeyAction {
        UP,
        DOWN,
        ENTER,
        BACK,
        DIGIT,
        UNKNOWN,
        EOF
    }

    public static final class ParsedInput {
        private final KeyAction action;
        private final String digit;
        private final String raw;

        public ParsedInput(KeyAction action, String digit, String raw) {
            this.action = action;
            this.digit = digit;
            this.raw = raw;
        }

        public KeyAction getAction() { return action; }
        public String getDigit() { return digit; }
        public String getRaw() { return raw; }
        public boolean isUp() { return action == KeyAction.UP; }
        public boolean isDown() { return action == KeyAction.DOWN; }
        public boolean isEnter() { return action == KeyAction.ENTER; }
        public boolean isBack() { return action == KeyAction.BACK; }
        public boolean isDigit() { return action == KeyAction.DIGIT; }
        public boolean isEof() { return action == KeyAction.EOF; }
    }

    private TerminalInput() {}

    /**
     * Reads a line from the provided scanner and parses it into a semantic KeyAction.
     */
    public static ParsedInput readInput(Scanner scanner) {
        if (scanner == null || !scanner.hasNextLine()) {
            return new ParsedInput(KeyAction.EOF, null, null);
        }
        String line = scanner.nextLine();
        return parse(line);
    }

    /**
     * Parses a raw line or escape sequence into semantic key actions.
     */
    public static ParsedInput parse(String line) {
        if (line == null) {
            return new ParsedInput(KeyAction.EOF, null, null);
        }
        String raw = line;
        String trimmed = line.trim();

        if (trimmed.isEmpty()) {
            return new ParsedInput(KeyAction.ENTER, null, raw);
        }

        String lower = trimmed.toLowerCase();

        // Check for Arrow Up ANSI sequences or navigational shortcuts
        if (raw.contains("\u001B[A") || raw.contains("\u001b[A") || lower.endsWith("[a")
                || lower.equals("w") || lower.equals("k") || lower.equals("up")) {
            return new ParsedInput(KeyAction.UP, null, raw);
        }

        // Check for Arrow Down ANSI sequences or navigational shortcuts
        if (raw.contains("\u001B[B") || raw.contains("\u001b[B") || lower.endsWith("[b")
                || lower.equals("s") || lower.equals("j") || lower.equals("down")) {
            return new ParsedInput(KeyAction.DOWN, null, raw);
        }

        // Check for Cancel / Back / Exit
        if (lower.equals("q") || lower.equals("exit") || lower.equals("back") || lower.equals("\u001b")) {
            return new ParsedInput(KeyAction.BACK, null, raw);
        }

        // Check for direct numeric selection (e.g. "1", "2", "10", "0")
        if (trimmed.matches("^[0-9]+$")) {
            return new ParsedInput(KeyAction.DIGIT, trimmed, raw);
        }

        return new ParsedInput(KeyAction.UNKNOWN, null, raw);
    }
}
