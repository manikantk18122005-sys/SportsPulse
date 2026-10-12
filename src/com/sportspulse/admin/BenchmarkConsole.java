package com.sportspulse.admin;

import com.sportspulse.benchmark.BenchmarkExporter;
import com.sportspulse.benchmark.BenchmarkResult;
import com.sportspulse.benchmark.BenchmarkRunner;
import com.sportspulse.util.Ansi;
import com.sportspulse.util.Console;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive menu interface for the Empirical Complexity Lab.
 */
public final class BenchmarkConsole {

    private BenchmarkConsole() {}

    public static void run(Scanner sc) {
        boolean back = false;
        while (!back) {
            Console.header("SPORTSPULSE EMPIRICAL COMPLEXITY LAB");
            System.out.println(Ansi.paint(Ansi.DIM, "  Experimental Verification of Algorithm Runtimes as Input Size Scales"));
            Console.section("Complexity Lab Menu");
            Console.bullet("1. KMP vs Naive String Search  (O(n+m) vs O(n*m))");
            Console.bullet("2. Max Flow: Ford-Fulkerson vs Edmonds-Karp vs Dinic");
            Console.bullet("3. Additional Algorithm Benchmarks  (Sorting & Multi-Matcher)");
            Console.bullet("4. Run All Empirical Experiments");
            Console.bullet("5. View Previous Benchmark Results");
            Console.bullet("6. Export Benchmark Results to CSV");
            Console.bullet("0. Back to Admin Lab");
            Console.prompt("\nSelect an option: ");

            if (!sc.hasNextLine()) {
                back = true;
                break;
            }
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> runKmpMenu(sc);
                case "2" -> BenchmarkRunner.runMaxFlowComparison(List.of(10, 20, 40, 80, 160));
                case "3" -> BenchmarkRunner.runAdditionalBenchmarks();
                case "4" -> BenchmarkRunner.runAllExperiments();
                case "5" -> viewPreviousResults();
                case "6" -> exportResults(sc);
                case "0" -> back = true;
                default -> Console.error("Invalid option, try again.");
            }
            if (!back) Console.pause(sc);
        }
    }

    private static void runKmpMenu(Scanner sc) {
        Console.section("KMP vs Naive Search Configuration");
        Console.bullet("1. Worst-Case Repetitive Prefix (Forces Theta(n*m) backtracking in Naive)");
        Console.bullet("2. Natural Sports Commentary Corpus (Realistic streaming match text)");
        Console.prompt("Select scenario [1/2, default=1]: ");
        String choice = sc.hasNextLine() ? sc.nextLine().trim() : "1";
        boolean worstCase = !choice.equals("2");

        List<Integer> sizes = List.of(1000, 2000, 5000, 10000, 20000, 50000, 100000);
        BenchmarkRunner.runKmpVsNaive(sizes, worstCase);
    }

    private static void viewPreviousResults() {
        Console.section("Recorded Benchmark History");
        List<BenchmarkResult> results = BenchmarkRunner.getHistory();
        if (results.isEmpty()) {
            Console.warn("No benchmarks have been executed in this session yet.");
            return;
        }

        List<String> headers = List.of("Algorithm", "Category", "Input Size", "Edges", "Avg Runtime", "Min Runtime", "Status");
        List<List<String>> rows = new ArrayList<>();
        for (BenchmarkResult r : results) {
            rows.add(List.of(
                    r.getAlgorithmName(),
                    r.getCategory(),
                    String.format("%,d", r.getInputSize()),
                    r.getEdgeCount() > 0 ? String.valueOf(r.getEdgeCount()) : "-",
                    BenchmarkResult.formatNs(r.getAverageTimeNs()),
                    BenchmarkResult.formatNs(r.getMinTimeNs()),
                    r.isVerifiedCorrect() ? Ansi.paint(Ansi.BRIGHT_GREEN, "PASS") : Ansi.paint(Ansi.BRIGHT_RED, "FAIL")
            ));
        }
        Console.table(headers, rows);
    }

    private static void exportResults(Scanner sc) {
        Console.section("Export Benchmark Results to CSV");
        Console.prompt("Enter CSV file path [default: benchmark_results.csv]: ");
        String path = sc.hasNextLine() ? sc.nextLine().trim() : "";
        if (path.isEmpty()) path = BenchmarkExporter.DEFAULT_CSV_PATH;
        BenchmarkExporter.exportToCsv(path);
    }
}
