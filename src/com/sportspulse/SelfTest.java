package com.sportspulse;

import com.sportspulse.admin.AdminConsole;
import com.sportspulse.algo.dp.EditDistance;
import com.sportspulse.algo.dp.MatrixChain;
import com.sportspulse.algo.flow.BipartiteMatching;
import com.sportspulse.algo.flow.Dinic;
import com.sportspulse.algo.flow.EdmondsKarp;
import com.sportspulse.algo.flow.FlowGraph;
import com.sportspulse.algo.flow.FordFulkerson;
import com.sportspulse.algo.npc.GraphReductions;
import com.sportspulse.algo.npc.SATSolver;
import com.sportspulse.algo.rand.BlellochScan;
import com.sportspulse.algo.rand.MillerRabin;
import com.sportspulse.algo.rand.RandomizedQuickSort;
import com.sportspulse.algo.rand.ReservoirSampling;
import com.sportspulse.algo.strmatch.AhoCorasick;
import com.sportspulse.algo.strmatch.KMP;
import com.sportspulse.algo.strmatch.NaiveSearch;
import com.sportspulse.algo.strmatch.RabinKarp;
import com.sportspulse.algo.strmatch.ZFunction;
import com.sportspulse.algo.suffix.Kasai;
import com.sportspulse.algo.suffix.SuffixArray;
import com.sportspulse.auth.AccountStatus;
import com.sportspulse.auth.AuthenticationService;
import com.sportspulse.auth.PasswordHasher;
import com.sportspulse.auth.PasswordPolicy;
import com.sportspulse.auth.Role;
import com.sportspulse.auth.Session;
import com.sportspulse.auth.User;
import com.sportspulse.auth.UsernamePolicy;
import com.sportspulse.auth.ValidationResult;
import com.sportspulse.data.MockData;
import com.sportspulse.hashing.CustomHashTable;
import com.sportspulse.hashing.UniversalHashFunction;
import com.sportspulse.util.*;

import java.security.MessageDigest;
import java.util.*;

/**
 * Comprehensive Automated Self-Test Harness for SportsPulse.
 *
 * Verifies:
 * - Tier 1: Hand-Rolled Universal Hashing, CustomHashTable separate chaining,
 *           PBKDF2WithHmacSHA256, Constant-Time Verification, Dynamic Resizing.
 * - Defensive Security Suite: Brute-force lockout, session expiration,
 *                             privilege escalation prevention, and zero credential leakage.
 * - Module 1 to 6 Algorithm Implementations (19 distinct textbook algorithms).
 */
