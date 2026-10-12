package com.sportspulse.algo.flow;

import java.util.*;

/**
 * Simple adjacency-list max-flow graph with reverse edges for residual capacity.
 *
 * Upgraded for Empirical Complexity Lab:
 * Includes copy() and resetFlow() so Ford-Fulkerson, Edmonds-Karp, and Dinic
 * can each receive fresh, identical, unmutated graphs during benchmarking.
 */
public class FlowGraph {
    public static class Edge {
        public int to, cap, flow;
        public int reverseIndex;
        public Edge(int to, int cap, int reverseIndex) { this.to = to; this.cap = cap; this.reverseIndex = reverseIndex; }
    }

    public final int n;
    public final List<List<Edge>> adj;
    private final List<int[]> originalEdges = new ArrayList<>();

    public FlowGraph(int n) {
        this.n = n;
        adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
    }

    public void addEdge(int from, int to, int cap) {
        Edge a = new Edge(to, cap, adj.get(to).size());
        Edge b = new Edge(from, 0, adj.get(from).size());
        adj.get(from).add(a);
        adj.get(to).add(b);
        originalEdges.add(new int[]{from, to, cap});
    }

    public List<Edge> edgesFrom(int u) { return adj.get(u); }

    /** Original (pre-flow) capacity of edge u->v, recovered from residual + reverse-residual capacities. */
    public int originalCapacity(int u, int v) {
        int residual = 0, reverseResidual = 0;
        for (Edge e : adj.get(u)) if (e.to == v) { residual = e.cap; break; }
        for (Edge e : adj.get(v)) if (e.to == u) { reverseResidual = e.cap; break; }
        return residual + reverseResidual;
    }

    /**
     * Creates an unmutated deep copy of this flow network with all edge flows set to 0.
     * Essential for fair and valid comparative benchmarking across Ford-Fulkerson,
     * Edmonds-Karp, and Dinic without residual capacity corruption.
     */
    public FlowGraph copy() {
        FlowGraph clone = new FlowGraph(this.n);
        for (int[] e : originalEdges) {
            clone.addEdge(e[0], e[1], e[2]);
        }
        return clone;
    }

    /**
     * Resets all edge flows back to zero across the residual graph.
     */
    public void resetFlow() {
        for (List<Edge> edges : adj) {
            for (Edge e : edges) {
                e.flow = 0;
            }
        }
    }

    public List<int[]> getOriginalEdges() {
        return Collections.unmodifiableList(originalEdges);
    }
}
