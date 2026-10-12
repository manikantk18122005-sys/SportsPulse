package com.sportspulse.algo.dp;

/**
 * Classic and Damerau variants of edit distance, used for player/team name
 * auto-correction, fuzzy autocomplete, and aligning noisy event-log text.
 */
public final class EditDistance {
    private EditDistance() {}

    /** Standard Levenshtein distance: insert, delete, substitute. O(n*m). */
    public static int levenshtein(String a, String b) {
        int n = a.length(), m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[n][m];
    }

    /** Damerau-Levenshtein: Levenshtein + adjacent transposition ("Mainkant" -> "Manikant"). */
    public static int damerauLevenshtein(String a, String b) {
        int n = a.length(), m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
                if (i > 1 && j > 1 && a.charAt(i - 1) == b.charAt(j - 2) && a.charAt(i - 2) == b.charAt(j - 1)) {
                    dp[i][j] = Math.min(dp[i][j], dp[i - 2][j - 2] + 1);
                }
            }
        }
        return dp[n][m];
    }

    /** Finds the closest match to `query` from `candidates` using Damerau-Levenshtein. */
    public static String closestMatch(String query, Iterable<String> candidates) {
        String best = null;
        int bestDist = Integer.MAX_VALUE;
        for (String c : candidates) {
            int d = damerauLevenshtein(query.toLowerCase(), c.toLowerCase());
            if (d < bestDist) { bestDist = d; best = c; }
        }
        return best;
    }
}
