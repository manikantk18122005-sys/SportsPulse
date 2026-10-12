package com.sportspulse.algo.strmatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Z-function: z[i] = length of the longest substring starting at i that matches
 * a prefix of the string. Used here to detect repeated commentary phrases/chants
 * and to do pattern search via the classic "pattern + separator + text" trick.
 */
public final class ZFunction {
    private ZFunction() {}

    public static int[] compute(String s) {
        int n = s.length();
        int[] z = new int[n];
        int l = 0, r = 0;
        for (int i = 1; i < n; i++) {
            if (i < r) z[i] = Math.min(r - i, z[i - l]);
            while (i + z[i] < n && s.charAt(z[i]) == s.charAt(i + z[i])) z[i]++;
            if (i + z[i] > r) { l = i; r = i + z[i]; }
        }
        return z;
    }

    /** Search occurrences of pattern in text using the Z-function trick. */
    public static List<Integer> search(String text, String pattern) {
        List<Integer> res = new ArrayList<>();
        if (pattern.isEmpty()) return res;
        String combined = pattern + "\u0001" + text;
        int[] z = compute(combined);
        int m = pattern.length();
        for (int i = m + 1; i < combined.length(); i++) {
            if (z[i] >= m) res.add(i - m - 1);
        }
        return res;
    }

    /**
     * Detects repeated "chant" phrases of a given minimum length that occur more than once,
     * by scanning z-values for every rotation-free repeated block (simple repetition finder).
     * Returns list of (phrase, occurrenceStartIndices) style found via sliding windows.
     */
    public static List<String> findRepeatedPhrases(String text, int windowWords, int minRepeats) {
        String[] words = text.trim().split("\\s+");
        List<String> repeats = new ArrayList<>();
        if (words.length < windowWords) return repeats;
        java.util.Map<String, Integer> counts = new java.util.LinkedHashMap<>();
        for (int i = 0; i + windowWords <= words.length; i++) {
            StringBuilder sb = new StringBuilder();
            for (int k = 0; k < windowWords; k++) sb.append(words[i + k]).append(' ');
            String phrase = sb.toString().trim();
            counts.merge(phrase, 1, Integer::sum);
        }
        for (var e : counts.entrySet()) {
            if (e.getValue() >= minRepeats) repeats.add(e.getKey() + "  (x" + e.getValue() + ")");
        }
        return repeats;
    }
}
