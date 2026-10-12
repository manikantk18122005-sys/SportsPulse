package com.sportspulse.util;

import java.util.Arrays;
import java.util.List;

/**
 * High-density ASCII & TrueColor data visualizations for SportsPulse.
 *
 * Implements:
 * 1. Responsive Horizontal Bar Charts with proportional block glyphs (█, ▒, ░).
 * 2. 2D Coordinate Grid Charts with tick marks (┤, ┴, ┼), axis scales, and legends.
 * 3. Hash Table Collision & Bucket Distribution Grids.
 * 4. Empirical Complexity Comparison Cards.
 */
public final class TerminalChart {

    private TerminalChart() {}

    public static final class DataPoint {
        private final String label;
        private final double value;
        private final String unit;

        public DataPoint(String label, double value, String unit) {
            this.label = label;
            this.value = value;
            this.unit = unit;
        }

        public String getLabel() { return label; }
        public double getValue() { return value; }
        public String getUnit() { return unit; }
    }

    /**
     * Renders a responsive horizontal bar chart scaled to the available terminal width.
     */
    public static void renderBarChart(String title, List<DataPoint> data) {
        if (data == null || data.isEmpty()) return;

        int termWidth = ConsoleCapabilities.getTerminalWidth();
        boolean unicode = ConsoleCapabilities.isUnicodeSupported();
        char blockFull = unicode ? '█' : '#';
        char blockLight = unicode ? '░' : '.';

        TerminalRenderer.renderDivider(title, termWidth);

        double maxVal = 0.0;
        int maxLabelLen = 0;
        for (DataPoint dp : data) {
            maxVal = Math.max(maxVal, dp.getValue());
            maxLabelLen = Math.max(maxLabelLen, dp.getLabel().length());
        }
        if (maxVal <= 0.0) maxVal = 1.0;

        // Label width capped at 24 chars
        int labelWidth = Math.min(24, maxLabelLen);
        // Reserve 16 chars for values, percentage, and spacing
        int barSpace = Math.max(15, termWidth - labelWidth - 22);

        for (DataPoint dp : data) {
            String label = padRight(dp.getLabel(), labelWidth);
            double ratio = Math.max(0.0, Math.min(1.0, dp.getValue() / maxVal));
            int filled = (int) Math.round(ratio * barSpace);
            int unfilled = Math.max(0, barSpace - filled);

            String barStr = String.valueOf(blockFull).repeat(filled) + String.valueOf(blockLight).repeat(unfilled);

            // Colorize based on ratio: green (low/fast) -> yellow -> red
            String color = ratio < 0.4 ? TerminalTheme.success() : (ratio < 0.75 ? TerminalTheme.warning() : TerminalTheme.error());
            String valStr = String.format("%6.2f %s (%3.0f%%)", dp.getValue(), dp.getUnit(), ratio * 100.0);

            System.out.printf("  %s %s %s%n",
                    Ansi.paint(TerminalTheme.secondary(), label),
                    Ansi.paint(color, barStr),
                    Ansi.paint(TerminalTheme.metric(), valStr));
        }
        System.out.println();
    }

