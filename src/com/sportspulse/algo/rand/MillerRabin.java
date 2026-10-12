package com.sportspulse.algo.rand;

import java.math.BigInteger;
import java.util.Random;

/**
 * Miller-Rabin probabilistic primality test, O(k log^3 n) for k rounds.
 * Used for: generating secure prime-based IDs / hash-table sizing parameters
 * for player/match transaction records, and quick cryptographic-style checks.
 */
public final class MillerRabin {
    private static final Random RNG = new Random();
    private MillerRabin() {}

    public static boolean isProbablePrime(long n, int rounds) {
        if (n < 2) return false;
        for (long p : new long[]{2, 3, 5, 7, 11, 13, 17, 19, 23}) {
            if (n == p) return true;
            if (n % p == 0) return false;
        }
        long d = n - 1;
        int r = 0;
        while (d % 2 == 0) { d /= 2; r++; }

        for (int i = 0; i < rounds; i++) {
            long a = 2 + (long) (RNG.nextDouble() * (n - 4));
            long x = modPow(a, d, n);
            if (x == 1 || x == n - 1) continue;
            boolean composite = true;
            for (int j = 0; j < r - 1; j++) {
                x = mulMod(x, x, n);
                if (x == n - 1) { composite = false; break; }
            }
            if (composite) return false;
        }
        return true;
    }

    /** Finds the next probable prime >= start, useful for secure ID / table-size generation. */
    public static long nextPrime(long start) {
        long candidate = Math.max(2, start);
        while (!isProbablePrime(candidate, 20)) candidate++;
        return candidate;
    }

    /** Generates a random secure-looking prime-based transaction ID of roughly `digits` length. */
    public static long generateSecureId(int digits) {
        long low = (long) Math.pow(10, digits - 1);
        long high = (long) Math.pow(10, digits) - 1;
        long candidate = low + (long) (RNG.nextDouble() * (high - low));
        return nextPrime(candidate);
    }

    private static long mulMod(long a, long b, long mod) {
        return BigInteger.valueOf(a).multiply(BigInteger.valueOf(b)).mod(BigInteger.valueOf(mod)).longValue();
    }

    private static long modPow(long base, long exp, long mod) {
        return BigInteger.valueOf(base).modPow(BigInteger.valueOf(exp), BigInteger.valueOf(mod)).longValue();
    }
}
