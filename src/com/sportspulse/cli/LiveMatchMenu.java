package com.sportspulse.cli;

import com.sportspulse.algo.rand.*;
import com.sportspulse.data.MockData;
import com.sportspulse.model.Match;
import com.sportspulse.model.MatchEvent;
import com.sportspulse.model.Player;
import com.sportspulse.util.Console;

import java.util.*;

public class LiveMatchMenu {
    private final MockData data;
    private final Scanner sc;

    public LiveMatchMenu(MockData data, Scanner sc) { this.data = data; this.sc = sc; }

    public void run() {
        boolean back = false;
        while (!back) {
            Console.header("LIVE MATCH CENTER  --  Real-Time Event Stream Analytics");
            Console.bullet("1. Simulate & sample a high-velocity event stream  (Reservoir Sampling)");
            Console.bullet("2. Cumulative stat totals over match minutes  (Blelloch Parallel Scan)");
            Console.bullet("3. Multi-threaded aggregate metrics  (Parallel Reduce)");
            Console.bullet("4. Player leaderboard ranking  (Randomized QuickSort)");
            Console.bullet("5. Generate & verify prime-valued transaction IDs  (Miller-Rabin)");
            Console.bullet("6. Estimate parallel speedup for analytics job  (Brent's Theorem)");
            Console.bullet("0. Back to main menu");
            Console.prompt("\nSelect an option: ");
            switch (sc.nextLine().trim()) {
                case "1" -> reservoirDemo();
                case "2" -> blellochDemo();
                case "3" -> parallelReduceDemo();
                case "4" -> leaderboardDemo();
                case "5" -> millerRabinDemo();
                case "6" -> brentDemo();
                case "0" -> back = true;
                default -> Console.error("Invalid option.");
            }
            if (!back) Console.pause(sc);
        }
    }

    private Match pickMatch() {
        System.out.println("Matches available:");
        for (int i = 0; i < data.allMatches.size(); i++) System.out.println("  " + (i + 1) + ". " + data.allMatches.get(i));
        System.out.print("Pick a match number: ");
        try {
            int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
            if (idx < 0 || idx >= data.allMatches.size()) { Console.warn("Out of range."); return null; }
            return data.allMatches.get(idx);
        } catch (NumberFormatException e) { Console.error("Invalid number."); return null; }
    }

    private void reservoirDemo() {
        Console.section("Reservoir Sampling over a Simulated High-Velocity Stream");
        Match m = pickMatch();
        if (m == null) return;
        // Simulate a much larger event stream by replaying the match's small set of real
        // events (there are only a handful per match) many times, as a high-velocity feed
        // would repeat similar event types across a long broadcast. Each replay is tagged
        // with a unique sequence number (EVT-#####) so that two samples with the same
        // underlying event type/description are still visibly distinct stream entries.
        List<String> simulatedStream = new ArrayList<>();
        Random rng = new Random();
        for (int i = 1; i <= 5000; i++) {
            MatchEvent base = m.getEvents().get(rng.nextInt(m.getEvents().size()));
            simulatedStream.add(String.format("EVT-%05d  %s", i, base));
        }
        int k = 8;
        List<String> sample = ReservoirSampling.sample(simulatedStream, k);
        Console.kv("Simulated stream length", simulatedStream.size());
        Console.kv("Unbiased sample size", k);
        System.out.println("Sample drawn (each EVT-##### is a distinct point in the stream,");
        System.out.println("even if two entries replay the same underlying match event):");
        for (String e : sample) System.out.println("  " + e);
    }

    private void blellochDemo() {
        Console.section("Blelloch Parallel Scan: Cumulative Stats Over Time");
        Match m = pickMatch();
        if (m == null) return;
        int maxMinute = m.getEvents().stream().mapToInt(MatchEvent::getMinute).max().orElse(90);
        int[] perMinuteCount = new int[maxMinute + 1];
        for (MatchEvent e : m.getEvents()) perMinuteCount[e.getMinute()]++;
        int[] running = BlellochScan.inclusiveScan(perMinuteCount);
        Console.kv("Total events", Arrays.stream(perMinuteCount).sum());
        System.out.println("Minute : events-that-minute : running total");
        for (int i = 0; i <= maxMinute; i++) {
            if (perMinuteCount[i] > 0) System.out.printf("  %3d    :       %d          :     %d%n", i, perMinuteCount[i], running[i]);
        }
    }