    /**
     * Renders a 2D coordinate plot with tick marks, axis scale, and legend.
     */
    public static void renderCoordinateChart(String title, double[] xVals, double[] yVals, String xLabel, String yLabel) {
        int height = 8;
        int width = Math.min(48, ConsoleCapabilities.getTerminalWidth() - 16);
        boolean unicode = ConsoleCapabilities.isUnicodeSupported();

        char vertAxis  = unicode ? '│' : '|';
        char horizAxis = unicode ? '─' : '-';
        char tickY     = unicode ? '┤' : '+';
        char tickX     = unicode ? '┴' : '+';
        char origin    = unicode ? '└' : '+';
        char pointDot  = unicode ? '●' : '*';

        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
        for (double y : yVals) {
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }
        if (minY == maxY) { maxY = minY + 1.0; }

        double minX = xVals[0], maxX = xVals[xVals.length - 1];
        if (minX == maxX) maxX = minX + 1.0;

        char[][] grid = new char[height][width];
        for (char[] row : grid) Arrays.fill(row, ' ');

        for (int i = 0; i < xVals.length; i++) {
            int col = (int) Math.round(((xVals[i] - minX) / (maxX - minX)) * (width - 1));
            int row = (int) Math.round(((yVals[i] - minY) / (maxY - minY)) * (height - 1));
            col = Math.max(0, Math.min(width - 1, col));
            row = Math.max(0, Math.min(height - 1, row));
            grid[height - 1 - row][col] = pointDot;
        }

        TerminalRenderer.renderDivider(title, ConsoleCapabilities.getTerminalWidth());

        // Print Grid with Y-axis
        for (int r = 0; r < height; r++) {
            String yValStr = (r == 0) ? String.format("%5.1f", maxY) :
                             (r == height - 1) ? String.format("%5.1f", minY) :
                             (r == height / 2) ? String.format("%5.1f", (minY + maxY) / 2.0) : "     ";
            char tick = (r == 0 || r == height - 1 || r == height / 2) ? tickY : vertAxis;
            System.out.printf("  %s %s", Ansi.paint(TerminalTheme.muted(), yValStr), Ansi.paint(TerminalTheme.border(), String.valueOf(tick)));

            StringBuilder line = new StringBuilder();
            for (int c = 0; c < width; c++) {
                char ch = grid[r][c];
                if (ch == pointDot) {
                    line.append(Ansi.paint(Ansi.BOLD + TerminalTheme.accent(), String.valueOf(ch)));
                } else {
                    line.append(' ');
                }
            }
            System.out.println(line);
        }

        // Print X-axis
        StringBuilder xAx = new StringBuilder("        ").append(origin);
        for (int c = 0; c < width; c++) {
            if (c == 0 || c == width / 2 || c == width - 1) {
                xAx.append(tickX);
            } else {
                xAx.append(horizAxis);
            }
        }
        System.out.println(Ansi.paint(TerminalTheme.border(), xAx.toString()));

        // Print X-axis scale labels
        String xMinStr = String.format("%.0f", minX);
        String xMidStr = String.format("%.0f", (minX + maxX) / 2.0);
        String xMaxStr = String.format("%.0f", maxX);
        int midPad = (width / 2) - xMinStr.length() - (xMidStr.length() / 2);
        int rightPad = width - (width / 2) - (xMidStr.length() / 2) - xMaxStr.length();
        System.out.printf("         %s%s%s%s%s%n",
                xMinStr,
                " ".repeat(Math.max(1, midPad)),
                xMidStr,
                " ".repeat(Math.max(1, rightPad)),
                xMaxStr);

        // Legend and empirical note
        String legend = String.format("  Legend: %s Empirical Points (%s) | %s / %s",
                Ansi.paint(Ansi.BOLD + TerminalTheme.accent(), String.valueOf(pointDot)),
                yLabel, xLabel, yLabel);
        System.out.println(Ansi.paint(TerminalTheme.muted(), legend));
        System.out.println();
    }

    /**
     * Renders a bucket distribution visualization for Universal Hash Tables.
     */
    public static void renderHashDistribution(int[] bucketDepths, int capacity) {
        boolean unicode = ConsoleCapabilities.isUnicodeSupported();
        char block = unicode ? '■' : '#';
        int cols = 5;

        TerminalRenderer.renderDivider("Hash Bucket Distribution Grid", ConsoleCapabilities.getTerminalWidth());

        for (int i = 0; i < capacity; i++) {
            int depth = (i < bucketDepths.length) ? bucketDepths[i] : 0;
            String depthSymbols = String.valueOf(block).repeat(Math.min(3, depth));
            String color = (depth == 0) ? TerminalTheme.muted() :
                           (depth == 1) ? TerminalTheme.success() :
                           (depth == 2) ? TerminalTheme.warning() : TerminalTheme.error();

            String cell = String.format("[%2d: %-3s (%d)]", i, depthSymbols, depth);
            System.out.print(" " + Ansi.paint(color, cell));
            if ((i + 1) % cols == 0 || i == capacity - 1) {
                System.out.println();
            }
        }
        System.out.println();
    }

    /**
     * Renders an empirical complexity card comparing theoretical O(N) vs measured runtime.
     */
    public static void renderComplexityCard(String algorithm, String theoretical, double empiricalMs, String status) {
        int width = Math.min(76, ConsoleCapabilities.getTerminalWidth());
        TerminalRenderer.renderDivider("Complexity Verification: " + algorithm, width);

        System.out.printf("  %-24s: %s%n",
                Ansi.paint(TerminalTheme.secondary(), "Theoretical Complexity"),
                Ansi.paint(Ansi.BOLD + TerminalTheme.primary(), theoretical));
        System.out.printf("  %-24s: %s%n",
                Ansi.paint(TerminalTheme.secondary(), "Empirical Runtime"),
                Ansi.paint(Ansi.BOLD + TerminalTheme.metric(), String.format("%.3f ms", empiricalMs)));
        System.out.printf("  %-24s: %s%n",
                Ansi.paint(TerminalTheme.secondary(), "Optimization Status"),
                TerminalTheme.successBadge(status));
        System.out.println();
    }

    private static String padRight(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s;
        return s + " ".repeat(width - s.length());
    }
}
