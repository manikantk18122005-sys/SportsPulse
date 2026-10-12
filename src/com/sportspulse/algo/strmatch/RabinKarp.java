package com.sportspulse.algo.strmatch;

import java.util.*;

/**
 * Rabin-Karp rolling-hash string matching, extended to multi-pattern batch search.
 * Used for: quick batch lookups of player IDs / match IDs against a big log of text,
 * hashing each ID once and rolling a window hash across the text.
 */
public final class RabinKarp {
    private static final long BASE = 256L;
    private static final long MOD = 1_000_000_007L;

    private RabinKarp() {}

    public static List<Integer> search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        int n = text.length(), m = pattern.length();
        if (m == 0 || m > n) return matches;

        long patternHash = 0, windowHash = 0, highOrder = 1;
        for (int i = 0; i < m - 1; i++) highOrder = (highOrder * BASE) % MOD;

        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * BASE + pattern.charAt(i)) % MOD;
            windowHash = (windowHash * BASE + text.charAt(i)) % MOD;
        }

        for (int i = 0; i <= n - m; i++) {
            if (patternHash == windowHash && text.regionMatches(i, pattern, 0, m)) {
                matches.add(i);
            }
            if (i < n - m) {
                windowHash = (windowHash - text.charAt(i) * highOrder % MOD + MOD) % MOD;
                windowHash = (windowHash * BASE + text.charAt(i + m)) % MOD;
            }
        }
        return matches;
    }

    /**
     * Batch search: given a set of fixed-length IDs (e.g. player/match IDs, all length k),
     * finds every ID present in the text using a single rolling hash pass over text
     * of window size k, checked against a hash-set of pattern hashes. O(n*k) worst case
     * on collision but average O(n + total pattern length).
     */
    public static Map<String, List<Integer>> batchSearch(String text, List<String> ids) {
        Map<String, List<Integer>> results = new LinkedHashMap<>();
        for (String id : ids) results.put(id, new ArrayList<>());
        // group ids by length since Rabin-Karp needs a fixed window per length
        Map<Integer, List<String>> byLen = new HashMap<>();
        for (String id : ids) byLen.computeIfAbsent(id.length(), k -> new ArrayList<>()).add(id);

        for (var entry : byLen.entrySet()) {
            int k = entry.getKey();
            if (k == 0 || k > text.length()) continue;
            Map<Long, List<String>> hashToIds = new HashMap<>();
            for (String id : entry.getValue()) {
                hashToIds.computeIfAbsent(hashOf(id), h -> new ArrayList<>()).add(id);
            }
            long windowHash = 0, highOrder = 1;
            for (int i = 0; i < k - 1; i++) highOrder = (highOrder * BASE) % MOD;
            for (int i = 0; i < k; i++) windowHash = (windowHash * BASE + text.charAt(i)) % MOD;

            for (int i = 0; i <= text.length() - k; i++) {
                List<String> candidates = hashToIds.get(windowHash);
                if (candidates != null) {
                    for (String cand : candidates) {
                        if (text.regionMatches(i, cand, 0, k)) results.get(cand).add(i);
                    }
                }
                if (i < text.length() - k) {
                    windowHash = (windowHash - text.charAt(i) * highOrder % MOD + MOD) % MOD;
                    windowHash = (windowHash * BASE + text.charAt(i + k)) % MOD;
                }
            }
        }
        return results;
    }

    private static long hashOf(String s) {
        long h = 0;
        for (int i = 0; i < s.length(); i++) h = (h * BASE + s.charAt(i)) % MOD;
        return h;
    }
}
