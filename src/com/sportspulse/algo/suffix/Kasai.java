package com.sportspulse.algo.suffix;

/**
 * Kasai's algorithm: builds the LCP (Longest Common Prefix) array from a suffix
 * array in O(n). Used to find the longest repeated substring in a report (self
 * similarity / duplicate commentary phrase) and, applied to a concatenation of
 * two reports, to measure cross-report similarity/plagiarism.
 */
public final class Kasai {
    private Kasai() {}

    public static int[] buildLCP(SuffixArray suffixArray) {
        int[] sa = suffixArray.getSuffixArray();
        int[] rank = suffixArray.getRank();
        String text = suffixArray.getText();
        int n = sa.length;
        int[] lcp = new int[n];
        int h = 0;
        for (int i = 0; i < n; i++) {
            if (rank[i] > 0) {
                int j = sa[rank[i] - 1];
                while (i + h < n && j + h < n && text.charAt(i + h) == text.charAt(j + h)) h++;
                lcp[rank[i]] = h;
                if (h > 0) h--;
            } else {
                h = 0;
            }
        }
        return lcp;
    }

    /** Longest repeated substring in the text (excluding the sentinel), using LCP. */
    public static String longestRepeatedSubstring(String text) {
        SuffixArray sa = new SuffixArray(text);
        int[] lcp = buildLCP(sa);
        int bestLen = 0, bestIdx = -1;
        for (int i = 1; i < lcp.length; i++) {
            if (lcp[i] > bestLen) { bestLen = lcp[i]; bestIdx = sa.getSuffixArray()[i]; }
        }
        if (bestIdx == -1 || bestLen == 0) return "";
        return text.substring(bestIdx, Math.min(bestIdx + bestLen, text.length()));
    }

    /**
     * Approximate similarity score (0-1) between two texts, computed as the
     * fraction of the shorter text's length that is covered by the single
     * longest common substring found via a combined suffix array + LCP scan.
     */
    public static double similarity(String a, String b) {
        String sep = "\u0002";
        String combined = a + sep + b;
        SuffixArray sa = new SuffixArray(combined);
        int[] lcp = buildLCP(sa);
        int[] suf = sa.getSuffixArray();
        int splitPoint = a.length(); // index of separator in combined
        int best = 0;
        for (int i = 1; i < suf.length; i++) {
            boolean prevInA = suf[i - 1] < splitPoint;
            boolean curInA = suf[i] < splitPoint;
            if (prevInA != curInA) { // one suffix from each side -> valid common substring candidate
                best = Math.max(best, lcp[i]);
            }
        }
        int shorter = Math.min(a.length(), b.length());
        if (shorter == 0) return 0.0;
        return Math.min(1.0, (double) best / shorter);
    }
}
