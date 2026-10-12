package com.sportspulse.hashing;

import java.util.Random;

/**
 * Hand-rolled Universal Hash Function.
 *
 * Implements Carter-Wegman universal hashing:
 *     h(k) = ((a * k + b) mod p) mod m
 *
 * Theoretical Foundations (Module 6: Randomized Algorithms & Hashing):
 * - Let H = { h_{a,b} : a in [1..p-1], b in [0..p-1] }
 * - For any two distinct keys x != y:
 *       Pr_{a,b}[ h(x) == h(y) ] <= 1 / m
 * - The parameters (a, b) are chosen uniformly at random when the table is initialized
 *   or rehashed. This guarantees that no worst-case sequence of inputs chosen by an
 *   adversary can consistently force O(n) degenerate chain collisions.
 *
 * Parameters:
 * - p: A prime modulus strictly larger than the universe of integer keys (p = 1000003).
 * - m: The table capacity (number of buckets).
 * - a: Multiplicative factor chosen uniformly from [1 .. p - 1] (must be non-zero).
 * - b: Additive shift chosen uniformly from [0 .. p - 1].
 */
public class UniversalHashFunction {

    /** Default large prime modulus. 1,000,003 is prime. */
    public static final long DEFAULT_PRIME = 1000003L;

    private final long p;
    private int m;
    private long a;
    private long b;
    private final Random rng;

    /**
     * Constructs a universal hash function with randomly selected parameters a and b.
     * @param m Table capacity (number of buckets).
     */
    public UniversalHashFunction(int m) {
        this(m, DEFAULT_PRIME, new Random());
    }

    public UniversalHashFunction(int m, long p, Random rng) {
        if (m <= 0) throw new IllegalArgumentException("Table capacity m must be positive: " + m);
        if (p <= 1) throw new IllegalArgumentException("Prime modulus p must be > 1: " + p);
        this.m = m;
        this.p = p;
        this.rng = rng;
        regenerateParameters();
    }

    /**
     * Constructs a universal hash function with explicit parameters (for testing/reproducibility).
     */
    public UniversalHashFunction(int m, long p, long a, long b) {
        if (m <= 0) throw new IllegalArgumentException("Table capacity m must be positive: " + m);
        if (p <= 1) throw new IllegalArgumentException("Prime modulus p must be > 1: " + p);
        if (a <= 0 || a >= p) throw new IllegalArgumentException("Parameter a must be in [1, p-1]: " + a);
        if (b < 0 || b >= p) throw new IllegalArgumentException("Parameter b must be in [0, p-1]: " + b);
        this.m = m;
        this.p = p;
        this.a = a;
        this.b = b;
        this.rng = new Random();
    }

    /**
     * Converts a string username into an integer key k in [0, p-1]
     * using a polynomial rolling hash with radix 31 modulo p.
     *
     * k = sum(s[i] * 31^(len-1-i)) mod p
     */
    public long stringToKey(String s) {
        if (s == null || s.isEmpty()) return 0L;
        long key = 0L;
        for (int i = 0; i < s.length(); i++) {
            key = (key * 31L + (long) s.charAt(i)) % p;
        }
        return Math.abs(key);
    }

    /**
     * Computes the universal hash index for an integer key k:
     *     h(k) = ((a * k + b) mod p) mod m
     *
     * Uses 64-bit long arithmetic to prevent intermediate overflow.
     */
    public int hash(long key) {
        // Ensure non-negative key within [0, p-1]
        long normalizedKey = Math.abs(key) % p;
        long linearTerm = ((a * normalizedKey) % p + b) % p;
        int bucketIndex = (int) (linearTerm % m);
        return (bucketIndex + m) % m; // Ensure non-negative index
    }

    /**
     * Converts a string username to an integer key k, then computes the bucket index.
     */
    public int hash(String username) {
        return hash(stringToKey(username));
    }

    /**
     * Regenerates random parameters a in [1, p-1] and b in [0, p-1].
     */
    public final void regenerateParameters() {
        // a must be in [1, p - 1] (non-zero)
        long candidateA = 1L + (long) (rng.nextDouble() * (p - 1));
        if (candidateA <= 0 || candidateA >= p) candidateA = 1L;
        this.a = candidateA;

        // b must be in [0, p - 1]
        long candidateB = (long) (rng.nextDouble() * p);
        if (candidateB < 0 || candidateB >= p) candidateB = 0L;
        this.b = candidateB;
    }

    /**
     * Sets explicit a and b values (useful for demonstration or deterministic testing).
     */
    public void setParameters(long a, long b) {
        if (a <= 0 || a >= p) throw new IllegalArgumentException("a must be in [1, p-1]");
        if (b < 0 || b >= p) throw new IllegalArgumentException("b must be in [0, p-1]");
        this.a = a;
        this.b = b;
    }

    /**
     * Creates a new UniversalHashFunction instance adapting to a resized capacity m.
     */
    public UniversalHashFunction withCapacity(int newM) {
        return new UniversalHashFunction(newM, this.p, this.rng);
    }

    public long getA() { return a; }
    public long getB() { return b; }
    public long getP() { return p; }
    public int getM() { return m; }

    public void setM(int newM) {
        if (newM <= 0) throw new IllegalArgumentException("Capacity must be positive");
        this.m = newM;
    }

    public String getFormula() {
        return "h(k) = ((a * k + b) mod p) mod m";
    }

    @Override
    public String toString() {
        return String.format("UniversalHashFunction[p=%d, m=%d, a=%d, b=%d]", p, m, a, b);
    }
}
