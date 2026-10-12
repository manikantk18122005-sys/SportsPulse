package com.sportspulse.algo.strmatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Textbook Naive (Brute-Force) String Matching Algorithm.
 *
 * Used for:
 * Baseline comparative analysis against KMP in the Empirical Complexity Lab.
 *
 * Theoretical Complexity:
 * - Time: O((n - m + 1) * m) worst case, where n = text length, m = pattern length.
 *         O(n) best/average case on natural language with small alphabet overlap.
 * - Space: O(1) auxiliary space (beyond match index storage).
 */
public final class NaiveSearch {

    private NaiveSearch() {}

    /**
     * Searches for all occurrences of pattern in text using sliding-window brute-force comparison.
     *
     * @param text    The source text.
     * @param pattern The search pattern.
     * @return List of 0-based start indices where pattern occurs in text.
     */
    public static List<Integer> search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        if (text == null || pattern == null || pattern.isEmpty() || text.isEmpty() || pattern.length() > text.length()) {
            return matches;
        }

        int n = text.length();
        int m = pattern.length();

        for (int i = 0; i <= n - m; i++) {
            int j = 0;
            while (j < m && text.charAt(i + j) == pattern.charAt(j)) {
                j++;
            }
            if (j == m) {
                matches.add(i);
            }
        }
        return matches;
    }
}
