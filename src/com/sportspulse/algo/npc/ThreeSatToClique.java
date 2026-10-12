package com.sportspulse.algo.npc;

import java.util.*;

/**
 * Classic textbook reduction: 3-SAT instance (k clauses, each with exactly 3
 * literals) --> a graph with 3k vertices (one per literal-occurrence, grouped
 * into k groups of 3). Edge between two literal-occurrences from DIFFERENT
 * clauses iff they are not negations of each other. The formula is satisfiable
 * iff this graph has a clique of size k (one vertex chosen per clause-group,
 * mutually consistent). Used here to translate "roster availability rules"
 * (expressed as 3-SAT clauses over player/position booleans) into a compatible
 * player-selection clique problem.
 */
public final class ThreeSatToClique {
    private ThreeSatToClique() {}

    public static class LiteralNode {
        public final int clauseIndex;
        public final int literal; // signed variable id
        LiteralNode(int clauseIndex, int literal) { this.clauseIndex = clauseIndex; this.literal = literal; }
        @Override public String toString() {
            return "C" + clauseIndex + ":" + (literal > 0 ? "x" + literal : "!x" + (-literal));
        }
    }

    public static class Reduction {
        public final List<LiteralNode> nodes;
        public final boolean[][] adjacency;
        public final int targetCliqueSize;
        Reduction(List<LiteralNode> nodes, boolean[][] adjacency, int targetCliqueSize) {
            this.nodes = nodes; this.adjacency = adjacency; this.targetCliqueSize = targetCliqueSize;
        }
    }

    /** clauses: each int[] must have length 3 (three signed literals). */
    public static Reduction reduce(List<int[]> clauses) {
        List<LiteralNode> nodes = new ArrayList<>();
        for (int c = 0; c < clauses.size(); c++) {
            for (int lit : clauses.get(c)) nodes.add(new LiteralNode(c, lit));
        }
        int n = nodes.size();
        boolean[][] adj = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                LiteralNode a = nodes.get(i), b = nodes.get(j);
                boolean sameClause = a.clauseIndex == b.clauseIndex;
                boolean negations = a.literal == -b.literal;
                adj[i][j] = !sameClause && !negations;
            }
        }
        return new Reduction(nodes, adj, clauses.size());
    }

    /** Runs the reduction then checks satisfiability via max-clique search. */
    public static boolean isSatisfiableViaClique(List<int[]> clauses) {
        Reduction r = reduce(clauses);
        List<Integer> clique = GraphReductions.maxClique(r.adjacency, r.nodes.size());
        return clique.size() >= r.targetCliqueSize;
    }
}
