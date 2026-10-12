package com.sportspulse.algo.strmatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Knuth-Morris-Pratt exact string matching.
 * Used for: fast searching of player names / team names / event keywords inside
 * a large commentary or event-log stream, in O(n + m).
 */
public final class KMP {
    private KMP() {}

    /** Builds the longest-proper-prefix-suffix (failure) table for the pattern. */
    private static int[] buildLPS(String pattern) {
        int m = pattern.length();
        int[] lps = new int[m];
        int len = 0;
        int i = 1;
        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                lps[i++] = ++len;
            } else if (len != 0) {
                len = lps[len - 1];
            } else {
                lps[i++] = 0;
            }
        }
        return lps;
    }

    /** Returns all starting indices (0-based) where pattern occurs in text. */
    public static List<Integer> search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        if (pattern.isEmpty() || text.isEmpty() || pattern.length() > text.length()) return matches;
        int[] lps = buildLPS(pattern);
        int n = text.length(), m = pattern.length();
        int i = 0, j = 0;
        while (i < n) {
            if (text.charAt(i) == pattern.charAt(j)) {
                i++; j++;
                if (j == m) {
                    matches.add(i - j);
                    j = lps[j - 1];
                }
            } else if (j != 0) {
                j = lps[j - 1];
            } else {
                i++;
            }
        }
        return matches;
    }
}
