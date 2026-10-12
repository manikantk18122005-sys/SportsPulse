package com.sportspulse.algo.npc;

import java.util.*;

/**
 * A small DPLL-style backtracking SAT solver operating on CNF formulas
 * (list of clauses, each clause a list of signed literals, e.g. +3 means
 * variable 3 true, -3 means variable 3 false). Used to encode roster rules
 * like "if playerA selected then playerB must also be selected" or
 * "at most one of {goalkeeperA, goalkeeperB} may start" into boolean
 * constraints and check satisfiability / find one satisfying assignment.
 */
public final class SATSolver {
    private final int numVars;
    private final List<int[]> clauses;

    public SATSolver(int numVars) {
        this.numVars = numVars;
        this.clauses = new ArrayList<>();
    }

    public void addClause(int... literals) { clauses.add(literals); }

    /** Returns a satisfying assignment (1-indexed, value true/false) or null if UNSAT. */
    public Map<Integer, Boolean> solve() {
        Map<Integer, Boolean> assignment = new HashMap<>();
        if (dpll(new ArrayList<>(clauses), assignment)) return assignment;
        return null;
    }

    private boolean dpll(List<int[]> clauses, Map<Integer, Boolean> assignment) {
        // unit propagation
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int[] clause : clauses) {
                Integer unit = unitLiteral(clause, assignment);
                if (unit != null) {
                    assignment.put(Math.abs(unit), unit > 0);
                    changed = true;
                }
            }
            if (isConflict(clauses, assignment)) return false;
        }
        if (isSatisfied(clauses, assignment)) return true;

        int freeVar = pickUnassigned(assignment);
        if (freeVar == -1) return isSatisfied(clauses, assignment);

        for (boolean val : new boolean[]{true, false}) {
            Map<Integer, Boolean> trial = new HashMap<>(assignment);
            trial.put(freeVar, val);
            if (!isConflict(clauses, trial) && dpll(clauses, trial)) {
                assignment.clear();
                assignment.putAll(trial);
                return true;
            }
        }
        return false;
    }

    private Integer unitLiteral(int[] clause, Map<Integer, Boolean> assignment) {
        int unassignedCount = 0, unassignedLit = 0;
        for (int lit : clause) {
            Boolean v = assignment.get(Math.abs(lit));
            if (v == null) { unassignedCount++; unassignedLit = lit; }
            else if ((lit > 0) == v) return null; // clause already satisfied
        }
        return unassignedCount == 1 ? unassignedLit : null;
    }

    private boolean isConflict(List<int[]> clauses, Map<Integer, Boolean> assignment) {
        for (int[] clause : clauses) {
            boolean allAssigned = true, satisfied = false;
            for (int lit : clause) {
                Boolean v = assignment.get(Math.abs(lit));
                if (v == null) { allAssigned = false; }
                else if ((lit > 0) == v) { satisfied = true; }
            }
            if (allAssigned && !satisfied) return true;
        }
        return false;
    }

    private boolean isSatisfied(List<int[]> clauses, Map<Integer, Boolean> assignment) {
        for (int[] clause : clauses) {
            boolean ok = false;
            for (int lit : clause) {
                Boolean v = assignment.get(Math.abs(lit));
                if (v != null && (lit > 0) == v) { ok = true; break; }
            }
            if (!ok) return false;
        }
        return true;
    }

    private int pickUnassigned(Map<Integer, Boolean> assignment) {
        for (int v = 1; v <= numVars; v++) if (!assignment.containsKey(v)) return v;
        return -1;
    }
}
