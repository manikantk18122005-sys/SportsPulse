package com.sportspulse.hashing;

import com.sportspulse.auth.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Hand-rolled Hash Table implementing Separate Chaining with Universal Hashing.
 *
 * =========================================================================
 * ARCHITECTURAL REQUIREMENTS (ACADEMIC TIER 1):
 * =========================================================================
 * - ZERO ready-made Java collections (no java.util.HashMap, Hashtable, Map).
 * - Primitive array of HashEntry buckets (HashEntry[] table).
 * - Collision resolution via custom singly-linked chains (HashEntry.next).
 * - Bucket indexing governed by UniversalHashFunction:
 *       h(k) = ((a * k + b) mod p) mod m
 * - Collision counter tracking every time a key lands in an already-occupied bucket.
 * - Dynamic resizing when load factor alpha = n / m exceeds the threshold.
 * =========================================================================
 */
public class CustomHashTable {

    /** Default initial capacity: prime number 17. */
    public static final int DEFAULT_CAPACITY = 17;
    public static final double DEFAULT_MAX_LOAD_FACTOR = 0.75;

    private HashEntry[] table;
    private int capacity;
    private int size;
    private long collisionCount;
    private UniversalHashFunction hashFunction;
    private final double maxLoadFactor;

    public CustomHashTable() {
        this(DEFAULT_CAPACITY, DEFAULT_MAX_LOAD_FACTOR);
    }

    public CustomHashTable(int initialCapacity) {
        this(initialCapacity, DEFAULT_MAX_LOAD_FACTOR);
    }

    public CustomHashTable(int initialCapacity, double maxLoadFactor) {
        if (initialCapacity <= 0) initialCapacity = DEFAULT_CAPACITY;
        this.capacity = initialCapacity;
        this.maxLoadFactor = maxLoadFactor;
        this.table = new HashEntry[this.capacity];
        this.size = 0;
        this.collisionCount = 0;
        this.hashFunction = new UniversalHashFunction(this.capacity);
    }

    /**
     * Constructs a hash table with an explicit universal hash function (for testing).
     */
    public CustomHashTable(int initialCapacity, UniversalHashFunction hashFunction) {
        this.capacity = initialCapacity;
        this.maxLoadFactor = DEFAULT_MAX_LOAD_FACTOR;
        this.table = new HashEntry[this.capacity];
        this.size = 0;
        this.collisionCount = 0;
        this.hashFunction = hashFunction;
        this.hashFunction.setM(initialCapacity);
    }

    /**
     * Inserts a user credential record into the hash table.
     * If the username already exists, updates the user object.
     * If inserting a new key into an occupied bucket, increments collisionCount.
     *
     * @param username The key.
     * @param user     The credential record value.
     * @return true if a new key was inserted, false if an existing key was updated.
     */
    public synchronized boolean insert(String username, User user) {
        if (username == null || user == null) {
            throw new IllegalArgumentException("Username and User cannot be null");
        }

        // Automatic resizing check
        if ((double) (size + 1) / capacity > maxLoadFactor) {
            resize(nextPrime(capacity * 2));
        }

        int index = hashFunction.hash(username);
        HashEntry head = table[index];

        // 1. Search for existing key in bucket chain
        HashEntry curr = head;
        while (curr != null) {
            if (curr.username.equals(username)) {
                curr.user = user; // Update existing
                return false;
            }
            curr = curr.next;
        }

        // 2. Collision detection: If bucket already has elements, this is a collision
        if (head != null) {
            collisionCount++;
        }

        // 3. Prepend new entry to bucket chain
        HashEntry newEntry = new HashEntry(username, user, head);
        table[index] = newEntry;
        size++;
        return true;
    }

    /**
     * Searches for a user record by username in O(1) expected time.
     */
    public synchronized User search(String username) {
        if (username == null) return null;
        int index = hashFunction.hash(username);
        HashEntry curr = table[index];
        while (curr != null) {
            if (curr.username.equals(username)) {
                return curr.user;
            }
            curr = curr.next;
        }
        return null;
    }

    /**
     * Checks if a username exists in the hash table.
     */
    public synchronized boolean contains(String username) {
        return search(username) != null;
    }

    /**
     * Removes a user record by username.
     * @return true if found and removed, false otherwise.
     */
    public synchronized boolean remove(String username) {
        if (username == null) return false;
        int index = hashFunction.hash(username);
        HashEntry curr = table[index];
        HashEntry prev = null;

        while (curr != null) {
            if (curr.username.equals(username)) {
                if (prev == null) {
                    table[index] = curr.next;
                } else {
                    prev.next = curr.next;
                }
                size--;
                return true;
            }
            prev = curr;
            curr = curr.next;
        }
        return false;
    }

