package com.sportspulse.algo.npc;

import java.util.*;

/**
 * Demonstrates the classic NP-complete reduction chain used for lineup synergy
 * analysis:
 *   3-SAT --> CLIQUE --> INDEPENDENT SET --> VERTEX COVER
 * plus a fast Vertex-Cover 2-approximation for when exact search is intractable.
 *
 * Graph model here: vertices = players, edge (i,j) = "compatible/synergistic pair".
 * - Max CLIQUE in the compatibility graph = largest fully mutually-compatible
 *   sub-group of players (a candidate high-synergy lineup core).
 * - Max INDEPENDENT SET in the *conflict* graph (complement) = largest group
 *   with no pairwise conflicts.
 * - MIN VERTEX COVER of the conflict graph = smallest set of players whose
 *   removal (or bench assignment) eliminates every tactical conflict; by
 *   duality, complement of vertex cover in an n-vertex graph = independent set.
 */
public final class GraphReductions {
    private GraphReductions() {}

    /** Brute-force / branch-and-bound max clique for small n (<= ~24). Exponential worst case. */
    public static List<Integer> maxClique(boolean[][] adj, int n) {
        List<Integer> best = new ArrayList<>();
        List<Integer> current = new ArrayList<>();
        List<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < n; i++) candidates.add(i);
        expandClique(adj, current, candidates, best);
        return best;
    }

    private static void expandClique(boolean[][] adj, List<Integer> current, List<Integer> candidates, List<Integer> best) {
        if (candidates.isEmpty()) {
            if (current.size() > best.size()) { best.clear(); best.addAll(current); }
            return;
        }
        if (current.size() + candidates.size() <= best.size()) return; // prune
        for (int i = 0; i < candidates.size(); i++) {
            int v = candidates.get(i);
            List<Integer> newCandidates = new ArrayList<>();
            for (int j = i + 1; j < candidates.size(); j++) {
                int u = candidates.get(j);
                if (adj[v][u]) newCandidates.add(u);
            }
            current.add(v);
            expandClique(adj, current, newCandidates, best);
            current.remove(current.size() - 1);
        }
    }

    /** Complement graph: flips edges (no self loops). Used to move between clique/independent-set views. */
    public static boolean[][] complement(boolean[][] adj, int n) {
        boolean[][] comp = new boolean[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (i != j) comp[i][j] = !adj[i][j];
        return comp;
    }

    /** Max independent set = max clique on the complement graph. */
    public static List<Integer> maxIndependentSet(boolean[][] conflictAdj, int n) {
        return maxClique(complement(conflictAdj, n), n);
    }

    /** Exact min vertex cover for small n: complement of the max independent set. */
    public static List<Integer> minVertexCoverExact(boolean[][] adj, int n) {
        List<Integer> mis = maxIndependentSet(adj, n);
        Set<Integer> misSet = new HashSet<>(mis);
        List<Integer> cover = new ArrayList<>();
        for (int i = 0; i < n; i++) if (!misSet.contains(i)) cover.add(i);
        return cover;
    }

    /**
     * Vertex-Cover 2-approximation: repeatedly pick any uncovered edge, add BOTH
     * endpoints to the cover, remove all edges touching them. Guaranteed <= 2x optimal.
     * Runs in O(V+E) -- used when n is too large for exact search.
     */
    public static Set<Integer> vertexCover2Approx(boolean[][] adj, int n) {
        boolean[][] remaining = new boolean[n][n];
        for (int i = 0; i < n; i++) remaining[i] = adj[i].clone();
        Set<Integer> cover = new LinkedHashSet<>();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (remaining[i][j]) {
                    cover.add(i);
                    cover.add(j);
                    for (int k = 0; k < n; k++) {
                        remaining[i][k] = remaining[k][i] = false;
                        remaining[j][k] = remaining[k][j] = false;
                    }
                }
            }
        }
        return cover;
    }
}
