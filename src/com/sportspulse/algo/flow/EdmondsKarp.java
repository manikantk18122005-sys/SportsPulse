package com.sportspulse.algo.flow;

import java.util.*;

/**
 * Edmonds-Karp: Ford-Fulkerson specialized with BFS shortest augmenting paths,
 * giving a guaranteed O(V * E^2) bound. Used here for capacity-based bottleneck
 * detection in stadium/facility throughput (identifies the min-cut, i.e. the
 * limiting resource edges).
 */
public final class EdmondsKarp {
    private EdmondsKarp() {}

    public static class Result {
        public final int maxFlow;
        public final List<int[]> minCutEdges; // pairs (fromNode, toNode) that saturate the min cut
        public Result(int maxFlow, List<int[]> minCutEdges) { this.maxFlow = maxFlow; this.minCutEdges = minCutEdges; }
    }

    public static Result maxFlowWithMinCut(FlowGraph g, int source, int sink) {
        int flow = 0;
        while (true) {
            FlowGraph.Edge[] parentEdge = new FlowGraph.Edge[g.n];
            int[] parentNode = new int[g.n];
            Arrays.fill(parentNode, -1);
            Queue<Integer> queue = new LinkedList<>();
            queue.add(source);
            parentNode[source] = source;
            while (!queue.isEmpty() && parentNode[sink] == -1) {
                int u = queue.poll();
                for (FlowGraph.Edge e : g.edgesFrom(u)) {
                    if (parentNode[e.to] == -1 && e.cap - e.flow > 0) {
                        parentNode[e.to] = u;
                        parentEdge[e.to] = e;
                        queue.add(e.to);
                    }
                }
            }
            if (parentNode[sink] == -1) break; // no augmenting path
            int bottleneck = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parentNode[v]) {
                bottleneck = Math.min(bottleneck, parentEdge[v].cap - parentEdge[v].flow);
            }
            for (int v = sink; v != source; v = parentNode[v]) {
                FlowGraph.Edge e = parentEdge[v];
                e.flow += bottleneck;
                g.adj.get(e.to).get(e.reverseIndex).flow -= bottleneck;
            }
            flow += bottleneck;
        }

        // find min-cut: nodes reachable from source in residual graph after max flow
        boolean[] reachable = new boolean[g.n];
        Queue<Integer> queue = new LinkedList<>();
        queue.add(source);
        reachable[source] = true;
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (FlowGraph.Edge e : g.edgesFrom(u)) {
                if (!reachable[e.to] && e.cap - e.flow > 0) {
                    reachable[e.to] = true;
                    queue.add(e.to);
                }
            }
        }
        List<int[]> cutEdges = new ArrayList<>();
        for (int u = 0; u < g.n; u++) {
            if (!reachable[u]) continue;
            for (FlowGraph.Edge e : g.edgesFrom(u)) {
                if (e.cap > 0 && !reachable[e.to]) cutEdges.add(new int[]{u, e.to});
            }
        }
        return new Result(flow, cutEdges);
    }
}
