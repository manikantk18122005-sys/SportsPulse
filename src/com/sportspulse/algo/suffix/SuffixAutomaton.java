package com.sportspulse.algo.suffix;

import java.util.HashMap;
import java.util.Map;

/**
 * Suffix Automaton (SAM): a compact automaton recognizing exactly the substrings
 * of a string, built online in O(n). Supports O(m) substring-existence checks
 * regardless of text length -- ideal for repeated "does this phrase ever occur
 * anywhere in the whole commentary archive" queries.
 */
public final class SuffixAutomaton {

    private static class State {
        int len, link;
        Map<Character, Integer> next = new HashMap<>();
    }

    private State[] states;
    private int size, last;

    public SuffixAutomaton(String s) {
        int maxStates = 2 * s.length() + 5;
        states = new State[maxStates];
        states[0] = new State();
        states[0].len = 0;
        states[0].link = -1;
        size = 1;
        last = 0;
        for (char c : s.toCharArray()) extend(c);
    }

    private void extend(char c) {
        int cur = size++;
        states[cur] = new State();
        states[cur].len = states[last].len + 1;
        int p = last;
        while (p != -1 && !states[p].next.containsKey(c)) {
            states[p].next.put(c, cur);
            p = states[p].link;
        }
        if (p == -1) {
            states[cur].link = 0;
        } else {
            int q = states[p].next.get(c);
            if (states[p].len + 1 == states[q].len) {
                states[cur].link = q;
            } else {
                int clone = size++;
                states[clone] = new State();
                states[clone].len = states[p].len + 1;
                states[clone].link = states[q].link;
                states[clone].next = new HashMap<>(states[q].next);
                while (p != -1 && states[p].next.get(c) != null && states[p].next.get(c) == q) {
                    states[p].next.put(c, clone);
                    p = states[p].link;
                }
                states[q].link = clone;
                states[cur].link = clone;
            }
        }
        last = cur;
    }

    /** O(m) check: does `pattern` occur anywhere in the original text? */
    public boolean contains(String pattern) {
        int cur = 0;
        for (char c : pattern.toCharArray()) {
            Integer nxt = states[cur].next.get(c);
            if (nxt == null) return false;
            cur = nxt;
        }
        return true;
    }

    /** Longest substring length reachable = states[last].len gives longest repeated info via link tree in general use. */
    public int numberOfStates() { return size; }
}
