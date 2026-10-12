package com.sportspulse.algo.rand;

import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;
import java.util.function.ToDoubleFunction;

/**
 * Multi-threaded reduction using Java's Fork/Join framework: recursively splits
 * the input in half, computes each half's aggregate on separate tasks/threads,
 * and combines results. Used for aggregating match metrics across a big event
 * list (e.g. total passes, total distance covered) faster than a single-threaded
 * loop on multi-core hardware.
 */
public final class ParallelReduce {
    private static final int THRESHOLD = 1000; // below this, just sum sequentially
    private ParallelReduce() {}

    private static class SumTask<T> extends RecursiveTask<Double> {
        private static final long serialVersionUID = 1L;
        private final transient List<T> data;
        private final int lo, hi;
        private final transient ToDoubleFunction<T> extractor;

        SumTask(List<T> data, int lo, int hi, ToDoubleFunction<T> extractor) {
            this.data = data; this.lo = lo; this.hi = hi; this.extractor = extractor;
        }

        @Override
        protected Double compute() {
            if (hi - lo <= THRESHOLD) {
                double sum = 0;
                for (int i = lo; i < hi; i++) sum += extractor.applyAsDouble(data.get(i));
                return sum;
            }
            int mid = (lo + hi) >>> 1;
            SumTask<T> left = new SumTask<>(data, lo, mid, extractor);
            SumTask<T> right = new SumTask<>(data, mid, hi, extractor);
            left.fork();
            double rightResult = right.compute();
            double leftResult = left.join();
            return leftResult + rightResult;
        }
    }

    public static <T> double sum(List<T> data, ToDoubleFunction<T> extractor) {
        if (data.isEmpty()) return 0;
        ForkJoinPool pool = ForkJoinPool.commonPool();
        return pool.invoke(new SumTask<>(data, 0, data.size(), extractor));
    }
}
