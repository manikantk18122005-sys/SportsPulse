package com.sportspulse.algo.rand;

import java.util.*;

/**
 * Algorithm R (reservoir sampling): maintains an unbiased random sample of size k
 * from a stream of unknown/unbounded length in a single pass, O(n) time O(k) space.
 * Used to sample match events (e.g. ball-tracking pings) for spot-checking without
 * buffering the entire stream.
 */
public final class ReservoirSampling {
    private final Random rng = new Random();
    private final int k;
    private final List<Object> reservoir = new ArrayList<>();
    private long seen = 0;

    public ReservoirSampling(int k) { this.k = k; }

    public void offer(Object item) {
        seen++;
        if (reservoir.size() < k) {
            reservoir.add(item);
        } else {
            int j = rng.nextInt((int) seen); // 0..seen-1
            if (j < k) reservoir.set(j, item);
        }
    }

    public List<Object> getSample() { return reservoir; }
    public long getSeenCount() { return seen; }

    /** Convenience one-shot sampler over a finite iterable stream. */
    public static <T> List<T> sample(Iterable<T> stream, int k) {
        ReservoirSampling rs = new ReservoirSampling(k);
        for (T item : stream) rs.offer(item);
        @SuppressWarnings("unchecked")
        List<T> result = (List<T>) (List<?>) rs.getSample();
        return result;
    }
}
