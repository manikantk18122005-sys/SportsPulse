package com.sportspulse.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Responsive, width-aware terminal table renderer for SportsPulse.
 *
 * Capabilities:
 * - Automatically adapts to terminal width (NARROW 65, STANDARD 80, WIDE 120).
 * - Intelligently truncates overlong columns with ellipsis ("...") to prevent awkward terminal wrap.
 * - Accurate column measurement using Ansi.visibleLength() to prevent ANSI color codes from distorting layout.
 * - Supports Unicode box-drawing characters (┌─┬─┐, │, ├─┼─┤, └─┴─┘) with seamless ASCII (+--+) fallback.
 * - Configurable column alignments (LEFT, RIGHT, CENTER).
 */
public final class TerminalTable {

    public enum Alignment {
        LEFT,
        RIGHT,
        CENTER
    }

    private final List<String> headers;
    private final List<List<String>> rows = new ArrayList<>();
    private final List<Alignment> alignments = new ArrayList<>();
    private boolean useBoxBorders = true;

    public TerminalTable(List<String> headers) {
        this.headers = new ArrayList<>(headers);
        for (int i = 0; i < headers.size(); i++) {
            alignments.add(Alignment.LEFT);
        }
    }

    public static TerminalTable of(List<String> headers, List<List<String>> rows) {
        TerminalTable table = new TerminalTable(headers);
        if (rows != null) {
            for (List<String> row : rows) {
                table.addRow(row);
            }
        }
        return table;
    }

    public TerminalTable addRow(List<String> row) {
        if (row != null) {
            this.rows.add(new ArrayList<>(row));
        }
        return this;
    }

    public TerminalTable setAlignment(int colIndex, Alignment alignment) {
        if (colIndex >= 0 && colIndex < alignments.size()) {
            alignments.set(colIndex, alignment);
        }
        return this;
    }

    public TerminalTable setUseBoxBorders(boolean boxBorders) {
        this.useBoxBorders = boxBorders;
        return this;
    }

    public void print() {
        System.out.println(render());
    }

    public String render() {
        if (headers.isEmpty()) return "";

        int numCols = headers.size();
        int terminalWidth = ConsoleCapabilities.getTerminalWidth();
        boolean unicode = ConsoleCapabilities.isUnicodeSupported();

        // Step 1: Calculate natural visible widths
        int[] widths = new int[numCols];
        for (int i = 0; i < numCols; i++) {
            widths[i] = Ansi.visibleLength(headers.get(i));
        }
        for (List<String> row : rows) {
            for (int i = 0; i < Math.min(numCols, row.size()); i++) {
                widths[i] = Math.max(widths[i], Ansi.visibleLength(row.get(i)));
            }
        }

        // Add padding (1 space left, 1 space right = 2 per column)
        // Overhead with borders: (numCols + 1) border chars + (numCols * 2) spaces
        int overhead = (numCols + 1) + (numCols * 2);
        int totalWidth = overhead;
        for (int w : widths) totalWidth += w;

        // Step 2: If table exceeds terminal width, compress widest column
        if (totalWidth > terminalWidth) {
            int excess = totalWidth - terminalWidth;
            // Find widest column index
            int widestCol = 0;
            for (int i = 1; i < numCols; i++) {
                if (widths[i] > widths[widestCol]) widestCol = i;
            }
            int minColWidth = 10;
            int reducible = widths[widestCol] - minColWidth;
            if (reducible > 0) {
                int reduction = Math.min(excess, reducible);
                widths[widestCol] -= reduction;
            }
        }

        // Step 3: Define Border Characters
        char topLeft  = unicode ? '┌' : '+';
        char topMid   = unicode ? '┬' : '+';
        char topRight = unicode ? '┐' : '+';
        char midLeft  = unicode ? '├' : '+';
        char midCross = unicode ? '┼' : '+';
        char midRight = unicode ? '┤' : '+';
        char botLeft  = unicode ? '└' : '+';
        char botMid   = unicode ? '┴' : '+';
        char botRight = unicode ? '┘' : '+';
        char horiz    = unicode ? '─' : '-';
        char vert     = unicode ? '│' : '|';

        StringBuilder sb = new StringBuilder();

        // Top Border
        if (useBoxBorders) {
            sb.append(TerminalTheme.border()).append(topLeft);
            for (int i = 0; i < numCols; i++) {
                sb.append(String.valueOf(horiz).repeat(widths[i] + 2));
                sb.append(i == numCols - 1 ? topRight : topMid);
            }
            sb.append(Ansi.RESET).append("\n");
        }

        // Header Row
        sb.append(TerminalTheme.border()).append(vert).append(Ansi.RESET);
        for (int i = 0; i < numCols; i++) {
            String hText = truncateOrPad(headers.get(i), widths[i], alignments.get(i));
            sb.append(" ").append(Ansi.paint(Ansi.BOLD + TerminalTheme.primary(), hText)).append(" ");
            sb.append(TerminalTheme.border()).append(vert).append(Ansi.RESET);
        }
        sb.append("\n");

        // Middle Divider
        sb.append(TerminalTheme.border()).append(midLeft);
        for (int i = 0; i < numCols; i++) {
            sb.append(String.valueOf(horiz).repeat(widths[i] + 2));
            sb.append(i == numCols - 1 ? midRight : midCross);
        }
        sb.append(Ansi.RESET).append("\n");

        // Data Rows
        for (int r = 0; r < rows.size(); r++) {
            List<String> row = rows.get(r);
            sb.append(TerminalTheme.border()).append(vert).append(Ansi.RESET);
            for (int i = 0; i < numCols; i++) {
                String cell = (i < row.size() && row.get(i) != null) ? row.get(i) : "";
                String formatted = truncateOrPad(cell, widths[i], alignments.get(i));
                sb.append(" ").append(formatted).append(" ");
                sb.append(TerminalTheme.border()).append(vert).append(Ansi.RESET);
            }
            sb.append("\n");
        }

        // Bottom Border
        if (useBoxBorders) {
            sb.append(TerminalTheme.border()).append(botLeft);
            for (int i = 0; i < numCols; i++) {
                sb.append(String.valueOf(horiz).repeat(widths[i] + 2));
                sb.append(i == numCols - 1 ? botRight : botMid);
            }
            sb.append(Ansi.RESET);
        }

        return sb.toString();
    }

    private static String truncateOrPad(String text, int targetWidth, Alignment alignment) {
        int visibleLen = Ansi.visibleLength(text);
        if (visibleLen > targetWidth) {
            // Truncate with ellipsis
            if (targetWidth <= 3) {
                return text.substring(0, Math.min(text.length(), targetWidth));
            }
            // Strip formatting to safely truncate
            String clean = Ansi.strip(text);
            return clean.substring(0, targetWidth - 3) + "...";
        }

        int paddingNeeded = targetWidth - visibleLen;
        if (paddingNeeded <= 0) return text;

        switch (alignment) {
            case RIGHT:
                return " ".repeat(paddingNeeded) + text;
            case CENTER:
                int left = paddingNeeded / 2;
                int right = paddingNeeded - left;
                return " ".repeat(left) + text + " ".repeat(right);
            case LEFT:
            default:
                return text + " ".repeat(paddingNeeded);
        }
    }
}
