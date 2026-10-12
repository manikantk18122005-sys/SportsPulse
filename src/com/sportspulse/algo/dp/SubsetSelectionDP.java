package com.sportspulse.algo.dp;

import com.sportspulse.model.Player;

import java.util.*;

/**
 * DP on subsets (0/1 knapsack-style, generalized): choose a subset of players
 * maximizing total skill subject to a hard budget cap. This is a classic
 * bounded-knapsack DP over (item index, remaining budget), O(n * Budget).
 * Complements BitmaskDP (exact-size, synergy-aware) with a budget-constrained,
 * larger-n variant used for full-squad selection.
 */
public final class SubsetSelectionDP {
    private SubsetSelectionDP() {}

    public static class Result {
        public final List<Player> selected;
        public final double totalSkill;
        public final double totalCost;
        public Result(List<Player> selected, double totalSkill, double totalCost) {
            this.selected = selected; this.totalSkill = totalSkill; this.totalCost = totalCost;
        }
    }

    /** Budget is discretized to integer units for DP table indexing. */
    public static Result select(List<Player> players, int budgetUnits) {
        int n = players.size();
        double[][] dp = new double[n + 1][budgetUnits + 1];
        boolean[][] take = new boolean[n + 1][budgetUnits + 1];

        for (int i = 1; i <= n; i++) {
            Player p = players.get(i - 1);
            int cost = (int) Math.round(p.getCost());
            double skill = p.getSkill();
            for (int b = 0; b <= budgetUnits; b++) {
                dp[i][b] = dp[i - 1][b]; // don't take
                if (cost <= b && dp[i - 1][b - cost] + skill > dp[i][b]) {
                    dp[i][b] = dp[i - 1][b - cost] + skill;
                    take[i][b] = true;
                }
            }
        }

        List<Player> chosen = new ArrayList<>();
        int b = budgetUnits;
        double totalCost = 0;
        for (int i = n; i >= 1; i--) {
            if (take[i][b]) {
                Player p = players.get(i - 1);
                chosen.add(p);
                b -= (int) Math.round(p.getCost());
                totalCost += p.getCost();
            }
        }
        Collections.reverse(chosen);
        return new Result(chosen, dp[n][budgetUnits], totalCost);
    }
}
