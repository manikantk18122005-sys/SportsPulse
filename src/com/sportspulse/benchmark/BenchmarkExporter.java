package com.sportspulse.benchmark;

import com.sportspulse.util.AppPaths;
import com.sportspulse.util.Console;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Exports in-memory benchmark metrics to clean, dependency-free CSV files.
 *
 * Fully portable: Resolves target path via AppPaths into runtime/benchmarks/
 * or user-specified path, ensuring parent directories are created on demand.
 */
public final class BenchmarkExporter {

    public static final String DEFAULT_CSV_PATH = "benchmark_results.csv";

    private BenchmarkExporter() {}

    /**
     * Exports all benchmark results recorded during this session to a CSV file.
     *
     * @param targetFilePath Target filesystem path or filename.
     * @return true if write succeeded, false otherwise.
     */
    public static boolean exportToCsv(String targetFilePath) {
        List<BenchmarkResult> results = BenchmarkRunner.getHistory();
        if (results.isEmpty()) {
            Console.warn("No benchmark results have been recorded yet. Run an experiment first.");
            return false;
        }

        Path destination;
        if (targetFilePath == null || targetFilePath.isBlank() || targetFilePath.equals(DEFAULT_CSV_PATH)) {
            destination = AppPaths.getBenchmarksDir().resolve(DEFAULT_CSV_PATH);
        } else {
            Path custom = Paths.get(targetFilePath.trim());
            destination = custom.isAbsolute() ? custom : AppPaths.getBenchmarksDir().resolve(custom.getFileName());
        }

        try {
            if (destination.getParent() != null) {
                Files.createDirectories(destination.getParent());
            }

            try (BufferedWriter writer = Files.newBufferedWriter(destination, StandardCharsets.UTF_8)) {
                // Write CSV header
                writer.write("algorithm,category,inputSize,edgeCount,averageTimeNs,minTimeNs,maxTimeNs,repetitions,verifiedCorrect,note");
                writer.newLine();

                // Write data rows
                for (BenchmarkResult r : results) {
                    writer.write(r.toCsvRow());
                    writer.newLine();
                }
            }

            // Also keep a copy at project root for convenience if default was chosen
            if (DEFAULT_CSV_PATH.equals(destination.getFileName().toString())) {
                try {
                    Path rootCopy = AppPaths.getProjectRoot().resolve(DEFAULT_CSV_PATH);
                    Files.copy(destination, rootCopy, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception ignored) {}
            }

            Console.success(String.format("Successfully exported %d benchmark records to: %s",
                    results.size(), destination.getFileName()));
            Console.info("Location: " + destination.toAbsolutePath().normalize());
            return true;
        } catch (IOException e) {
            Console.error("Failed to export benchmark results: " + e.getMessage());
            return false;
        }
    }
}
