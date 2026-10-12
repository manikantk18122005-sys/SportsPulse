package com.sportspulse.cli;

import com.sportspulse.algo.flow.*;
import com.sportspulse.data.MockData;
import com.sportspulse.model.Facility;
import com.sportspulse.model.Player;
import com.sportspulse.util.Console;

import java.util.*;

public class FacilityMenu {
    private final MockData data;
    private final Scanner sc;

    public FacilityMenu(MockData data, Scanner sc) { this.data = data; this.sc = sc; }

    public void run() {
        boolean back = false;
        while (!back) {
            Console.header("FACILITY SCHEDULER  --  Resource Assignment");
            Console.bullet("1. Max resource throughput  (Ford-Fulkerson)");
            Console.bullet("2. Bottleneck / min-cut detection  (Edmonds-Karp)");
            Console.bullet("3. Large-scale scalable flow  (Dinic's Algorithm)");
            Console.bullet("4. Player-to-facility assignment  (Bipartite Matching + Konig's min vertex cover)");
            Console.bullet("0. Back to main menu");
            Console.prompt("\nSelect an option: ");
            switch (sc.nextLine().trim()) {
                case "1" -> fordFulkersonDemo();
                case "2" -> edmondsKarpDemo();
                case "3" -> dinicDemo();
                case "4" -> bipartiteDemo();
                case "0" -> back = true;
                default -> Console.error("Invalid option.");
            }
            if (!back) Console.pause(sc);
        }
    }

    /**
     * Builds a flow network: SOURCE -> each team's demand node -> each facility node
     * (capacity = facility capacity) -> SINK. Team demand capped by how many slots
     * they need this week.
     */
    private FlowGraph buildFacilityNetwork(int[] teamDemands, Map<Integer, int[]> facilityCapAndEdges) {
        // node 0 = source, nodes 1..T = teams, nodes T+1..T+F = facilities, last = sink
        int numTeams = teamDemands.length;
        int numFacilities = data.allFacilities.size();
        int source = 0, sink = 1 + numTeams + numFacilities;
        FlowGraph g = new FlowGraph(sink + 1);
        for (int t = 0; t < numTeams; t++) g.addEdge(source, 1 + t, teamDemands[t]);
        for (int f = 0; f < numFacilities; f++) {
            Facility fac = data.allFacilities.get(f);
            g.addEdge(1 + numTeams + f, sink, fac.getCapacity());
        }
        // every team can use every facility (capacity = facility cap, simplification for demo)
        for (int t = 0; t < numTeams; t++) {
            for (int f = 0; f < numFacilities; f++) {
                Facility fac = data.allFacilities.get(f);
                g.addEdge(1 + t, 1 + numTeams + f, fac.getCapacity());
            }
        }
        return g;
    }

    private void printFacilities() {
        System.out.println("Available facilities:");
        for (Facility f : data.allFacilities) System.out.println("  " + f);
    }

    private void fordFulkersonDemo() {
        Console.section("Ford-Fulkerson: Max Facility Throughput");
        printFacilities();
        int[] demands = {12, 10, 8}; // 3 demo teams needing slots this week
        System.out.println("Demo scenario: 3 teams need " + Arrays.toString(demands) + " training slots respectively.");
        FlowGraph g = buildFacilityNetwork(demands, null);
        int sink = g.n - 1;
        int flow = FordFulkerson.maxFlow(g, 0, sink);
        Console.kv("Maximum slots successfully allocated", flow);
        Console.kv("Total slots requested", Arrays.stream(demands).sum());
    }

