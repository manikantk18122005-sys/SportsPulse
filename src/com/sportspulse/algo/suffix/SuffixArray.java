package com.sportspulse.algo.suffix;

import java.util.*;

/**
 * Suffix array construction using prefix-doubling + rank sorting: O(n log^2 n).
 * Used for large-scale indexing of match reports/commentary archives so we can
 * binary-search for any substring in O(m log n).
 */
public final class SuffixArray {
    private final String text;
    private final int[] sa;     // suffix array: sa[i] = starting index of i-th smallest suffix
    private final int[] rank;   // rank[i] = rank of suffix starting at i

    public SuffixArray(String input) {
        this.text = input + "\u0000"; // sentinel smaller than any char
        int n = text.length();
        sa = new int[n];
        rank = new int[n];
        int[] tmp = new int[n];
        for (int i = 0; i < n; i++) { sa[i] = i; rank[i] = text.charAt(i); }

        for (int k = 1; k < n; k <<= 1) {
            final int kk = k;
            final int[] r = rank;
            Comparator<Integer> cmp = (a, b) -> {
                if (r[a] != r[b]) return Integer.compare(r[a], r[b]);
                int ra = a + kk < n ? r[a + kk] : -1;
                int rb = b + kk < n ? r[b + kk] : -1;
                return Integer.compare(ra, rb);
            };
            Integer[] saBoxed = new Integer[n];
            for (int i = 0; i < n; i++) saBoxed[i] = sa[i];
            Arrays.sort(saBoxed, cmp);
            for (int i = 0; i < n; i++) sa[i] = saBoxed[i];

            tmp[sa[0]] = 0;
            for (int i = 1; i < n; i++) {
                tmp[sa[i]] = tmp[sa[i - 1]] + (cmp.compare(sa[i - 1], sa[i]) < 0 ? 1 : 0);
            }
            System.arraycopy(tmp, 0, rank, 0, n);
            if (rank[sa[n - 1]] == n - 1) break;
        }
    }

    public int[] getSuffixArray() { return sa; }
    public int[] getRank() { return rank; }
    public String getText() { return text; }

    /** Binary search for all occurrences of pattern; O(m log n). */
    public List<Integer> search(String pattern) {
        List<Integer> results = new ArrayList<>();
        int n = sa.length;
        int lo = lowerBound(pattern), hi = upperBound(pattern);
        for (int i = lo; i < hi; i++) results.add(sa[i]);
        Collections.sort(results);
        return results;
    }

    private int lowerBound(String pattern) {
        int lo = 0, hi = sa.length;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            String suffix = text.substring(sa[mid]);
            String cmpStr = suffix.length() > pattern.length() ? suffix.substring(0, pattern.length()) : suffix;
            if (cmpStr.compareTo(pattern) < 0) lo = mid + 1; else hi = mid;
        }
        return lo;
    }

    private int upperBound(String pattern) {
        int lo = 0, hi = sa.length;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            String suffix = text.substring(sa[mid]);
            String cmpStr = suffix.length() > pattern.length() ? suffix.substring(0, pattern.length()) : suffix;
            if (cmpStr.compareTo(pattern) <= 0) lo = mid + 1; else hi = mid;
        }
        return lo;
    }
}
