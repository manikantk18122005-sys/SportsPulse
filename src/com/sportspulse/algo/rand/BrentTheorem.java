package com.sportspulse.algo.rand;

/**
 * Brent's theorem: given the total WORK (W = total operations across all
 * processors) and DEPTH (D = length of the longest dependency chain, i.e. the
 * critical path) of a parallel algorithm, running it on p processors takes
 * time T_p bounded by:  T_p <= W/p + D
 * This class estimates achievable runtime / speedup / efficiency for a given
 * workload+depth on p processors -- used to decide, e.g., how many threads are
 * worth allocating to a parallel aggregation job before diminishing returns.
 */
public final class BrentTheorem {
    private BrentTheorem() {}

    public static class Estimate {
        public final long work, depth;
        public final int processors;
        public final double estimatedTime;
        public final double speedupVsSequential;
        public final double efficiency; // speedup / processors, 0..1

        Estimate(long work, long depth, int processors) {
            this.work = work;
            this.depth = depth;
            this.processors = processors;
            this.estimatedTime = (double) work / processors + depth;
            this.speedupVsSequential = work / estimatedTime;
            this.efficiency = speedupVsSequential / processors;
        }
    }

    public static Estimate estimate(long work, long depth, int processors) {
        return new Estimate(work, depth, processors);
    }

    /** Sweeps processor counts 1..maxProcessors to show the speedup/efficiency curve. */
    public static Estimate[] sweep(long work, long depth, int maxProcessors) {
        Estimate[] results = new Estimate[maxProcessors];
        for (int p = 1; p <= maxProcessors; p++) results[p - 1] = estimate(work, depth, p);
        return results;
    }
}