    private void edmondsKarpDemo() {
        Console.section("Edmonds-Karp: Bottleneck / Min-Cut Detection");
        printFacilities();
        int[] demands = {15, 14, 13};
        System.out.println("Demo scenario: 3 teams need " + Arrays.toString(demands) + " training slots respectively.");
        FlowGraph g = buildFacilityNetwork(demands, null);
        int sink = g.n - 1;
        EdmondsKarp.Result result = EdmondsKarp.maxFlowWithMinCut(g, 0, sink);
        Console.kv("Maximum flow (slots allocated)", result.maxFlow);
        Console.kv("Min-cut edge count (bottleneck edges)", result.minCutEdges.size());
        System.out.println("  These edges represent the true capacity bottleneck in the network:");
        int totalCutCapacity = 0;
        for (int[] edge : result.minCutEdges) {
            int cap = g.originalCapacity(edge[0], edge[1]);
            totalCutCapacity += cap;
            System.out.printf("    %-14s -> %-24s  capacity=%d%n",
                    nodeLabel(edge[0], demands.length), nodeLabel(edge[1], demands.length), cap);
        }
        Console.kv("Total cut capacity (must equal max flow, by the max-flow/min-cut theorem)", totalCutCapacity);
    }

    /** Human-readable label for a node in the SOURCE -> teams -> facilities -> SINK network. */
    private String nodeLabel(int node, int numTeams) {
        int numFacilities = data.allFacilities.size();
        if (node == 0) return "Source";
        if (node <= numTeams) return "Team " + (char) ('A' + node - 1);
        if (node <= numTeams + numFacilities) return data.allFacilities.get(node - numTeams - 1).getName();
        return "Sink";
    }

    private void dinicDemo() {
        Console.section("Dinic's Algorithm: Scalable Multi-Facility Flow");
        printFacilities();
        int[] demands = {25, 20, 18, 22, 16}; // larger demo with 5 teams
        System.out.println("Demo scenario (larger network): 5 teams need " + Arrays.toString(demands) + " slots.");
        FlowGraph g = buildFacilityNetwork(demands, null);
        int sink = g.n - 1;
        long start = System.nanoTime();
        int flow = new Dinic(g).maxFlow(0, sink);
        long elapsed = System.nanoTime() - start;
        Console.kv("Maximum flow (slots allocated)", flow);
        Console.kv("Total requested", Arrays.stream(demands).sum());
        Console.kv("Computed in", String.format("%.3f ms", elapsed / 1_000_000.0));
    }

    private void bipartiteDemo() {
        Console.section("Bipartite Matching (Hopcroft-Karp) + Konig's Min Vertex Cover");
        System.out.print("Pick a sport (1=Football, 2=Basketball, 3=Cricket): ");
        String choice = sc.nextLine().trim();
        var sport = switch (choice) {
            case "2" -> com.sportspulse.data.Sports.BASKETBALL;
            case "3" -> com.sportspulse.data.Sports.CRICKET;
            default -> com.sportspulse.data.Sports.FOOTBALL;
        };
        List<Player> players = data.playersBySport(sport);
        List<Facility> facilities = data.allFacilities;

        BipartiteMatching bm = new BipartiteMatching(players.size(), facilities.size());
        for (int i = 0; i < players.size(); i++) {
            for (int j = 0; j < facilities.size(); j++) {
                Facility f = facilities.get(j);
                if (f.getSportSpecialty().equals("ANY") || f.getSportSpecialty().equals(sport.getName())) {
                    bm.addEdge(i, j);
                }
            }
        }
        int matched = bm.maxMatching();
        Console.kv("Players", players.size());
        Console.kv("Facilities", facilities.size());
        Console.kv("Maximum matching size (players assigned a training slot)", matched);

        int[] matchLeft = bm.getMatchLeft();
        Console.section("Assignment");
        for (int i = 0; i < players.size(); i++) {
            if (matchLeft[i] != -1) {
                System.out.printf("  %-20s -> %s%n", players.get(i).getName(), facilities.get(matchLeft[i]).getName());
            }
        }
        int[][] cover = bm.minVertexCoverKonig();
        Console.section("Konig's Min Vertex Cover (core conflict points)");
        Console.kv("Vertex cover size (should equal max matching)", cover[0].length + cover[1].length);
        System.out.println("  Player-side cover vertices: " + cover[0].length);
        System.out.println("  Facility-side cover vertices: " + cover[1].length);
    }
}
