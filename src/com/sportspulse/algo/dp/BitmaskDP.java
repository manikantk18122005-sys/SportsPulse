package com.sportspulse.algo.dp;

import com.sportspulse.model.Player;

import java.util.*;

/**
 * Bitmask DP over small player pools (n <= ~20) to select the best K-man lineup
 * that maximizes total skill + pairwise synergy. State = subset mask, transition
 * adds one more player; O(2^n * n).
 */
public final class BitmaskDP {
    private BitmaskDP() {}

    public static class Result {
        public final List<Player> lineup;
        public final double score;
        public Result(List<Player> lineup, double score) { this.lineup = lineup; this.score = score; }
    }

    /**
     * @param players    candidate pool (should be small, <= 20, for feasibility)
     * @param k          exact lineup size required
     * @param synergy    synergy[i][j] = extra score for having both player i and j together
     */
    public static Result bestLineup(List<Player> players, int k, double[][] synergy) {
        int n = players.size();
        if (n > 22) throw new IllegalArgumentException("Bitmask DP pool too large (max 22): " + n);
        int full = 1 << n;

        // dp[mask] = best score achievable using exactly popcount(mask) players = mask's players
        double[] dp = new double[full];
        int[] parentPop = new int[full]; // not needed explicitly; we recompute by iterating masks with exactly k bits
        Arrays.fill(dp, Double.NEGATIVE_INFINITY);
        dp[0] = 0.0;

        for (int mask = 0; mask < full; mask++) {
            if (dp[mask] == Double.NEGATIVE_INFINITY) continue;
            int popcount = Integer.bitCount(mask);
            if (popcount >= k) continue;
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) continue;
                double added = players.get(i).getSkill();
                // add synergy with every player already in the mask
                for (int j = 0; j < n; j++) {
                    if ((mask & (1 << j)) != 0) added += synergy[i][j];
                }
                int newMask = mask | (1 << i);
                double newScore = dp[mask] + added;
                if (newScore > dp[newMask]) dp[newMask] = newScore;
            }
        }

        double best = Double.NEGATIVE_INFINITY;
        int bestMask = 0;
        for (int mask = 0; mask < full; mask++) {
            if (Integer.bitCount(mask) == k && dp[mask] > best) {
                best = dp[mask];
                bestMask = mask;
            }
        }

        List<Player> lineup = new ArrayList<>();
        for (int i = 0; i < n; i++) if ((bestMask & (1 << i)) != 0) lineup.add(players.get(i));
        return new Result(lineup, best == Double.NEGATIVE_INFINITY ? 0 : best);
    }
}
