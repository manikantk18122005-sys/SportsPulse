package com.sportspulse.algo.rand;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Randomized QuickSort: pivot chosen uniformly at random to give expected
 * O(n log n) regardless of input order (avoids the O(n^2) worst case on
 * already-sorted leaderboard data). Used for ranking players by multi-attribute
 * performance criteria (comparator supplied by caller).
 */
public final class RandomizedQuickSort {
    private static final Random RNG = new Random();
    private RandomizedQuickSort() {}

    public static <T> void sort(List<T> list, Comparator<T> comparator) {
        quicksort(list, 0, list.size() - 1, comparator);
    }

    private static <T> void quicksort(List<T> list, int lo, int hi, Comparator<T> cmp) {
        if (lo >= hi) return;
        int pivotIndex = lo + RNG.nextInt(hi - lo + 1);
        swap(list, pivotIndex, hi);
        T pivot = list.get(hi);
        int store = lo;
        for (int i = lo; i < hi; i++) {
            if (cmp.compare(list.get(i), pivot) < 0) {
                swap(list, i, store);
                store++;
            }
        }
        swap(list, store, hi);
        quicksort(list, lo, store - 1, cmp);
        quicksort(list, store + 1, hi, cmp);
    }

    private static <T> void swap(List<T> list, int i, int j) {
        T tmp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, tmp);
    }
}