    /**
     * Resizes the internal table array and rehashes all elements.
     * Preserves universal hashing by updating m on the hash function.
     */
    public synchronized void resize(int newCapacity) {
        if (newCapacity <= size) newCapacity = nextPrime(size * 2 + 1);
        HashEntry[] oldTable = this.table;
        int oldCapacity = this.capacity;

        this.capacity = newCapacity;
        this.table = new HashEntry[newCapacity];
        this.hashFunction = this.hashFunction.withCapacity(newCapacity);

        // Rehash all entries into new buckets
        int rehashedCount = 0;
        long newCollisions = 0;

        for (int i = 0; i < oldCapacity; i++) {
            HashEntry curr = oldTable[i];
            while (curr != null) {
                HashEntry next = curr.next;
                int newIndex = hashFunction.hash(curr.username);

                if (table[newIndex] != null) {
                    newCollisions++;
                }

                curr.next = table[newIndex];
                table[newIndex] = curr;
                rehashedCount++;

                curr = next;
            }
        }
        this.size = rehashedCount;
        this.collisionCount = newCollisions;
    }

    /**
     * Rehashes existing entries with freshly randomized universal hash parameters (a, b).
     * Used for interactive viva demonstrations to observe how keys redistribute.
     */
    public synchronized void rehashWithNewParameters() {
        this.hashFunction.regenerateParameters();
        HashEntry[] oldTable = this.table;
        this.table = new HashEntry[this.capacity];

        long newCollisions = 0;
        int rehashedCount = 0;

        for (int i = 0; i < capacity; i++) {
            HashEntry curr = oldTable[i];
            while (curr != null) {
                HashEntry next = curr.next;
                int newIndex = hashFunction.hash(curr.username);

                if (table[newIndex] != null) {
                    newCollisions++;
                }

                curr.next = table[newIndex];
                table[newIndex] = curr;
                rehashedCount++;

                curr = next;
            }
        }
        this.size = rehashedCount;
        this.collisionCount = newCollisions;
    }

    // =========================================================================
    // DIAGNOSTIC & STATISTICAL METRICS (For Admin Console & Viva)
    // =========================================================================

    public int getCapacity() { return capacity; }
    public int getSize() { return size; }
    public long getCollisionCount() { return collisionCount; }
    public double getLoadFactor() { return (double) size / capacity; }
    public UniversalHashFunction getHashFunction() { return hashFunction; }

    public int getNonEmptyBucketCount() {
        int count = 0;
        for (int i = 0; i < capacity; i++) {
            if (table[i] != null) count++;
        }
        return count;
    }

    public int getEmptyBucketCount() {
        return capacity - getNonEmptyBucketCount();
    }

    public int getMaxChainLength() {
        int maxLen = 0;
        for (int i = 0; i < capacity; i++) {
            int len = 0;
            HashEntry curr = table[i];
            while (curr != null) {
                len++;
                curr = curr.next;
            }
            if (len > maxLen) maxLen = len;
        }
        return maxLen;
    }

    public double getAverageChainLength() {
        int nonEmpty = getNonEmptyBucketCount();
        if (nonEmpty == 0) return 0.0;
        return (double) size / nonEmpty;
    }

    /**
     * Returns an array of chain lengths for all m buckets.
     */
    public int[] getBucketDistribution() {
        int[] dist = new int[capacity];
        for (int i = 0; i < capacity; i++) {
            int len = 0;
            HashEntry curr = table[i];
            while (curr != null) {
                len++;
                curr = curr.next;
            }
            dist[i] = len;
        }
        return dist;
    }

    /**
     * Returns the usernames residing in a given bucket index.
     */
    public List<String> getBucketUsernames(int bucketIndex) {
        List<String> names = new ArrayList<>();
        if (bucketIndex < 0 || bucketIndex >= capacity) return names;
        HashEntry curr = table[bucketIndex];
        while (curr != null) {
            names.add(curr.username);
            curr = curr.next;
        }
        return names;
    }

    /**
     * Returns all stored User records (for administrative display only).
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            HashEntry curr = table[i];
            while (curr != null) {
                users.add(curr.user);
                curr = curr.next;
            }
        }
        return users;
    }

    /** Helper to find the next prime number >= n. */
    private static int nextPrime(int n) {
        if (n <= 2) return 2;
        int candidate = (n % 2 == 0) ? n + 1 : n;
        while (!isPrime(candidate)) {
            candidate += 2;
        }
        return candidate;
    }

    private static boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
        }
        return true;
    }
}
