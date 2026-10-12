package com.sportspulse.algo.strmatch;

import java.util.*;

/**
 * Aho-Corasick automaton: builds a trie of keywords plus failure links so an
 * entire text can be scanned once (O(n + total pattern length + matches)) to
 * find ALL keyword occurrences simultaneously. Used to spot multiple event
 * terms at once in commentary text, e.g. "goal", "foul", "yellow card", "wicket".
 */
public final class AhoCorasick {

    private static class Node {
        Map<Character, Node> children = new HashMap<>();
        Node fail;
        List<String> outputs = new ArrayList<>();
    }

    private final Node root = new Node();

    public void addKeyword(String keyword) {
        Node cur = root;
        String lower = keyword.toLowerCase();
        for (char c : lower.toCharArray()) {
            cur = cur.children.computeIfAbsent(c, k -> new Node());
        }
        cur.outputs.add(keyword);
    }

    /** Must be called after all keywords are added, before searching. */
    public void build() {
        Deque<Node> queue = new ArrayDeque<>();
        root.fail = root;
        for (Node child : root.children.values()) {
            child.fail = root;
            queue.add(child);
        }
        while (!queue.isEmpty()) {
            Node cur = queue.poll();
            for (var entry : cur.children.entrySet()) {
                char c = entry.getKey();
                Node child = entry.getValue();
                Node f = cur.fail;
                while (f != root && !f.children.containsKey(c)) f = f.fail;
                if (f.children.containsKey(c) && f.children.get(c) != child) f = f.children.get(c);
                child.fail = (f == child) ? root : f;
                if (child.fail.outputs != null) child.outputs.addAll(child.fail.outputs.isEmpty() ? List.of() : List.of());
                child.outputs = mergeOutputs(child, child.fail);
                queue.add(child);
            }
        }
    }

    private List<String> mergeOutputs(Node child, Node failNode) {
        List<String> merged = new ArrayList<>(child.outputs);
        merged.addAll(failNode.outputs);
        return merged;
    }

    /** Returns map: matched keyword -> list of end-indices (inclusive) in text. */
    public Map<String, List<Integer>> search(String text) {
        Map<String, List<Integer>> results = new LinkedHashMap<>();
        Node cur = root;
        String lower = text.toLowerCase();
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            while (cur != root && !cur.children.containsKey(c)) cur = cur.fail;
            cur = cur.children.getOrDefault(c, root);
            for (String kw : cur.outputs) {
                results.computeIfAbsent(kw, k -> new ArrayList<>()).add(i);
            }
        }
        return results;
    }
}
