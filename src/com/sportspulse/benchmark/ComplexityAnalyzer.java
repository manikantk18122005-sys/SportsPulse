package com.sportspulse.benchmark;

import com.sportspulse.util.Ansi;
import com.sportspulse.util.Console;

import java.util.*;

/**
 * Renders comparison tables, ASCII console performance curves,
 * and academic complexity interpretation reports.
 */
public final class ComplexityAnalyzer {

    private ComplexityAnalyzer() {}

    // =========================================================================
    // ASCII GRAPH RENDERING
    // =========================================================================

    /**
     * Renders a multi-curve ASCII graph comparing algorithmic runtime growth.
     *
     * @param title      Graph header title.
     * @param seriesMap  Map of algorithm name to list of (inputSize, averageTimeNs) points.
     * @param xLabels    Ordered list of input sizes to plot on the x-axis.
     */
    public static void renderAsciiGraph(String title, Map<String, List<Double>> seriesMap, List<Integer> xLabels) {
        Console.section("ASCII RUNTIME GROWTH CURVE: " + title);

        char[] symbols = {'*', '+', '#', 'o', '^'};
        String[] colors = {Ansi.BRIGHT_RED, Ansi.BRIGHT_GREEN, Ansi.BRIGHT_CYAN, Ansi.BRIGHT_YELLOW, Ansi.BRIGHT_MAGENTA};

        // Determine max runtime across all series
        double maxTime = 0.0;
        for (List<Double> times : seriesMap.values()) {
            for (Double t : times) {
                if (t != null && t > maxTime) maxTime = t;
            }
        }
        if (maxTime <= 0.0) maxTime = 1.0;

        int plotHeight = 12; // Grid lines
        int colWidth = Math.max(8, 64 / Math.max(1, xLabels.size()));
        int numCols = xLabels.size();

        // Print Legend
        System.out.println("  LEGEND:");
        int symIdx = 0;
        for (String name : seriesMap.keySet()) {
            char sym = symbols[symIdx % symbols.length];
            String color = colors[symIdx % colors.length];
            System.out.printf("    %s %s: %s%n", Ansi.paint(color + Ansi.BOLD, String.valueOf(sym)), name, sym);
            symIdx++;
        }
        System.out.println();

        // Build 2D character grid
        char[][] grid = new char[plotHeight][numCols];
        int[][] colorGrid = new int[plotHeight][numCols];
        for (int r = 0; r < plotHeight; r++) {
            Arrays.fill(grid[r], ' ');
            Arrays.fill(colorGrid[r], -1);
        }

        // Plot points
        int sIdx = 0;
        for (Map.Entry<String, List<Double>> entry : seriesMap.entrySet()) {
            List<Double> times = entry.getValue();
            for (int col = 0; col < Math.min(numCols, times.size()); col++) {
                Double t = times.get(col);
                if (t != null) {
                    int row = (int) Math.round((t / maxTime) * (plotHeight - 1));
                    row = Math.max(0, Math.min(plotHeight - 1, row));
                    int gridRow = (plotHeight - 1) - row; // Invert for display (row 0 is top)
                    grid[gridRow][col] = symbols[sIdx % symbols.length];
                    colorGrid[gridRow][col] = sIdx % colors.length;
                }
            }
            sIdx++;
        }

        // Render Graph Canvas
        System.out.printf("  %s%n", Ansi.paint(Ansi.DIM, "Runtime (ns)"));
        for (int r = 0; r < plotHeight; r++) {
            double currentLevel = maxTime * (plotHeight - 1 - r) / (plotHeight - 1);
            String yAxisLabel = String.format("%8s |", BenchmarkResult.formatShortNs(currentLevel));
            System.out.print(Ansi.paint(Ansi.DIM, yAxisLabel));

            for (int c = 0; c < numCols; c++) {
                char ch = grid[r][c];
                int cIdx = colorGrid[r][c];
                int leftPad = colWidth / 2;
                int rightPad = colWidth - leftPad - 1;

                System.out.print(" ".repeat(Math.max(0, leftPad)));
                if (ch != ' ' && cIdx >= 0) {
                    System.out.print(Ansi.paint(colors[cIdx] + Ansi.BOLD, String.valueOf(ch)));
                } else {
                    System.out.print(Ansi.paint(Ansi.DIM, "."));
                }
                System.out.print(" ".repeat(Math.max(0, rightPad)));
            }
            System.out.println();
        }

        // Render X-Axis
        String xLine = "         +" + "-".repeat(numCols * colWidth + 2);
        System.out.println(Ansi.paint(Ansi.DIM, xLine));

        // Render X-Axis Ticks
        System.out.print("          ");
        for (int c = 0; c < numCols; c++) {
            String label = formatSize(xLabels.get(c));
            int pad = Math.max(0, colWidth - label.length());
            int left = pad / 2;
            int right = pad - left;
            System.out.print(" ".repeat(left) + Ansi.paint(Ansi.BRIGHT_WHITE, label) + " ".repeat(right));
        }
        System.out.println();
        System.out.println(Ansi.paint(Ansi.DIM, "          " + " ".repeat(Math.max(0, (numCols * colWidth) / 2 - 5)) + "Input Size (n)"));
        System.out.println();
    }

    private static String formatSize(int n) {
        if (n >= 1000) return (n / 1000) + "K";
        return String.valueOf(n);
    }

    // =========================================================================
    // ACADEMIC INTERPRETATION & CAVEATS
    // =========================================================================

    public static void printReport(String experimentName, String theoreticalComplexity,
                                   String observedTrend, String interpretation) {
        Console.section("EMPIRICAL COMPLEXITY ANALYSIS REPORT");
        Console.kv("Experiment", experimentName);
        Console.kv("Theoretical Bound", theoreticalComplexity);
        Console.kv("Observed Growth", observedTrend);
        System.out.println();
        System.out.println(Ansi.paint(Ansi.BRIGHT_YELLOW + Ansi.BOLD, "  Academic Interpretation:"));
        System.out.println("    " + interpretation);
        System.out.println();
        System.out.println(Ansi.paint(Ansi.DIM, "  ------------------------------------------------------------------------"));
        System.out.println(Ansi.paint(Ansi.BRIGHT_RED + Ansi.BOLD, "  VIVA CRITICAL NOTICE -- LIMITATIONS OF EMPIRICAL BENCHMARKING:"));
        System.out.println(Ansi.paint(Ansi.DIM, "  1. Empirical wall-clock/nanosecond measurements DO NOT mathematically prove Big-O complexity."));
        System.out.println(Ansi.paint(Ansi.DIM, "  2. Runtimes in a managed runtime (JVM) are subject to JIT compilation (C1/C2 tiers),"));
        System.out.println(Ansi.paint(Ansi.DIM, "     garbage collection pauses, thread scheduling by the OS, and CPU cache hierarchy effects."));
        System.out.println(Ansi.paint(Ansi.DIM, "  3. To ensure scientific rigor, we:"));
        System.out.println(Ansi.paint(Ansi.DIM, "     - Conducted JVM warm-up iterations prior to measurement collection."));
        System.out.println(Ansi.paint(Ansi.DIM, "     - Repeated executions over multiple runs and averaged nanosecond timestamps."));
        System.out.println(Ansi.paint(Ansi.DIM, "     - Isolated timing strictly to the algorithmic execution window."));
        System.out.println(Ansi.paint(Ansi.DIM, "  ------------------------------------------------------------------------"));
    }
}
