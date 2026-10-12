package com.sportspulse.cli;

import com.sportspulse.algo.npc.*;
import com.sportspulse.data.MockData;
import com.sportspulse.data.Sports;
import com.sportspulse.model.Player;
import com.sportspulse.model.Sport;
import com.sportspulse.util.Console;

import java.util.*;

public class SelectionAdvisorMenu {
    private final MockData data;
    private final Scanner sc;
    private final Random rng = new Random(7);

    public SelectionAdvisorMenu(MockData data, Scanner sc) { this.data = data; this.sc = sc; }

    public void run() {
        boolean back = false;
        while (!back) {
            Console.header("SELECTION ADVISOR  --  Constraint-Based Compatible Lineups");
            Console.bullet("1. Check roster availability rules  (SAT / 3-SAT Solver)");
            Console.bullet("2. 3-SAT to CLIQUE reduction demo");
            Console.bullet("3. Find largest fully-compatible player group  (Max Clique)");
            Console.bullet("4. Find largest conflict-free group  (Max Independent Set)");
            Console.bullet("5. Exact minimum conflict resolution set  (Min Vertex Cover)");
            Console.bullet("6. Fast approximate conflict resolution  (Vertex-Cover 2-Approximation)");
            Console.bullet("0. Back to main menu");
            Console.prompt("\nSelect an option: ");
            switch (sc.nextLine().trim()) {
                case "1" -> satDemo();
                case "2" -> threeSatToCliqueDemo();
                case "3" -> maxCliqueDemo();
                case "4" -> independentSetDemo();
                case "5" -> minVertexCoverDemo();
                case "6" -> vertexCoverApproxDemo();
                case "0" -> back = true;
                default -> Console.error("Invalid option.");
            }
            if (!back) Console.pause(sc);
        }
    }

    private void satDemo() {
        Console.section("SAT Solver: Roster Availability Rules");
        System.out.println("Encoding example rules as boolean variables (1=GK-A starts, 2=GK-B starts,");
        System.out.println("3=StrikerA starts, 4=StrikerB starts):");
        System.out.println("  Rule 1: exactly one goalkeeper starts       -> (x1 OR x2) AND (!x1 OR !x2)");
        System.out.println("  Rule 2: if StrikerA starts, StrikerB rests  -> (!x3 OR !x4)");
        System.out.println("  Rule 3: at least one striker must start     -> (x3 OR x4)");
        SATSolver solver = new SATSolver(4);
        solver.addClause(1, 2);
        solver.addClause(-1, -2);
        solver.addClause(-3, -4);
        solver.addClause(3, 4);
        Map<Integer, Boolean> assignment = solver.solve();
        if (assignment == null) {
            System.out.println("UNSATISFIABLE: no valid roster meets all rules.");
        } else {
            System.out.println("SATISFIABLE. One valid assignment:");
            String[] names = {"GK-A starts", "GK-B starts", "StrikerA starts", "StrikerB starts"};
            for (int i = 1; i <= 4; i++) {
                System.out.printf("  x%d (%s) = %s%n", i, names[i - 1], assignment.getOrDefault(i, false));
            }
        }
    }

    private void threeSatToCliqueDemo() {
        Console.section("3-SAT to CLIQUE Reduction");
        List<int[]> clauses = List.of(
                new int[]{1, 2, -3},
                new int[]{-1, 2, 3},
                new int[]{1, -2, 3}
        );
        System.out.println("3-SAT formula: (x1 v x2 v !x3) ^ (!x1 v x2 v x3) ^ (x1 v !x2 v x3)");
        ThreeSatToClique.Reduction r = ThreeSatToClique.reduce(clauses);
        System.out.println("Reduced to a graph with " + r.nodes.size() + " vertices (3 per clause).");
        System.out.println("Target clique size = number of clauses = " + r.targetCliqueSize);
        List<Integer> clique = GraphReductions.maxClique(r.adjacency, r.nodes.size());
        System.out.println("Max clique found, size = " + clique.size() + ":");
        for (int idx : clique) System.out.println("    " + r.nodes.get(idx));
        if (!clique.isEmpty()) {
            boolean allCompatible = true;
            for (int i : clique) for (int j : clique) {
                if (i != j && !r.adjacency[i][j]) allCompatible = false;
            }
            System.out.println(allCompatible
                    ? "    (All selected literals are pairwise compatible -- no literal and its negation both appear.)"
                    : "    (WARNING: selected literals are not all pairwise compatible -- this would indicate a bug.)");
        }
        System.out.println(clique.size() >= r.targetCliqueSize
                ? "==> Formula is SATISFIABLE (clique size meets target)."
                : "==> Formula is UNSATISFIABLE (no clique of required size).");
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

    /** Builds a synthetic compatibility graph among a sport's players (deterministic pseudo-random). */
    private boolean[][] buildCompatibilityGraph(List<Player> players) {
        int n = players.size();
        boolean[][] adj = new boolean[n][n];
        Random localRng = new Random(players.size() * 31L + 7);
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                boolean samePosition = players.get(i).getPosition().equals(players.get(j).getPosition());
                boolean compatible = !samePosition && localRng.nextDouble() > 0.35;
                adj[i][j] = adj[j][i] = compatible;
            }
        }
        return adj;
    }

    private void maxCliqueDemo() {
        Console.section("Max Clique: Largest Fully-Compatible Player Group");
        Sport sport = pickSport();
        List<Player> players = data.playersBySport(sport);
        boolean[][] adj = buildCompatibilityGraph(players);
        List<Integer> clique = GraphReductions.maxClique(adj, players.size());
        Console.kv("Largest fully mutually-compatible group size", clique.size());
        for (int idx : clique) System.out.println("  " + players.get(idx));
    }

    private void independentSetDemo() {
        Console.section("Max Independent Set: Largest Conflict-Free Group");
        Sport sport = pickSport();
        List<Player> players = data.playersBySport(sport);
        boolean[][] conflictAdj = invert(buildCompatibilityGraph(players)); // treat non-compatible as "conflict" edges
        List<Integer> is = GraphReductions.maxIndependentSet(conflictAdj, players.size());
        Console.kv("Largest conflict-free group size", is.size());
        for (int idx : is) System.out.println("  " + players.get(idx));
    }

    private void minVertexCoverDemo() {
        Console.section("Exact Minimum Vertex Cover: Core Conflict Points");
        Sport sport = pickSport();
        List<Player> players = data.playersBySport(sport);
        boolean[][] conflictAdj = invert(buildCompatibilityGraph(players));
        List<Integer> cover = GraphReductions.minVertexCoverExact(conflictAdj, players.size());
        Console.kv("Minimum players needed to bench to remove all conflicts", cover.size());
        for (int idx : cover) System.out.println("  " + players.get(idx));
    }

    private void vertexCoverApproxDemo() {
        Console.section("Vertex-Cover 2-Approximation (fast, for larger pools)");
        Sport sport = pickSport();
        List<Player> players = data.playersBySport(sport);
        boolean[][] conflictAdj = invert(buildCompatibilityGraph(players));
        Set<Integer> cover = GraphReductions.vertexCover2Approx(conflictAdj, players.size());
        Console.kv("Approximate cover size (<= 2x optimal, guaranteed)", cover.size());
        for (int idx : cover) System.out.println("  " + players.get(idx));
    }

    private boolean[][] invert(boolean[][] adj) {
        int n = adj.length;
        boolean[][] inv = new boolean[n][n];
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) if (i != j) inv[i][j] = !adj[i][j];
        return inv;
    }
}