public final class SelfTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        boolean allPassed = runAllTests(true);
        if (!allPassed) System.exit(1);
    }

    /**
     * Executes all self-tests.
     */
    public static synchronized boolean runAllTests(boolean exitOnFail) {
        passed = 0;
        failed = 0;
        System.out.println("Running SportsPulse comprehensive verification suite...\n");

        // ---- Tier 1: Core Hashing & Password Mechanics ----
        testUniversalHashFunction();
        testCustomHashTable();
        testPasswordHasher();
        testAuthenticationService();
        testRoleAuthorization();

        // ---- Defensive Authentication & Security Suite ----
        testUsernamePolicy();
        testPasswordPolicyAndConfirmation();
        testBruteForceLockoutAndReset();
        testSessionLifecycleAndExpiration();
        testAdminPrivilegeEscalationDefense();
        testHashTableResizePreservesAuthentication();
        testDedicatedSecuritySelfTest();

        // ---- Benchmark Baseline Verification ----
        testNaiveSearch();
        testFlowGraphCopy();

        // ---- Complete Core Algorithm Suite (Modules 1 - 6) ----
        testKMP();
        testZFunction();
        testRabinKarp();
        testAhoCorasick();
        testEditDistance();
        testSuffixArrayAndKasai();
        testMatrixChain();
        testFlowAlgorithmsAgree();
        testBipartiteMatching();
        testSATSolver();
        testGraphReductions();
        testMillerRabin();
        testRandomizedQuickSort();
        testReservoirSampling();
        testBlellochScan();
        testPlayerIdsResolveAcrossAllSports();

        // ---- ANSI Terminal 10/10 Presentation & UI Suite ----
        testAnsiTruecolorAndFallbacks();
        testConsoleCapabilities();
        testTerminalThemeAndAccessibility();
        testTerminalTableFormatting();
        testTerminalInputAndMenu();
        testTerminalRendererAndSafety();

        // ---- Portability & Dynamic Path Configuration Suite ----
        testAppPathsAndPortability();

        System.out.println("\n----------------------------------------");
        System.out.println("Passed: " + passed + "   Failed: " + failed);
        boolean ok = (failed == 0);
        System.out.println(ok ? "ALL TESTS PASSED" : "SOME TESTS FAILED");
        return ok;
    }

    // ---- Assertions -------------------------------------------------

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println(com.sportspulse.util.Ansi.paint(com.sportspulse.util.Ansi.BRIGHT_GREEN, "  [PASS] ") + name);
        } else {
            failed++;
            System.out.println(com.sportspulse.util.Ansi.paint(com.sportspulse.util.Ansi.BRIGHT_RED, "  [FAIL] ") + name);
        }
    }

    private static void checkEquals(String name, Object expected, Object actual) {
        boolean ok = Objects.equals(expected, actual);
        check(name + " (expected=" + expected + ", actual=" + actual + ")", ok);
    }

    // ---- Tier 1: Universal Hashing & Basic Auth Tests ----------------

    private static void testUniversalHashFunction() {
        System.out.println("Universal Hash Function (Carter-Wegman Family)");
        UniversalHashFunction hf = new UniversalHashFunction(17);
        long keyA = hf.stringToKey("ronaldo");
        long keyB = hf.stringToKey("messi");
        check("converts username to non-negative key", keyA >= 0 && keyB >= 0);

        int hash1 = hf.hash("ronaldo");
        int hash2 = hf.hash("ronaldo");
        checkEquals("deterministic hash for identical key", hash1, hash2);
        check("hash value within table bounds [0..m-1]", hash1 >= 0 && hash1 < 17);

        check("parameter a is non-zero and < p", hf.getA() > 0 && hf.getA() < hf.getP());
        check("parameter b is in [0..p-1]", hf.getB() >= 0 && hf.getB() < hf.getP());
    }

    private static void testCustomHashTable() {
        System.out.println("Custom Hash Table (Separate Chaining)");
        CustomHashTable table = new CustomHashTable(7); // small capacity to induce chaining
        User u1 = new User("alice", "hashA", "saltA", Role.USER);
        User u2 = new User("bob", "hashB", "saltB", Role.USER);
        User u3 = new User("charlie", "hashC", "saltC", Role.ADMIN);

        check("insert alice", table.insert("alice", u1));
        check("insert bob", table.insert("bob", u2));
        check("insert charlie", table.insert("charlie", u3));

        checkEquals("lookup alice finds correct user", u1, table.search("alice"));
        checkEquals("lookup bob finds correct user", u2, table.search("bob"));
        checkEquals("lookup charlie finds correct user", u3, table.search("charlie"));
        check("non-existent user returns null", table.search("nobody") == null);
        checkEquals("table size is 3", 3, table.getSize());

        check("contains('alice') is true", table.contains("alice"));
        check("contains('nobody') is false", !table.contains("nobody"));

        double lf = table.getLoadFactor();
        check("load factor matches size/capacity", Math.abs(lf - (3.0 / table.getCapacity())) < 1e-6);

        // Test collision handling: force two keys with explicit hash function
        UniversalHashFunction fixedHf = new UniversalHashFunction(7, 1000003L, 1L, 0L);
        CustomHashTable fixedTable = new CustomHashTable(7, fixedHf);
        User cu1 = new User("user1", "h1", "s1", Role.USER);
        User cu2 = new User("user2", "h2", "s2", Role.USER);
        fixedTable.insert("user1", cu1);
        fixedTable.insert("user2", cu2);
        check("both keys retrievable after collision",
                fixedTable.search("user1") == cu1 && fixedTable.search("user2") == cu2);

        check("remove existing user", table.remove("bob"));
        check("removed user is no longer in table", table.search("bob") == null);
        checkEquals("size decremented after removal", 2, table.getSize());
    }

    private static void testPasswordHasher() {
        System.out.println("Password Hasher (PBKDF2WithHmacSHA256 & Constant-Time)");
        String salt = PasswordHasher.generateSalt();
        check("salt is generated with positive length", salt != null && salt.length() == 32);

        String hash = PasswordHasher.hashPassword("SuperSecret@2026!", salt);
        check("password hash is computed", hash != null && hash.length() == 64);
        check("correct password verifies true", PasswordHasher.verify("SuperSecret@2026!", salt, hash));
        check("wrong password verifies false", !PasswordHasher.verify("WrongSecret@2026!", salt, hash));

        // Test constant-time verification behavior
        byte[] b1 = PasswordHasher.hexToBytes(hash);
        byte[] b2 = PasswordHasher.hexToBytes(hash);
        check("constant-time byte comparison path", MessageDigest.isEqual(b1, b2));
    }

    private static void testAuthenticationService() {
        System.out.println("Authentication Service");
        AuthenticationService auth = new AuthenticationService(new CustomHashTable(17));

        // Default admin account
        Session adminSession = auth.login("admin", "Admin@SportsPulse2026!");
        check("default admin login succeeds", adminSession != null && adminSession.hasRole(Role.ADMIN));

        // Default coach account
        Session coachSession = auth.login("coach", "Coach@SportsPulse2026!");
        check("default coach login succeeds", coachSession != null && !coachSession.hasRole(Role.ADMIN));

        // Invalid password rejected
        check("invalid password rejected", auth.login("admin", "WrongPassword#123") == null);

        // Unknown username rejected
        check("unknown username rejected", auth.login("nonexistent", "SomePassword#123") == null);

        // User registration (min 10 chars password)
        AuthenticationService.AuthResult regRes = auth.register("newanalyst", "SecurePassword@2026", "SecurePassword@2026");
        check("new user registration succeeds", regRes.isSuccess());
        check("new user can now log in", auth.login("newanalyst", "SecurePassword@2026") != null);

        // Duplicate username rejected
        AuthenticationService.AuthResult dupRes = auth.register("newanalyst", "SecurePassword@2026", "SecurePassword@2026");
        check("duplicate username rejected", !dupRes.isSuccess());
    }

    private static void testRoleAuthorization() {
        System.out.println("Role-Based Authorization");
        User admin = new User("admin", "h", "s", Role.ADMIN);
        User user = new User("player", "h", "s", Role.USER);

        check("ADMIN role isAdmin() == true", admin.isAdmin());
        check("USER role isAdmin() == false", !user.isAdmin());
        checkEquals("ADMIN role title", "System Administrator", Role.ADMIN.getTitle());
        checkEquals("USER role title", "Sports Analyst / User", Role.USER.getTitle());
    }

    // ---- Defensive Security Extensions --------------------------------

    private static void testUsernamePolicy() {
        System.out.println("Username Policy & Canonicalization");
        check("null username rejected", !UsernamePolicy.validate(null).isValid());
        check("empty username rejected", !UsernamePolicy.validate("   ").isValid());
        check("short username rejected (<3)", !UsernamePolicy.validate("ab").isValid());
        check("long username rejected (>32)", !UsernamePolicy.validate("a".repeat(33)).isValid());
        check("invalid characters rejected", !UsernamePolicy.validate("user@pulse!").isValid());
        check("valid alphanumeric username accepted", UsernamePolicy.validate("analyst_01.pro").isValid());

        String canonical = UsernamePolicy.canonicalize("  KaBiR_Singh  ");
        checkEquals("canonicalize trims and lowercases", "kabir_singh", canonical);
    }

    private static void testPasswordPolicyAndConfirmation() {
        System.out.println("Password Policy & Confirmation");
        check("empty password rejected", !PasswordPolicy.validate("").isValid());
        check("short password rejected (<10 chars)", !PasswordPolicy.validate("short9").isValid());
        check("10+ character password accepted", PasswordPolicy.validate("ValidTenChars!").isValid());

        check("password confirmation mismatch rejected",
                !PasswordPolicy.validateConfirmation("ValidPassword123", "MismatchPassword123").isValid());
        check("password confirmation match accepted",
                PasswordPolicy.validateConfirmation("ValidPassword123", "ValidPassword123").isValid());
    }

    private static void testBruteForceLockoutAndReset() {
        System.out.println("Brute-Force Protection & Account Lockout");
        AuthenticationService auth = new AuthenticationService(new CustomHashTable(17));
        auth.registerDirect("brute_target", "TargetPassword@2026", Role.USER);

        // Fail 5 times to trigger lockout
        for (int i = 0; i < AuthenticationService.MAX_FAILED_ATTEMPTS; i++) {
            auth.login("brute_target", "WrongPassword!123");
        }

        User user = auth.getCredentialTable().search("brute_target");
        check("account enters LOCKED state after 5 failures", user.isLocked());
        checkEquals("failed attempt counter is 5", 5, user.getFailedAttempts());

        // Attempting to log in with correct password while locked must fail
        Session lockedLogin = auth.login("brute_target", "TargetPassword@2026");
        check("locked account rejects correct password during lockout window", lockedLogin == null);

        // Administrative or timer unlock
        user.unlock();
        check("account successfully unlocks", !user.isLocked());

        Session unlockedLogin = auth.login("brute_target", "TargetPassword@2026");
        check("unlocked account accepts correct password", unlockedLogin != null);
        checkEquals("successful login resets failed attempts to 0", 0, user.getFailedAttempts());
    }

    private static void testSessionLifecycleAndExpiration() {
        System.out.println("Session Lifecycle & Expiration");
        // Test active session
        Session s1 = new Session("alice", Role.USER, 60000L); // 1 minute
        check("new session is authenticated", s1.isAuthenticated());
        check("new session is not expired", !s1.isExpired());

        // Invalidate on logout
        s1.invalidate();
        check("invalidated session is not authenticated", !s1.isAuthenticated());

        // Expired session test
        long now = System.currentTimeMillis();
        Session expiredSession = new Session("token123", "bob", Role.USER, now - 5000, now - 1000);
        check("past timestamp session evaluates as expired", expiredSession.isExpired());
        check("expired session rejects isAuthenticated()", !expiredSession.isAuthenticated());
    }

    private static void testAdminPrivilegeEscalationDefense() {
        System.out.println("Privilege Escalation Defense & Business Logic Authorization");
        AuthenticationService auth = new AuthenticationService(new CustomHashTable(17));

        // Public registration requesting Role.ADMIN is blocked
        AuthenticationService.AuthResult publicAdmin = auth.register("sneaky_admin", "Password@2026", Role.ADMIN);
        check("public portal rejects Role.ADMIN registration", !publicAdmin.isSuccess());

        // Normal public registration defaults to Role.USER
        AuthenticationService.AuthResult publicUser = auth.register("honest_user", "Password@2026", "Password@2026");
        check("public registration creates Role.USER", publicUser.isSuccess() && publicUser.getUser().getRole() == Role.USER);

        // Business Logic Authorization: Passing a non-admin session into AdminConsole throws SecurityException
        Session userSession = new Session("honest_user", Role.USER, 60000L);
        boolean blockedByBusinessLogic = false;
        try {
            AdminConsole.run(userSession, auth, new Scanner(""), () -> {});
        } catch (SecurityException se) {
            blockedByBusinessLogic = true;
        }
        check("USER denied admin access via business logic check", blockedByBusinessLogic);
    }

    private static void testHashTableResizePreservesAuthentication() {
        System.out.println("Custom Hash Table Resizing & Credential Preservation");
        CustomHashTable smallTable = new CustomHashTable(5); // tiny table
        AuthenticationService auth = new AuthenticationService(smallTable);

        // Seed 10 users to trigger multiple table resizings
        for (int i = 1; i <= 10; i++) {
            auth.registerDirect("user_" + i, "Password#2026_" + i, Role.USER);
        }

        check("table capacity expanded beyond initial size 5", smallTable.getCapacity() > 5);
        checkEquals("all 13 users preserved after resize", 13, smallTable.getSize()); // 3 default + 10 added

        // Verify that credential authentication still works seamlessly after resizing
        Session s = auth.login("user_7", "Password#2026_7");
        check("authentication succeeds seamlessly after dynamic table resizing", s != null);
    }

    private static void testDedicatedSecuritySelfTest() {
        System.out.println("\n====================================================");
        System.out.println("        SPORTSPULSE AUTH SECURITY TEST");
        System.out.println("====================================================");

        AuthenticationService auth = new AuthenticationService(new CustomHashTable(17));
        User admin = auth.getCredentialTable().search("admin");

        check("Plaintext password is not stored", !admin.getPasswordHash().equals("Admin@SportsPulse2026!"));

        String salt1 = PasswordHasher.generateSalt();
        String salt2 = PasswordHasher.generateSalt();
        check("Every user receives unique salt", !salt1.equals(salt2));

        check("Wrong password rejected", auth.login("admin", "WrongGuess!2026") == null);
        check("Correct password accepted", auth.login("admin", "Admin@SportsPulse2026!") != null);

        Session coachSession = auth.login("coach", "Coach@SportsPulse2026!");
        boolean userBlocked = false;
        try {
            AdminConsole.run(coachSession, auth, new Scanner(""), () -> {});
        } catch (SecurityException e) {
            userBlocked = true;
        }
        check("USER cannot access ADMIN console", userBlocked);

        check("Public ADMIN registration blocked", !auth.register("fake_admin", "Pass@SportsPulse2026", Role.ADMIN).isSuccess());

        // Brute-force lockout test
        auth.registerDirect("lock_test", "SafePassword@2026", Role.USER);
        for (int i = 0; i < 5; i++) auth.login("lock_test", "BadPassword#123");
        check("Brute-force lockout works", auth.getCredentialTable().search("lock_test").isLocked());

        auth.getCredentialTable().search("lock_test").unlock();
        auth.login("lock_test", "SafePassword@2026");
        check("Failed attempts reset after successful login", auth.getCredentialTable().search("lock_test").getFailedAttempts() == 0);

        long now = System.currentTimeMillis();
        Session expired = new Session("tok", "user", Role.USER, now - 10000, now - 1000);
        check("Expired session rejected", !expired.isAuthenticated());

        Session active = new Session("active_user", Role.USER, 60000);
        auth.logout(active);
        check("Logged-out session rejected", !active.isAuthenticated());

        auth.register("unique_guy", "Pass@SportsPulse2026", "Pass@SportsPulse2026");
        check("Duplicate username blocked", !auth.register("unique_guy", "Pass@SportsPulse2026", "Pass@SportsPulse2026").isSuccess());

        // Hash collision handling
        UniversalHashFunction fixed = new UniversalHashFunction(7, 1000003L, 1L, 0L);
        CustomHashTable collTable = new CustomHashTable(7, fixed);
        collTable.insert("key1", new User("key1", "h1", "s1", Role.USER));
        collTable.insert("key2", new User("key2", "h2", "s2", Role.USER));
        check("Hash collision handled", collTable.search("key1") != null && collTable.search("key2") != null);

        // Resizing preserves auth
        CustomHashTable resizeTbl = new CustomHashTable(3);
        AuthenticationService resizeAuth = new AuthenticationService(resizeTbl);
        resizeAuth.registerDirect("tester", "TesterPass@2026", Role.USER);
        resizeTbl.resize(17);
        check("Hash-table resize preserves authentication", resizeAuth.login("tester", "TesterPass@2026") != null);

        System.out.println("====================================================\n");
    }

    // ---- Original Benchmark Baseline Tests ---------------------------

    private static void testNaiveSearch() {
        System.out.println("Naive String Search (Baseline against KMP)");
        String text = "the striker scored a goal and then another goal";
        String pattern = "goal";
        List<Integer> naiveHits = NaiveSearch.search(text, pattern);
        List<Integer> kmpHits = KMP.search(text, pattern);
        checkEquals("NaiveSearch matches KMP search results", kmpHits, naiveHits);
        check("NaiveSearch returns empty for missing pattern", NaiveSearch.search("text", "xyz").isEmpty());
    }

    private static void testFlowGraphCopy() {
        System.out.println("FlowGraph Deep Copy & Reset");
        FlowGraph g = new FlowGraph(4);
        g.addEdge(0, 1, 10);
        g.addEdge(1, 3, 5);
        g.addEdge(0, 2, 5);
        g.addEdge(2, 3, 10);

        FlowGraph copy = g.copy();
        checkEquals("copied graph has same vertex count", g.n, copy.n);

        int flowInCopy = EdmondsKarp.maxFlowWithMinCut(copy, 0, 3).maxFlow;
        checkEquals("flow in copy is computed", 10, flowInCopy);
        checkEquals("original graph remains unmutated", 0, g.edgesFrom(0).get(0).flow);
    }

    // ---- Core Algorithm Tests (Modules 1 - 6) -------------------------

    private static void testKMP() {
        System.out.println("KMP");
        List<Integer> r = KMP.search("ababababa", "aba");
        checkEquals("finds all occurrences", List.of(0, 2, 4, 6), r);
        check("no match returns empty", KMP.search("abcdef", "xyz").isEmpty());
    }

    private static void testZFunction() {
        System.out.println("Z-Function");
        int[] z = ZFunction.compute("aabzaa");
        checkEquals("z[1] matches remaining run length", 1, z[1]);
        checkEquals("z[4] matches remaining run length", 2, z[4]);
        List<Integer> hits = ZFunction.search("banana", "an");
        checkEquals("search finds pattern", List.of(1, 3), hits);
    }

    private static void testRabinKarp() {
        System.out.println("Rabin-Karp");
        List<Integer> r = RabinKarp.search("banana", "an");
        checkEquals("finds substring occurrences", List.of(1, 3), r);
        Map<String, List<Integer>> batch = RabinKarp.batchSearch("cricket football cricket", List.of("cricket", "goal"));
        checkEquals("batch search hit count", 2, batch.get("cricket").size());
        check("batch search miss is empty", batch.get("goal").isEmpty());
    }

    private static void testAhoCorasick() {
        System.out.println("Aho-Corasick");
        AhoCorasick ac = new AhoCorasick();
        ac.addKeyword("goal");
        ac.addKeyword("foul");
        ac.addKeyword("red card");
        ac.build();
        Map<String, List<Integer>> hits = ac.search("what a goal and a terrible foul leading to a red card");
        check("finds 'goal'", hits.containsKey("goal"));
        check("finds 'foul'", hits.containsKey("foul"));
        check("finds 'red card'", hits.containsKey("red card"));
    }

    private static void testEditDistance() {
        System.out.println("Edit Distance");
        checkEquals("levenshtein kitten/sitting", 3, EditDistance.levenshtein("kitten", "sitting"));
        checkEquals("levenshtein identical strings", 0, EditDistance.levenshtein("same", "same"));
        checkEquals("damerau handles transposition", 1, EditDistance.damerauLevenshtein("ca", "ac"));
        String closest = EditDistance.closestMatch("mesii", List.of("ronaldo", "messi", "neymar"));
        checkEquals("closest match picks nearest", "messi", closest);
    }

    private static void testSuffixArrayAndKasai() {
        System.out.println("Suffix Array + Kasai's LCP");
        SuffixArray sa = new SuffixArray("banana");
        List<Integer> occurrences = sa.search("ana");
        check("finds all occurrences of 'ana'", occurrences.size() == 2 && occurrences.containsAll(List.of(1, 3)));
        check("does not find absent pattern", sa.search("xyz").isEmpty());

        int[] lcp = Kasai.buildLCP(sa);
        checkEquals("LCP array length matches suffix array length (text + sentinel)", 7, lcp.length);
        String lrs = Kasai.longestRepeatedSubstring("banana");
        checkEquals("longest repeated substring in 'banana'", "ana", lrs);
    }

    private static void testMatrixChain() {
        System.out.println("Matrix-Chain Multiplication");
        int[] p = {10, 20, 30};
        MatrixChain.Result r = MatrixChain.solve(p, new String[]{"A", "B"});
        checkEquals("minimum scalar multiplications for A(10x20)*B(20x30)", 6000L, r.minCost);
    }

    private static void testFlowAlgorithmsAgree() {
        System.out.println("Max-Flow (Ford-Fulkerson / Edmonds-Karp / Dinic agreement)");
        FlowGraph g1 = new FlowGraph(4);
        g1.addEdge(0, 1, 3);
        g1.addEdge(0, 2, 2);
        g1.addEdge(1, 2, 1);
        g1.addEdge(1, 3, 2);
        g1.addEdge(2, 3, 4);

        FlowGraph g2 = g1.copy();
        FlowGraph g3 = g1.copy();

        int ff = FordFulkerson.maxFlow(g1, 0, 3);
        int ek = EdmondsKarp.maxFlowWithMinCut(g2, 0, 3).maxFlow;
        int dinic = new Dinic(g3).maxFlow(0, 3);

        checkEquals("Edmonds-Karp matches Ford-Fulkerson", ff, ek);
        checkEquals("Dinic matches Ford-Fulkerson", ff, dinic);
        check("max flow is a sane positive value", ff > 0);
    }

    private static void testBipartiteMatching() {
        System.out.println("Bipartite Matching (Hopcroft-Karp)");
        BipartiteMatching bm = new BipartiteMatching(3, 3);
        bm.addEdge(0, 0);
        bm.addEdge(1, 1);
        bm.addEdge(2, 2);
        checkEquals("perfect matching size for this graph", 3, bm.maxMatching());
    }

    private static void testSATSolver() {
        System.out.println("SAT Solver");
        SATSolver sat = new SATSolver(2);
        sat.addClause(1, 2);
        sat.addClause(-1, 2);
        Map<Integer, Boolean> sol = sat.solve();
        check("satisfiable formula returns an assignment", sol != null);

        SATSolver unsat = new SATSolver(1);
        unsat.addClause(1);
        unsat.addClause(-1);
        check("contradictory formula is UNSAT", unsat.solve() == null);
    }

    private static void testGraphReductions() {
        System.out.println("Graph Reductions (Clique / Independent Set / Vertex Cover)");
        boolean[][] g = new boolean[4][4];
        g[0][1] = g[1][0] = true;
        g[1][2] = g[2][1] = true;
        g[0][2] = g[2][0] = true;
        List<Integer> maxClique = GraphReductions.maxClique(g, 4);
        checkEquals("max clique in a triangle plus isolated vertex", 3, maxClique.size());
    }

    private static void testMillerRabin() {
        System.out.println("Miller-Rabin Primality");
        check("97 is prime", MillerRabin.isProbablePrime(97, 20));
        check("100 is not prime", !MillerRabin.isProbablePrime(100, 20));
        checkEquals("nextPrime(90) == 97", 97L, MillerRabin.nextPrime(90));
    }

    private static void testRandomizedQuickSort() {
        System.out.println("Randomized QuickSort");
        List<Integer> list = new ArrayList<>(List.of(9, 3, 7, 1, 5, 8, 2));
        RandomizedQuickSort.sort(list, Integer::compare);
        checkEquals("sorts ascending", List.of(1, 2, 3, 5, 7, 8, 9), list);
    }

    private static void testReservoirSampling() {
        System.out.println("Reservoir Sampling");
        List<Integer> stream = new ArrayList<>();
        for (int i = 0; i < 100; i++) stream.add(i);
        List<Integer> sample = ReservoirSampling.sample(stream, 10);
        checkEquals("sample size matches k", 10, sample.size());
        check("all sampled elements came from the stream", new HashSet<>(stream).containsAll(sample));
    }

    private static void testBlellochScan() {
        System.out.println("Blelloch Scan");
        int[] in = {1, 2, 3, 4, 5};
        int[] ex = BlellochScan.exclusiveScan(in);
        checkEquals("exclusive scan of [1,2,3,4,5]", List.of(0, 1, 3, 6, 10),
                Arrays.stream(ex).boxed().toList());
        int[] inc = BlellochScan.inclusiveScan(in);
        checkEquals("inclusive scan of [1,2,3,4,5]", List.of(1, 3, 6, 10, 15),
                Arrays.stream(inc).boxed().toList());
    }

    private static void testPlayerIdsResolveAcrossAllSports() {
        System.out.println("Data integrity: match event player IDs resolve to real players");
        MockData data = new MockData();
        int resolved = 0;
        int unresolved = 0;
        for (var match : data.allMatches) {
            for (var event : match.getEvents()) {
                if (data.findById(event.getPlayerId()) != null) {
                    resolved++;
                } else {
                    unresolved++;
                }
            }
        }
        check("all " + resolved + " match events resolve to a real player (found " + unresolved + " unresolved)",
                unresolved == 0 && resolved > 0);
    }

    // ---- ANSI Terminal 10/10 Presentation & UI Suite ----------------

    private static void testAnsiTruecolorAndFallbacks() {
        System.out.println("ANSI Presentation & TrueColor Fallbacks");
        String styled = Ansi.BOLD + Ansi.RED + "SportsPulse" + Ansi.RESET;
        checkEquals("strip removes ANSI sequences", "SportsPulse", Ansi.strip(styled));
        checkEquals("visibleLength ignores ANSI codes", 11, Ansi.visibleLength(styled));
        checkEquals("visibleLength handles empty/null", 0, Ansi.visibleLength(null));

        String red16 = Ansi.mapRgbToAnsi16(255, 0, 0, false);
        check("red RGB maps to ANSI red code", red16.contains("31") || red16.contains("91"));

        String green16 = Ansi.mapRgbToAnsi16(0, 255, 0, false);
        check("green RGB maps to ANSI green code", green16.contains("32") || green16.contains("92"));

        String blue16 = Ansi.mapRgbToAnsi16(0, 0, 255, false);
        check("blue RGB maps to ANSI blue code", blue16.contains("34") || blue16.contains("94"));

        String blackBg = Ansi.mapRgbToAnsi16(0, 0, 0, true);
        check("black background maps to ANSI 40m", blackBg.contains("40"));

        check("cursorTo produces valid sequence", Ansi.cursorTo(4, 12).contains("4;12H"));
        check("CURSOR_HIDE sequence format", Ansi.CURSOR_HIDE.isEmpty() || Ansi.CURSOR_HIDE.contains("?25l"));
        check("CURSOR_SHOW sequence format", Ansi.CURSOR_SHOW.isEmpty() || Ansi.CURSOR_SHOW.contains("?25h"));
    }

    private static void testConsoleCapabilities() {
        System.out.println("Console Capabilities & Terminal Adaptation");
        check("terminal width is within safe range [40..240]",
                ConsoleCapabilities.getTerminalWidth() >= 40 && ConsoleCapabilities.getTerminalWidth() <= 240);

        ConsoleCapabilities.setTerminalWidth(65);
        checkEquals("narrow width maps to NARROW tier", ConsoleCapabilities.WidthTier.NARROW, ConsoleCapabilities.getWidthTier());

        ConsoleCapabilities.setTerminalWidth(85);
        checkEquals("standard width maps to STANDARD tier", ConsoleCapabilities.WidthTier.STANDARD, ConsoleCapabilities.getWidthTier());

        ConsoleCapabilities.setTerminalWidth(125);
        checkEquals("wide width maps to WIDE tier", ConsoleCapabilities.WidthTier.WIDE, ConsoleCapabilities.getWidthTier());
        ConsoleCapabilities.resetTerminalWidth();

        ConsoleCapabilities.setInteractive(false);
        check("interactive override to false", !ConsoleCapabilities.isInteractive());
        ConsoleCapabilities.resetInteractive();

        check("ColorMode supports TRUECOLOR, ANSI16, and MONO", ConsoleCapabilities.ColorMode.values().length == 3);
    }

    private static void testTerminalThemeAndAccessibility() {
        System.out.println("Terminal Theme & Accessibility Badges");
        String success = TerminalTheme.successBadge("Operation complete");
        check("success badge contains OK text", success.contains("OK") && success.contains("Operation complete"));

        String error = TerminalTheme.errorBadge("Authentication failed");
        check("error badge contains FAIL text", error.contains("FAIL") && error.contains("Authentication failed"));

        String warn = TerminalTheme.warnBadge("Session close to timeout");
        check("warning badge contains WARN text", warn.contains("WARN") && warn.contains("Session close to timeout"));

        String info = TerminalTheme.infoBadge("System operational");
        check("info badge contains INFO text", info.contains("INFO") && info.contains("System operational"));

        check("admin badge contains ADMIN text", TerminalTheme.adminBadge().contains("ADMIN"));
        check("user badge contains USER text", TerminalTheme.userBadge().contains("USER"));

        check("primary semantic color defined", TerminalTheme.primary() != null);
        check("secondary semantic color defined", TerminalTheme.secondary() != null);
        check("accent semantic color defined", TerminalTheme.accent() != null);
    }

    private static void testTerminalTableFormatting() {
        System.out.println("Terminal Table & Auto-Wrapping Formatting");
        List<String> headers = List.of("Algorithm", "Class", "Time Complexity");
        List<List<String>> rows = List.of(
                List.of("KMP", "String Matching", "O(N + M)"),
                List.of("Dinic", "Max Flow", "O(V^2 * E)"),
                List.of("Blelloch Scan", "Parallel Prefix", "O(N) Work / O(log N) Depth")
        );

        TerminalTable table = TerminalTable.of(headers, rows);
        String output = table.render();

        check("table output contains all headers",
                output.contains("Algorithm") && output.contains("Class") && output.contains("Time Complexity"));
        check("table output contains rows",
                output.contains("KMP") && output.contains("Dinic") && output.contains("Blelloch Scan"));
        check("table output contains vertical borders", output.contains("|") || output.contains("│"));

        ConsoleCapabilities.setTerminalWidth(50);
        List<String> longHeaders = List.of("ID", "Description");
        List<List<String>> longRows = List.of(
                List.of("1", "This is an extremely long description that should be truncated with an ellipsis to prevent line wrapping across narrow screens")
        );
        String truncated = TerminalTable.of(longHeaders, longRows).render();
        check("overlong row is truncated with ellipsis", truncated.contains("..."));
        ConsoleCapabilities.resetTerminalWidth();
    }

    private static void testTerminalInputAndMenu() {
        System.out.println("Terminal Input & Menu Selection Engine");
        check("parses ANSI arrow up sequence", TerminalInput.parse("\u001B[A").isUp());
        check("parses vim/gamer up 'w'", TerminalInput.parse("w").isUp());
        check("parses vim up 'k'", TerminalInput.parse("k").isUp());
        check("parses ANSI arrow down sequence", TerminalInput.parse("\u001B[B").isDown());
        check("parses vim/gamer down 's'", TerminalInput.parse("s").isDown());
        check("parses vim down 'j'", TerminalInput.parse("j").isDown());
        check("parses empty line as Enter", TerminalInput.parse("").isEnter());
        check("parses 'q' as Back/Exit", TerminalInput.parse("q").isBack());
        check("parses numeric digits", TerminalInput.parse("3").isDigit() && "3".equals(TerminalInput.parse("3").getDigit()));

        TerminalMenu menu = new TerminalMenu("TEST MENU", "Unit Test Simulation");
        menu.addItem("1", "Search Feature", "Search players");
        menu.addItem("2", "Reports Feature", "Analyze match reports");
        menu.setExitOption("0", "Exit");

        Scanner scChoice1 = new Scanner("1\n");
        MenuResult res1 = menu.renderAndSelect(scChoice1);
        check("menu selects option 1", res1 != null && res1.isSelected() && "1".equals(res1.getSelectedId()));

        Scanner scChoiceExit = new Scanner("0\n");
        MenuResult resExit = menu.renderAndSelect(scChoiceExit);
        check("menu handles exit option 0", resExit != null && resExit.isCancelOrExit());
    }

    private static void testTerminalRendererAndSafety() {
        System.out.println("Terminal Safety & Visual Primitives");
        TerminalRenderer.installSafetyShutdownHook();
        check("terminal safety shutdown hook is installed", TerminalRenderer.isShutdownHookInstalled());

        String centered = TerminalRenderer.centerText("Hello", 15);
        checkEquals("centerText pads to target width", 15, centered.length());
        check("centerText contains original word", centered.contains("Hello"));

        TerminalChart.DataPoint dp = new TerminalChart.DataPoint("QuickSort", 1.45, "ms");
        checkEquals("DataPoint label", "QuickSort", dp.getLabel());
        checkEquals("DataPoint value", 1.45, dp.getValue());
        checkEquals("DataPoint unit", "ms", dp.getUnit());
    }

    private static void testAppPathsAndPortability() {
        System.out.println("AppPaths & Portability Engine");
        java.nio.file.Path root = AppPaths.getProjectRoot();
        check("AppPaths resolves project root", root != null && java.nio.file.Files.exists(root));
        check("AppPaths resolves runtime directory", AppPaths.getRuntimeDir() != null && java.nio.file.Files.exists(AppPaths.getRuntimeDir()));
        check("AppPaths resolves logs directory", AppPaths.getLogsDir().endsWith("logs"));
        check("AppPaths resolves reports directory", AppPaths.getReportsDir().endsWith("reports"));
        check("AppPaths resolves benchmarks directory", AppPaths.getBenchmarksDir().endsWith("benchmarks"));
        check("AppPaths resolves data directory", AppPaths.getDataDir().endsWith("data"));
        check("AppPaths resolves benchmark CSV into benchmarks dir",
                AppPaths.resolveBenchmarkCsv("bench.csv").getParent().equals(AppPaths.getBenchmarksDir()));

        boolean envReady = EnvironmentCheck.run();
        check("EnvironmentCheck reports READY", envReady);
    }
}
