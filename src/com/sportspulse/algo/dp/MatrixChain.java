package com.sportspulse.algo.dp;

/**
 * Matrix-Chain Multiplication DP: given the dimensions of a sequence of
 * transformation matrices in an analytics pipeline (e.g. raw events -> per-player
 * aggregates -> normalized features -> ranking scores), finds the parenthesization
 * (execution order) that minimizes total scalar multiplications. O(n^3).
 */
public final class MatrixChain {
    private MatrixChain() {}

    public static class Result {
        public final long minCost;
        public final String order; // human readable parenthesization
        public Result(long minCost, String order) { this.minCost = minCost; this.order = order; }
    }

    /**
     * @param dims dimensions array of length n+1 describing n matrices,
     *             where matrix i has dimensions dims[i-1] x dims[i] (1-indexed logically)
     * @param stageNames optional labels for each of the n matrices/stages
     */
    public static Result solve(int[] dims, String[] stageNames) {
        int n = dims.length - 1; // number of matrices/stages
        long[][] dp = new long[n + 1][n + 1];
        int[][] split = new int[n + 1][n + 1];

        for (int len = 2; len <= n; len++) {
            for (int i = 1; i <= n - len + 1; i++) {
                int j = i + len - 1;
                dp[i][j] = Long.MAX_VALUE;
                for (int k = i; k < j; k++) {
                    long cost = dp[i][k] + dp[k + 1][j] + (long) dims[i - 1] * dims[k] * dims[j];
                    if (cost < dp[i][j]) { dp[i][j] = cost; split[i][j] = k; }
                }
            }
        }
        String order = buildOrder(split, 1, n, stageNames);
        return new Result(dp[1][n], order);
    }

    private static String buildOrder(int[][] split, int i, int j, String[] names) {
        if (i == j) return names[i - 1];
        int k = split[i][j];
        return "(" + buildOrder(split, i, k, names) + " x " + buildOrder(split, k + 1, j, names) + ")";
    }
}
