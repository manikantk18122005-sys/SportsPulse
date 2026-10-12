package com.sportspulse.algo.flow;

import java.util.*;

/**
 * Hopcroft-Karp algorithm for maximum bipartite matching in O(E * sqrt(V)),
 * used for optimal assignment of players to positions/roles/training slots.
 * Also applies König's theorem to derive the minimum vertex cover (and hence
 * the maximum independent set) from the matching + alternating-path search,
 * used to locate core player-position conflicts.
 */
public final class BipartiteMatching {
    private final int leftSize, rightSize;
    private final List<List<Integer>> adj; // left -> list of right indices
    private int[] matchLeft, matchRight;
    private int[] dist;
    private static final int INF = Integer.MAX_VALUE;

    public BipartiteMatching(int leftSize, int rightSize) {
        this.leftSize = leftSize;
        this.rightSize = rightSize;
        adj = new ArrayList<>();
        for (int i = 0; i < leftSize; i++) adj.add(new ArrayList<>());
    }

    public void addEdge(int left, int right) { adj.get(left).add(right); }

    public int maxMatching() {
        matchLeft = new int[leftSize];
        matchRight = new int[rightSize];
        Arrays.fill(matchLeft, -1);
        Arrays.fill(matchRight, -1);
        int matching = 0;
        while (bfs()) {
            for (int u = 0; u < leftSize; u++) {
                if (matchLeft[u] == -1 && dfs(u)) matching++;
            }
        }
        return matching;
    }

    private boolean bfs() {
        dist = new int[leftSize];
        Queue<Integer> queue = new LinkedList<>();
        for (int u = 0; u < leftSize; u++) {
            if (matchLeft[u] == -1) { dist[u] = 0; queue.add(u); } else dist[u] = INF;
        }
        boolean foundAugmentingPath = false;
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v : adj.get(u)) {
                int w = matchRight[v];
                if (w == -1) {
                    foundAugmentingPath = true;
                } else if (dist[w] == INF) {
                    dist[w] = dist[u] + 1;
                    queue.add(w);
                }
            }
        }
        return foundAugmentingPath;
    }

    private boolean dfs(int u) {
        for (int v : adj.get(u)) {
            int w = matchRight[v];
            if (w == -1 || (dist[w] == dist[u] + 1 && dfs(w))) {
                matchLeft[u] = v;
                matchRight[v] = u;
                return true;
            }
        }
        dist[u] = INF;
        return false;
    }

    public int[] getMatchLeft() { return matchLeft; }
    public int[] getMatchRight() { return matchRight; }

    /**
     * König's theorem: derives a minimum vertex cover from the maximum matching.
     * Returns {leftCoverIndices, rightCoverIndices}. |cover| == max matching size.
     */
    public int[][] minVertexCoverKonig() {
        // Z = set of vertices reachable via alternating paths from unmatched left vertices
        boolean[] visitedLeft = new boolean[leftSize];
        boolean[] visitedRight = new boolean[rightSize];
        Deque<Integer> stack = new ArrayDeque<>();
        for (int u = 0; u < leftSize; u++) {
            if (matchLeft[u] == -1) { visitedLeft[u] = true; stack.push(u); }
        }
        while (!stack.isEmpty()) {
            int u = stack.pop();
            for (int v : adj.get(u)) {
                if (!visitedRight[v]) {
                    visitedRight[v] = true;
                    int w = matchRight[v];
                    if (w != -1 && !visitedLeft[w]) { visitedLeft[w] = true; stack.push(w); }
                }
            }
        }
        // min vertex cover = (Left \ Z) union (Right ∩ Z)
        List<Integer> leftCover = new ArrayList<>();
        List<Integer> rightCover = new ArrayList<>();
        for (int u = 0; u < leftSize; u++) if (!visitedLeft[u] && matchLeft[u] != -1) leftCover.add(u);
        for (int v = 0; v < rightSize; v++) if (visitedRight[v]) rightCover.add(v);
        return new int[][]{ leftCover.stream().mapToInt(Integer::intValue).toArray(),
                             rightCover.stream().mapToInt(Integer::intValue).toArray() };
    }
}
