package com.sportspulse.algo.rand;

/**
 * Blelloch work-efficient parallel prefix-sum (scan): an up-sweep (reduce) phase
 * followed by a down-sweep phase, O(n) work and O(log n) depth -- suitable for
 * parallelization across cores. Used to compute running totals (e.g. cumulative
 * points, cumulative fouls over match minutes) that could be split across threads.
 * This implementation is sequential but structured exactly as the parallel
 * algorithm so it can be dropped into a fork-join executor unchanged.
 */
public final class BlellochScan {
    private BlellochScan() {}

    /** Returns the EXCLUSIVE prefix sum of input (array is padded to next power of two internally). */
    public static int[] exclusiveScan(int[] input) {
        int n = input.length;
        int size = Integer.highestOneBit(Math.max(1, n - 1)) << 1;
        if (size < n) size <<= 1;
        int[] data = new int[size];
        System.arraycopy(input, 0, data, 0, n);

        // Up-sweep (reduce) phase
        for (int d = 0; d < log2(size); d++) {
            int stride = 1 << (d + 1);
            for (int i = 0; i < size; i += stride) {
                data[i + stride - 1] += data[i + (stride >> 1) - 1];
            }
        }

        data[size - 1] = 0; // set identity at root for exclusive scan

        // Down-sweep phase
        for (int d = log2(size) - 1; d >= 0; d--) {
            int stride = 1 << (d + 1);
            for (int i = 0; i < size; i += stride) {
                int leftChild = i + (stride >> 1) - 1;
                int rightChild = i + stride - 1;
                int t = data[leftChild];
                data[leftChild] = data[rightChild];
                data[rightChild] += t;
            }
        }
        int[] result = new int[n];
        System.arraycopy(data, 0, result, 0, n);
        return result;
    }

    /** Convenience: INCLUSIVE prefix sum derived from the exclusive scan. */
    public static int[] inclusiveScan(int[] input) {
        int[] excl = exclusiveScan(input);
        int[] incl = new int[input.length];
        for (int i = 0; i < input.length; i++) incl[i] = excl[i] + input[i];
        return incl;
    }

    private static int log2(int n) { return Integer.numberOfTrailingZeros(n); }
}
