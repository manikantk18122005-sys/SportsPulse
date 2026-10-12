package com.sportspulse.admin;

import com.sportspulse.auth.AuthenticationService;
import com.sportspulse.auth.User;
import com.sportspulse.hashing.CustomHashTable;
import com.sportspulse.hashing.UniversalHashFunction;
import com.sportspulse.util.Ansi;
import com.sportspulse.util.Console;
import com.sportspulse.util.TerminalChart;

import java.util.List;
import java.util.Scanner;

/**
 * Administrative diagnostics console for inspecting the hand-rolled CustomHashTable
 * and Carter-Wegman Universal Hash Function internals.
 */
public final class HashTableDiagnostics {

    private HashTableDiagnostics() {}

    public static void run(AuthenticationService authService, Scanner sc) {
        CustomHashTable table = authService.getCredentialTable();
        boolean back = false;

        while (!back) {
            Console.header("HASH TABLE INTERNALS & UNIVERSAL HASH DIAGNOSTICS");
            printSummary(table);

            Console.section("Diagnostic Actions");
            Console.bullet("1. Print Full Bucket Distribution");
            Console.bullet("2. Insert Demonstration Users  (Trigger & Observe Collisions)");
            Console.bullet("3. Regenerate Universal Hash Parameters  (Observe Dynamic Key Redistribution)");
            Console.bullet("4. Trace Username Hash Computation  (Inspect k, a*k+b, and bucket index)");
            Console.bullet("0. Back to Admin Lab");
            Console.prompt("\nSelect an action: ");

            if (!sc.hasNextLine()) {
                back = true;
                break;
            }
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> printDistribution(table);
                case "2" -> insertDemoUsers(authService);
                case "3" -> regenerateParameters(table);
                case "4" -> traceLookup(table, sc);
                case "0" -> back = true;
                default -> Console.error("Invalid action.");
            }
            if (!back) Console.pause(sc);
        }
    }

    public static void printSummary(CustomHashTable table) {
        UniversalHashFunction hf = table.getHashFunction();

        System.out.println(Ansi.paint(Ansi.BOLD + Ansi.BRIGHT_WHITE, "  [Hash Table Metrics]"));
        Console.kv("Capacity (m)", table.getCapacity());
        Console.kv("Entries (n)", table.getSize());
        Console.kv("Load Factor (alpha = n/m)", String.format("%.4f", table.getLoadFactor()));
        Console.kv("Total Recorded Collisions", table.getCollisionCount());
        Console.kv("Non-empty Buckets", table.getNonEmptyBucketCount());
        Console.kv("Empty Buckets", table.getEmptyBucketCount());
        Console.kv("Maximum Chain Length", table.getMaxChainLength());
        Console.kv("Average Chain Length", String.format("%.2f", table.getAverageChainLength()));

        System.out.println();
        System.out.println(Ansi.paint(Ansi.BOLD + Ansi.BRIGHT_WHITE, "  [Universal Hash Function Parameters]"));
        Console.kv("Prime Modulus (p)", hf.getP());
        Console.kv("Table Capacity (m)", hf.getM());
        Console.kv("Parameter a (Multiplicative)", hf.getA());
        Console.kv("Parameter b (Additive Shift)", hf.getB());
        Console.kv("Formula", hf.getFormula());
    }

    private static void printDistribution(CustomHashTable table) {
        Console.section("Bucket Distribution & Separate Chaining Inspection");
        int capacity = table.getCapacity();
        int[] dist = table.getBucketDistribution();

        TerminalChart.renderHashDistribution(dist, capacity);

        for (int i = 0; i < capacity; i++) {
            List<String> users = table.getBucketUsernames(i);
            int count = dist[i];
            String bucketLabel = String.format("Bucket %2d", i);

            if (count == 0) {
                System.out.printf("  %s : 0 entries%n", Ansi.paint(Ansi.DIM, bucketLabel));
            } else if (count == 1) {
                System.out.printf("  %s : 1 entry   -> [%s]%n",
                        Ansi.paint(Ansi.BRIGHT_CYAN, bucketLabel),
                        Ansi.paint(Ansi.BRIGHT_WHITE, users.get(0)));
            } else {
                // Collision case!
                StringBuilder chain = new StringBuilder();
                for (int u = 0; u < users.size(); u++) {
                    chain.append(users.get(u));
                    if (u < users.size() - 1) chain.append(" -> ");
                }
                System.out.printf("  %s : %d entries -> [%s] %s%n",
                        Ansi.paint(Ansi.BRIGHT_YELLOW + Ansi.BOLD, bucketLabel),
                        count,
                        Ansi.paint(Ansi.BRIGHT_YELLOW, chain.toString()),
                        Ansi.paint(Ansi.BRIGHT_RED + Ansi.BOLD, "<-- COLLISION DETECTED"));
            }
        }
    }

    private static void insertDemoUsers(AuthenticationService authService) {
        Console.section("Inserting Demonstration Users to Induce Collisions");
        long beforeCollisions = authService.getCredentialTable().getCollisionCount();
        int added = authService.seedDemonstrationUsers();
        long afterCollisions = authService.getCredentialTable().getCollisionCount();

        Console.success(String.format("Added %d demonstration users.", added));
        Console.kv("Collisions Before", beforeCollisions);
        Console.kv("Collisions After", afterCollisions);
        Console.kv("New Collisions Induced", (afterCollisions - beforeCollisions));
        System.out.println(Ansi.paint(Ansi.BRIGHT_GREEN, "  Separate chaining successfully linked colliding users in the same bucket chains."));
    }

    private static void regenerateParameters(CustomHashTable table) {
        Console.section("Universal Hash Parameter Randomization");
        long oldA = table.getHashFunction().getA();
        long oldB = table.getHashFunction().getB();

        table.rehashWithNewParameters();

        long newA = table.getHashFunction().getA();
        long newB = table.getHashFunction().getB();

        Console.kv("Old Parameters", String.format("a=%d, b=%d", oldA, oldB));
        Console.kv("New Parameters", String.format("a=%d, b=%d", newA, newB));
        Console.kv("New Collision Count", table.getCollisionCount());
        Console.success("Table rehashed successfully with fresh universal parameters!");
    }

    private static void traceLookup(CustomHashTable table, Scanner sc) {
        Console.section("Trace Username Universal Hash Steps");
        Console.prompt("Enter a username to trace: ");
        if (!sc.hasNextLine()) return;
        String uname = sc.nextLine().trim().toLowerCase();

        UniversalHashFunction hf = table.getHashFunction();
        long key = hf.stringToKey(uname);
        long p = hf.getP();
        long a = hf.getA();
        long b = hf.getB();
        int m = hf.getM();

        long linear = ((a * key + b) % p + p) % p;
        int bucketIndex = (int) (linear % m);

        Console.kv("1. Input Username", uname);
        Console.kv("2. Polynomial Key (k)", key);
        Console.kv("3. Linear Term ((a*k + b) mod p)", String.format("((%d * %d + %d) mod %d) = %d", a, key, b, p, linear));
        Console.kv("4. Final Bucket Index (mod m)", String.format("%d mod %d = %d", linear, m, bucketIndex));

        User user = table.search(uname);
        if (user != null) {
            Console.success(String.format("  Match found in Bucket %d: %s [Role: %s]", bucketIndex, user.getUsername(), user.getRole()));
        } else {
            Console.warn(String.format("  Key hashes to Bucket %d, but user '%s' is not registered.", bucketIndex, uname));
        }
    }
}
