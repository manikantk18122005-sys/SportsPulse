

package com.sportspulse.benchmark;

/**
 * Stores empirical performance measurements and correctness status for an algorithm benchmark run.
 */
public class BenchmarkResult {
    private final String algorithmName;
    private final String category;
    private final int inputSize;
    private final int edgeCount;
    private final double averageTimeNs;
    private final double minTimeNs;
    private final double maxTimeNs;
    private final int repetitions;
    private final boolean verifiedCorrect;
    private final String outputSignature;

    public BenchmarkResult(String algorithmName, String category, int inputSize, int edgeCount,
                           double averageTimeNs, double minTimeNs, double maxTimeNs,
                           int repetitions, boolean verifiedCorrect, String outputSignature) {
        this.algorithmName = algorithmName;
        this.category = category;
        this.inputSize = inputSize;
        this.edgeCount = edgeCount;
        this.averageTimeNs = averageTimeNs;
        this.minTimeNs = minTimeNs;
        this.maxTimeNs = maxTimeNs;
        this.repetitions = repetitions;
        this.verifiedCorrect = verifiedCorrect;
        this.outputSignature = outputSignature;
    }

    public String getAlgorithmName() { return algorithmName; }
    public String getCategory() { return category; }
    public int getInputSize() { return inputSize; }
    public int getEdgeCount() { return edgeCount; }
    public double getAverageTimeNs() { return averageTimeNs; }
    public double getMinTimeNs() { return minTimeNs; }
    public double getMaxTimeNs() { return maxTimeNs; }
    public int getRepetitions() { return repetitions; }
    public boolean isVerifiedCorrect() { return verifiedCorrect; }
    public String getOutputSignature() { return outputSignature; }

    public static String formatNs(double ns) {
        if (ns < 1000.0) {
            return String.format("%.1f ns", ns);
        } else if (ns < 1_000_000.0) {
            return String.format("%,.1f us (%,.0f ns)", ns / 1000.0, ns);
        } else {
            return String.format("%,.2f ms (%,.0f ns)", ns / 1_000_000.0, ns);
        }
    }

    public static String formatShortNs(double ns) {
        if (ns < 1000.0) {
            return String.format("%.0f ns", ns);
        } else if (ns < 1_000_000.0) {
            return String.format("%.1f us", ns / 1000.0);
        } else {
            return String.format("%.2f ms", ns / 1_000_000.0);
        }
    }

    public String toCsvRow() {
        return String.format("%s,%s,%d,%d,%.1f,%.1f,%.1f,%d,%b,\"%s\"",
                algorithmName, category, inputSize, edgeCount,
                averageTimeNs, minTimeNs, maxTimeNs, repetitions,
                verifiedCorrect, outputSignature.replace("\"", "'"));
    }

    @Override
    public String toString() {
        return String.format("%s [n=%d, edges=%d]: avg=%s, min=%s, reps=%d, correct=%b",
                algorithmName, inputSize, edgeCount, formatNs(averageTimeNs), formatNs(minTimeNs), repetitions, verifiedCorrect);
    }
}
