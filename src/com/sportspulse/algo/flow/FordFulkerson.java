package com.sportspulse.algo.flow;

import java.util.*;

/**
 * Ford-Fulkerson method using DFS to find augmenting paths.
 * Used for: resource allocation / continuous physical flow modeling across a
 * sports complex (e.g. players -> shared equipment -> facility slots -> capacity sink).
 * Complexity: O(E * maxFlow) in the worst case with arbitrary integer capacities.
 */
public final class FordFulkerson {
    private FordFulkerson() {}

    public static int maxFlow(FlowGraph g, int source, int sink) {
        int flow = 0;
        while (true) {
            boolean[] visited = new boolean[g.n];
            int pushed = dfs(g, source, sink, Integer.MAX_VALUE, visited);
            if (pushed == 0) break;
            flow += pushed;
        }
        return flow;
    }

    private static int dfs(FlowGraph g, int u, int sink, int pushed, boolean[] visited) {
        if (u == sink) return pushed;
        visited[u] = true;
        for (FlowGraph.Edge e : g.edgesFrom(u)) {
            if (!visited[e.to] && e.cap - e.flow > 0) {
                int newPush = Math.min(pushed, e.cap - e.flow);
                int result = dfs(g, e.to, sink, newPush, visited);
                if (result > 0) {
                    e.flow += result;
                    g.adj.get(e.to).get(e.reverseIndex).flow -= result;
                    return result;
                }
            }
        }
        return 0;
    }
}
