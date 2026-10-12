package com.sportspulse.algo.flow;

import java.util.*;

/**
 * Dinic's algorithm: level graph (BFS) + blocking flow (DFS with iterator pointers),
 * O(V^2 * E) generally, much faster in practice. Used for scalable multi-facility
 * resource networks where FordFulkerson/EdmondsKarp would be too slow at scale.
 */
public final class Dinic {
    private final FlowGraph g;
    private int[] level;
    private int[] iter;

    public Dinic(FlowGraph g) { this.g = g; }

    public int maxFlow(int source, int sink) {
        int flow = 0;
        while (bfs(source, sink)) {
            iter = new int[g.n];
            int f;
            while ((f = dfs(source, sink, Integer.MAX_VALUE)) > 0) flow += f;
        }
        return flow;
    }

    private boolean bfs(int source, int sink) {
        level = new int[g.n];
        Arrays.fill(level, -1);
        Queue<Integer> queue = new LinkedList<>();
        level[source] = 0;
        queue.add(source);
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (FlowGraph.Edge e : g.edgesFrom(u)) {
                if (level[e.to] < 0 && e.cap - e.flow > 0) {
                    level[e.to] = level[u] + 1;
                    queue.add(e.to);
                }
            }
        }
        return level[sink] >= 0;
    }

    private int dfs(int u, int sink, int pushed) {
        if (u == sink) return pushed;
        for (; iter[u] < g.adj.get(u).size(); iter[u]++) {
            FlowGraph.Edge e = g.adj.get(u).get(iter[u]);
            if (level[e.to] == level[u] + 1 && e.cap - e.flow > 0) {
                int d = dfs(e.to, sink, Math.min(pushed, e.cap - e.flow));
                if (d > 0) {
                    e.flow += d;
                    g.adj.get(e.to).get(e.reverseIndex).flow -= d;
                    return d;
                }
            }
        }
        return 0;
    }
}
