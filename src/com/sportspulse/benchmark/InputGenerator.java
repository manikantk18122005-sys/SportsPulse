package com.sportspulse.benchmark;

import com.sportspulse.algo.flow.FlowGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Reproducible input generators for the Empirical Complexity Lab.
 *
 * Uses deterministic pseudorandom generators with documented fixed seeds
 * to ensure that all algorithm comparisons during faculty demonstrations
 * receive strictly identical inputs and are 100% reproducible.
 */
public final class InputGenerator {

    public static final long DEFAULT_SEED = 42_000_1337L;

    private InputGenerator() {}

    // =========================================================================
    // STRING SEARCH INPUT GENERATION
    // =========================================================================

    /**
     * Generates a classic worst-case string matching scenario for Naive search:
     * Text:    "AAAA...AAABAAAA...AAAB..."
     * Pattern: "AAAA...AAAB"
     *
     * In this text, Naive search matches m-1 characters at almost every position
     * before mismatching on 'B', driving runtime to Theta(n * m).
     * In contrast, KMP's failure table (LPS) shifts the pattern in O(1) without
     * backtracking the text pointer, completing in Theta(n + m).
     */
    public static class StringTestCase {
        public final String text;
        public final String pattern;
        public final String description;

        public StringTestCase(String text, String pattern, String description) {
            this.text = text;
            this.pattern = pattern;
            this.description = description;
        }
    }

    public static StringTestCase generateWorstCaseString(int textLength, int patternLength) {
        if (patternLength >= textLength) patternLength = Math.max(2, textLength / 4);
        StringBuilder textSb = new StringBuilder(textLength);
        StringBuilder patSb = new StringBuilder(patternLength);

        // Build pattern: "A" repeated (m-1) times followed by "B"
        for (int i = 0; i < patternLength - 1; i++) patSb.append('A');
        patSb.append('B');
        String pattern = patSb.toString();

        // Build text: repeated blocks of 'A's with occasional 'B'
        int block = patternLength;
        while (textSb.length() < textLength) {
            int remaining = textLength - textSb.length();
            int curBlock = Math.min(block, remaining);
            for (int i = 0; i < curBlock - 1; i++) textSb.append('A');
            if (textSb.length() < textLength) textSb.append('A'); // mostly 'A's to force worst-case
        }
        // Place one or two actual matches near the end
        if (textSb.length() >= patternLength) {
            textSb.setCharAt(textSb.length() - 1, 'B');
        }

        return new StringTestCase(textSb.toString(), pattern,
                String.format("Worst-case repeated prefix (Text: %d chars, Pattern: %d chars)", textLength, patternLength));
    }

    public static StringTestCase generateNaturalSportsText(int targetLength) {
        String[] phrases = {
                "In the 45th minute Messi picked up the ball in central midfield and accelerated.",
                "Mbappe sprinted down the left wing leaving two defenders trailing behind.",
                "Modric delivered a pinpoint 40-meter cross into the penalty box.",
                "Haaland rose highest to connect with a thunderous header into top corner.",
                "Courtois pulled off an extraordinary fingertip reflex save to deny a certain goal.",
                "The referee blew the whistle for a tactical foul right at the edge of the box.",
                "Real Madrid counter-attacked at blistering pace through Vinicius Junior.",
                "Manchester City established total possession dominance with 72 percent control."
        };

        StringBuilder sb = new StringBuilder(targetLength + 100);
        int idx = 0;
        while (sb.length() < targetLength) {
            sb.append(phrases[idx % phrases.length]).append(" ");
            idx++;
        }
        String text = sb.substring(0, targetLength);
        String pattern = "counter-attacked at blistering pace";
        return new StringTestCase(text, pattern,
                String.format("Natural Sports Commentary Corpus (Text: %d chars, Pattern: %d chars)", targetLength, pattern.length()));
    }

    // =========================================================================
    // FLOW NETWORK INPUT GENERATION
    // =========================================================================

    /**
     * Specification holding the edge topology of a flow network.
     * Allows producing fresh, identical FlowGraph instances for each algorithm run.
     */
    public static class FlowNetworkSpec {
        public final int vertexCount;
        public final int source;
        public final int sink;
        public final List<int[]> edges; // [from, to, capacity]

        public FlowNetworkSpec(int vertexCount, int source, int sink, List<int[]> edges) {
            this.vertexCount = vertexCount;
            this.source = source;
            this.sink = sink;
            this.edges = edges;
        }

        public FlowGraph createFreshGraph() {
            FlowGraph g = new FlowGraph(vertexCount);
            for (int[] e : edges) {
                g.addEdge(e[0], e[1], e[2]);
            }
            return g;
        }

        public int getEdgeCount() {
            return edges.size();
        }
    }

    /**
     * Generates a reproducible layered flow network with a guaranteed path from source to sink.
     *
     * @param vertexCount Number of vertices (must be >= 4).
     * @param seed        Random seed for deterministic generation.
     */
    public static FlowNetworkSpec generateLayeredFlowNetwork(int vertexCount, long seed) {
        if (vertexCount < 4) vertexCount = 4;
        Random rng = new Random(seed);
        int source = 0;
        int sink = vertexCount - 1;

        // Partition intermediate vertices into layers
        int intermediate = vertexCount - 2;
        int numLayers = Math.max(2, (int) Math.sqrt(intermediate));
        List<List<Integer>> layers = new ArrayList<>();
        for (int i = 0; i < numLayers; i++) layers.add(new ArrayList<>());

        for (int i = 1; i <= intermediate; i++) {
            int layerIdx = (i - 1) % numLayers;
            layers.get(layerIdx).add(i);
        }

        List<int[]> edges = new ArrayList<>();

        // Connect source to all nodes in first layer
        for (int v : layers.get(0)) {
            int cap = 10 + rng.nextInt(40);
            edges.add(new int[]{source, v, cap});
        }

        // Connect consecutive layers
        for (int l = 0; l < numLayers - 1; l++) {
            List<Integer> cur = layers.get(l);
            List<Integer> next = layers.get(l + 1);
            for (int u : cur) {
                // Ensure at least one forward edge per node
                int primaryTarget = next.get(rng.nextInt(next.size()));
                edges.add(new int[]{u, primaryTarget, 5 + rng.nextInt(35)});

                // Add additional cross-edges for density
                for (int v : next) {
                    if (v != primaryTarget && rng.nextDouble() < 0.4) {
                        edges.add(new int[]{u, v, 5 + rng.nextInt(25)});
                    }
                }
            }
        }

        // Connect last layer to sink
        for (int v : layers.get(numLayers - 1)) {
            int cap = 10 + rng.nextInt(40);
            edges.add(new int[]{v, sink, cap});
        }

        return new FlowNetworkSpec(vertexCount, source, sink, edges);
    }

    // =========================================================================
    // SORTING INPUT GENERATION
    // =========================================================================

    public static List<Integer> generateRandomList(int size, long seed) {
        Random rng = new Random(seed);
        List<Integer> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(rng.nextInt(size * 10));
        }
        return list;
    }
}
