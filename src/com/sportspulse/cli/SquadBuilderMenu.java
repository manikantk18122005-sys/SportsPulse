package com.sportspulse.cli;

import com.sportspulse.algo.dp.*;
import com.sportspulse.data.MockData;
import com.sportspulse.data.Sports;
import com.sportspulse.model.Player;
import com.sportspulse.model.Sport;
import com.sportspulse.util.Console;

import java.util.*;

public class SquadBuilderMenu {
    private final MockData data;
    private final Scanner sc;
    private final Random rng = new Random(42);

    public SquadBuilderMenu(MockData data, Scanner sc) { this.data = data; this.sc = sc; }

    public void run() {
        boolean back = false;
        while (!back) {
            Console.header("SQUAD BUILDER");
            Console.bullet("1. Best synergy-aware lineup for a sport  (Bitmask DP)");
            Console.bullet("2. Best squad under a budget cap  (DP on subsets / knapsack)");
            Console.bullet("3. Optimize analytics pipeline execution order  (Matrix-Chain Multiplication)");
            Console.bullet("0. Back to main menu");
            Console.prompt("\nSelect an option: ");
            switch (sc.nextLine().trim()) {
                case "1" -> bitmaskLineup();
                case "2" -> knapsackSquad();
                case "3" -> matrixChainDemo();
                case "0" -> back = true;
                default -> Console.error("Invalid option.");
            }
            if (!back) Console.pause(sc);
        }
    }

    private Sport pickSport() {
        System.out.println("Sports: 1. Football  2. Basketball  3. Cricket");
        System.out.print("Choose sport: ");
        return switch (sc.nextLine().trim()) {
            case "2" -> Sports.BASKETBALL;
            case "3" -> Sports.CRICKET;
            default -> Sports.FOOTBALL;
        };
    }

    private void bitmaskLineup() {
        Console.section("Bitmask DP: Synergy-Aware Lineup Selection");
        Sport sport = pickSport();
        List<Player> pool = data.playersBySport(sport);
        int k = Math.min(pool.size(), sport.getName().equals("Basketball") ? 3 : 5);
        System.out.print("How many players to select (max " + pool.size() + ", suggested " + k + "): ");
        String in = sc.nextLine().trim();
        int desiredK = parseIntOrDefault(in, k);
        if (desiredK < 1 || desiredK > pool.size()) { System.out.println("Out of range, using suggested " + k + "."); desiredK = k; }

        int n = pool.size();
        double[][] synergy = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double s = rng.nextDouble() * 5; // small pseudo-random synergy bonus, deterministic seed
                if (pool.get(i).getPosition().equals(pool.get(j).getPosition())) s -= 2; // discourage duplicate positions
                synergy[i][j] = s;
                synergy[j][i] = s;
            }
        }
        BitmaskDP.Result result = BitmaskDP.bestLineup(pool, desiredK, synergy);
        Console.kv("Optimal total score (skill + synergy)", String.format("%.2f", result.score));
        Console.section("Selected Lineup");
        for (Player p : result.lineup) System.out.println("  " + p);
    }

    private void knapsackSquad() {
        Console.section("DP on Subsets: Budget-Constrained Squad Selection");
        Sport sport = pickSport();
        List<Player> pool = data.playersBySport(sport);
        double avgCost = pool.stream().mapToDouble(Player::getCost).average().orElse(50);
        int suggestedBudget = (int) Math.round(avgCost * sport.getSquadSize() * 0.7);
        System.out.print("Enter total budget units (suggested ~" + suggestedBudget + "): ");
        String in = sc.nextLine().trim();
        int budget = parseIntOrDefault(in, suggestedBudget);
        if (budget < 0) { System.out.println("Invalid budget, using suggested " + suggestedBudget + "."); budget = suggestedBudget; }

        SubsetSelectionDP.Result result = SubsetSelectionDP.select(pool, budget);
        Console.kv("Total skill achieved", String.format("%.1f", result.totalSkill));
        Console.kv("Total cost used", String.format("%.1f / %d", result.totalCost, budget));
        Console.section("Selected Squad");
        for (Player p : result.selected) System.out.println("  " + p);
    }

    private void matrixChainDemo() {
        Console.section("Matrix-Chain Multiplication: Analytics Pipeline Ordering");
        System.out.println("Example pipeline: RawEvents -> PlayerAggregates -> NormalizedFeatures -> RankingScores -> Report");
        int[] dims = {5000, 40, 25, 15, 8}; // dims.length = stages+1
        String[] stages = {"RawEvents->PlayerAgg", "PlayerAgg->Normalized", "Normalized->RankingScores", "RankingScores->Report"};
        MatrixChain.Result result = MatrixChain.solve(dims, stages);
        Console.kv("Minimum scalar multiplications", result.minCost);
        Console.kv("Optimal execution order", result.order);
        System.out.println("\n  (Dimensions used: " + Arrays.toString(dims) + ")");
    }

    private int parseIntOrDefault(String s, int fallback) {
        if (s.isBlank()) return fallback;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return fallback; }
    }
}
