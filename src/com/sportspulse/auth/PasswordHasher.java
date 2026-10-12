package com.sportspulse.auth;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

/**
 * Defensive Password Security Engine.
 *
 * =========================================================================
 * ACADEMIC & ARCHITECTURAL FOUNDATION (VIVA DEFENSE):
 * =========================================================================
 * 1. PASSWORD KDF vs UNIVERSAL HASHING:
 *    - Password KDF (PBKDF2WithHmacSHA256):
 *      Slow, computationally expensive, memory/CPU iterated key derivation.
 *      Specifically designed to thwart GPU/ASIC offline dictionary and brute-force
 *      attacks by forcing 65,536 iterations of HMAC-SHA256 per candidate password.
 *    - Universal Hashing (Carter-Wegman family h(k) = ((a*k + b) mod p) mod m):
 *      Ultra-fast O(1) mathematical mapping designed for hash table bucket indexing.
 *      Has NO cryptographic pre-image resistance and must NEVER be used to store passwords.
 *
 * 2. UNIQUE CRYPTOGRAPHIC SALTING:
 *    - A unique 16-byte (128-bit) salt is generated for every user via java.security.SecureRandom.
 *    - Neutralizes Rainbow Table attacks and prevents identical passwords from producing
 *      identical stored hash values across different accounts.
 *
 * 3. CONSTANT-TIME VERIFICATION (TIMING ATTACK RESISTANCE):
 *    - Compares byte arrays using MessageDigest.isEqual(), which executes in constant time.
 *    - Standard String.equals() short-circuits on the first mismatched character, leaking
 *      timing information that allows an adversary to deduce hash prefixes.
 *
 * 4. USERNAME ENUMERATION MITIGATION (DUMMY TIMING WORK):
 *    - When an unknown username is submitted, performDummyWork() executes an identical
 *      PBKDF2 derivation against a fixed dummy salt/password.
 *    - Ensures that the server response latency for non-existent users is indistinguishable
 *      from existing users.
 * =========================================================================
 */
public final class PasswordHasher {

    /** NIST-recommended Password-Based Key Derivation Function. */
    public static final String PASSWORD_KDF = "PBKDF2WithHmacSHA256";

    /** Cryptographic salt size: 16 bytes (128 bits of entropy). */
    public static final int SALT_BYTES = 16;

    /** Number of PBKDF2 iterations to ensure sufficient computational work factor. */
    public static final int ITERATIONS = 65536;

    /** Length of derived key in bits: 256 bits (32 bytes). */
    public static final int KEY_LENGTH_BITS = 256;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // Pre-allocated static parameters for dummy work timing equalization
    private static final byte[] DUMMY_SALT = new byte[SALT_BYTES];
    private static final char[] DUMMY_PASSWORD = "DummyTimingEqualizationPassword#2026".toCharArray();

    static {
        SECURE_RANDOM.nextBytes(DUMMY_SALT);
    }

    private PasswordHasher() {}

    /**
     * Generates a unique 16-byte random salt and returns it as a 32-character hexadecimal string.
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        return bytesToHex(salt);
    }

    /**
     * Computes the PBKDF2WithHmacSHA256 derived key hash for a given password and salt.
     *
     * @param password Plaintext password.
     * @param saltHex  Hexadecimal representation of the unique salt.
     * @return Hexadecimal string of the 256-bit derived key.
     */
    public static String hashPassword(String password, String saltHex) {
        if (password == null) password = "";
        byte[] salt = hexToBytes(saltHex);
        char[] passwordChars = password.toCharArray();

        try {
            byte[] derived = deriveKey(passwordChars, salt, ITERATIONS, KEY_LENGTH_BITS);
            return bytesToHex(derived);
        } finally {
            // Defensive memory hygiene: zero out sensitive plaintext characters in memory
            Arrays.fill(passwordChars, '\0');
        }
    }

    /**
     * Verifies a candidate password against the stored salt and hash using CONSTANT-TIME comparison.
     *
     * @param candidatePassword Plaintext password entered by user.
     * @param storedSaltHex     Stored unique user salt in hex.
     * @param storedHashHex     Stored PBKDF2 derived hash in hex.
     * @return true if candidate matches, false otherwise.
     */
    public static boolean verify(String candidatePassword, String storedSaltHex, String storedHashHex) {
        if (candidatePassword == null || storedSaltHex == null || storedHashHex == null) {
            return false;
        }

        byte[] expectedHashBytes = hexToBytes(storedHashHex);
        if (expectedHashBytes.length == 0) return false;

        byte[] salt = hexToBytes(storedSaltHex);
        char[] candidateChars = candidatePassword.toCharArray();

        try {
            byte[] candidateHashBytes = deriveKey(candidateChars, salt, ITERATIONS, KEY_LENGTH_BITS);
            // CRITICAL DEFENSE: MessageDigest.isEqual performs constant-time byte comparison
            return MessageDigest.isEqual(candidateHashBytes, expectedHashBytes);
        } finally {
            Arrays.fill(candidateChars, '\0');
        }
    }

    /**
     * Executes identical PBKDF2 computational work on a dummy salt/password.
     * Neutralizes observable timing differences between existing and non-existent usernames.
     */
    public static void performDummyWork() {
        try {
            deriveKey(DUMMY_PASSWORD, DUMMY_SALT, ITERATIONS, KEY_LENGTH_BITS);
        } catch (Exception ignored) {
            // Fallback: silent absorption
        }
    }

    /**
     * Low-level PBKDF2 derivation helper.
     */
    private static byte[] deriveKey(char[] password, byte[] salt, int iterations, int keyLengthBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(PASSWORD_KDF);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Cryptographic provider failure for " + PASSWORD_KDF, e);
        }
    }

    // =========================================================================
    // HEXADECIMAL ENCODING / DECODING UTILITIES (Zero External Dependencies)
    // =========================================================================

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static byte[] hexToBytes(String hex) {
        if (hex == null || hex.isEmpty() || (hex.length() % 2 != 0)) {
            return new byte[0];
        }
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int high = Character.digit(hex.charAt(i), 16);
            int low = Character.digit(hex.charAt(i + 1), 16);
            if (high == -1 || low == -1) return new byte[0];
            data[i / 2] = (byte) ((high << 4) + low);
        }
        return data;
    }
}