    private void parallelReduceDemo() {
        Console.section("Parallel Reduce: Aggregate Match Metrics");
        Match m = pickMatch();
        if (m == null) return;
        double totalSkillOfInvolved = ParallelReduce.sum(m.getEvents(), e -> {
            Player p = data.findById(e.getPlayerId());
            return p == null ? 0 : p.getSkill();
        });
        Console.kv("Number of events aggregated", m.getEvents().size());
        Console.kv("Sum of skill ratings of players involved in events", String.format("%.1f", totalSkillOfInvolved));
        System.out.println("  (Computed via Java Fork/Join RecursiveTask splitting the event list across threads.)");
    }

    private void leaderboardDemo() {
        Console.section("Randomized QuickSort: Player Leaderboard");
        System.out.println("Ranking ALL players across every sport by skill rating (descending):");
        List<Player> all = new ArrayList<>(data.allPlayers);
        RandomizedQuickSort.sort(all, Comparator.comparingDouble(Player::getSkill).reversed());
        int rank = 1;
        for (Player p : all) {
            System.out.printf("  #%-3d %s%n", rank++, p);
        }
    }

    private void millerRabinDemo() {
        Console.section("Miller-Rabin: Probabilistic Primality Testing");
        System.out.println("Generating 5 prime-valued candidate transaction IDs (Miller-Rabin used to test candidates for primality):");
        for (int i = 0; i < 5; i++) {
            long id = MillerRabin.generateSecureId(9);
            System.out.println("  TXN-" + id + "   (verified prime: " + MillerRabin.isProbablePrime(id, 20) + ")");
        }
        System.out.print("\nEnter a number to primality-test yourself: ");
        try {
            long n = Long.parseLong(sc.nextLine().trim());
            boolean prime = MillerRabin.isProbablePrime(n, 20);
            Console.kv(n + " is prime? (Miller-Rabin, 20 rounds)", prime ? "YES" : "NO");
        } catch (NumberFormatException e) { System.out.println("Skipped (not a valid number)."); }
    }

    private void brentDemo() {
        Console.section("Brent's Theorem: Parallel Work-Time Estimation");
        System.out.print("Enter total WORK units for the analytics job (e.g. 1000000): ");
        long work = safeLong(sc.nextLine().trim(), 1_000_000);
        System.out.print("Enter critical-path DEPTH (e.g. 20): ");
        long depth = safeLong(sc.nextLine().trim(), 20);
        System.out.print("Sweep how many processor counts (e.g. 8): ");
        int maxP = (int) safeLong(sc.nextLine().trim(), 8);

        BrentTheorem.Estimate[] sweep = BrentTheorem.sweep(work, depth, Math.max(1, maxP));
        System.out.println("Brent's bound:    T_p <= W/p + T_inf   (T_inf = critical-path depth = " + depth + ")");
        System.out.println("Theoretical min:  T_p >= max(W/p, T_inf)");
        System.out.println();
        System.out.println("Processors | Lower bound | Brent est. | Speedup | Efficiency");
        for (BrentTheorem.Estimate est : sweep) {
            double lowerBound = Math.max((double) work / est.processors, depth);
            System.out.printf("  %-10d | %-11.1f | %-10.1f | %-7.2fx | %.0f%%%n",
                    est.processors, lowerBound, est.estimatedTime, est.speedupVsSequential, est.efficiency * 100);
        }
        if (work / (double) maxP < depth * 5) {
            System.out.println();
            System.out.println("Note: work is not much larger than depth*processors here, so the critical-path");
            System.out.println("term T_inf is a real constraint on speedup, not just a rounding artifact.");
        } else {
            System.out.println();
            System.out.println("Note: work dominates depth at this scale, so speedup tracks close to linear (p x)");
            System.out.println("and the critical-path depth barely limits parallelism yet. Try a much larger");
            System.out.println("depth relative to work to see Brent's bound actually constrain speedup.");
        }
    }

    private long safeLong(String s, long fallback) {
        try { return Long.parseLong(s); } catch (Exception e) { return fallback; }
    }
}
