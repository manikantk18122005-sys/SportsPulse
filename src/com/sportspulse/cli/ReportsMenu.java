package com.sportspulse.cli;

import com.sportspulse.algo.suffix.*;
import com.sportspulse.data.MockData;
import com.sportspulse.model.MatchReport;
import com.sportspulse.util.Console;

import java.util.List;
import java.util.Scanner;

public class ReportsMenu {
    private final MockData data;
    private final Scanner sc;

    public ReportsMenu(MockData data, Scanner sc) { this.data = data; this.sc = sc; }

    public void run() {
        boolean back = false;
        while (!back) {
            Console.header("REPORTS & COMMENTARY ANALYSIS");
            Console.bullet("1. Index a report and search substrings  (Suffix Array)");
            Console.bullet("2. Longest repeated phrase in a report  (Kasai's LCP)");
            Console.bullet("3. Cross-report similarity / plagiarism check  (LCP-based)");
            Console.bullet("4. Fast substring existence check  (Suffix Automaton)");
            Console.bullet("0. Back to main menu");
            Console.prompt("\nSelect an option: ");
            switch (sc.nextLine().trim()) {
                case "1" -> suffixArraySearch();
                case "2" -> longestRepeated();
                case "3" -> crossReportSimilarity();
                case "4" -> suffixAutomatonDemo();
                case "0" -> back = true;
                default -> Console.error("Invalid option.");
            }
            if (!back) Console.pause(sc);
        }
    }

    private MatchReport pickReport() {
        System.out.println("Match reports available:");
        for (int i = 0; i < data.allReports.size(); i++) System.out.println("  " + (i + 1) + ". " + data.allReports.get(i));
        System.out.print("Pick a report number: ");
        try {
            int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
            if (idx < 0 || idx >= data.allReports.size()) { Console.warn("Out of range."); return null; }
            return data.allReports.get(idx);
        } catch (NumberFormatException e) { Console.error("Invalid number."); return null; }
    }

    private void suffixArraySearch() {
        Console.section("Suffix Array Indexing & Search");
        MatchReport r = pickReport();
        if (r == null) return;
        SuffixArray sa = new SuffixArray(r.getBody().toLowerCase());
        System.out.print("Enter a substring to search for: ");
        String pattern = sc.nextLine().trim().toLowerCase();
        List<Integer> positions = sa.search(pattern);
        Console.kv("Occurrences", positions.size());
        if (!positions.isEmpty()) Console.kv("Character offsets", positions);
        Console.kv("Suffix array size (incl. sentinel)", sa.getSuffixArray().length);
    }

    private void longestRepeated() {
        Console.section("Longest Repeated Substring (Kasai's LCP)");
        MatchReport r = pickReport();
        if (r == null) return;
        String longest = Kasai.longestRepeatedSubstring(r.getBody());
        Console.kv("Longest repeated substring", longest.isBlank() ? "(none found)" : "\"" + longest.trim() + "\"");
        Console.kv("Length", longest.length());
    }

    private void crossReportSimilarity() {
        Console.section("Cross-Report Similarity Check");
        if (data.allReports.size() < 2) { System.out.println("Need at least 2 reports."); return; }
        System.out.println("Comparing all report pairs for shared longest common substring coverage:");
        for (int i = 0; i < data.allReports.size(); i++) {
            for (int j = i + 1; j < data.allReports.size(); j++) {
                MatchReport a = data.allReports.get(i), b = data.allReports.get(j);
                double sim = Kasai.similarity(a.getBody(), b.getBody());
                System.out.printf("  %-45s vs %-45s -> similarity=%.1f%%%n",
                        truncate(a.getTitle(), 40), truncate(b.getTitle(), 40), sim * 100);
            }
        }
    }

    private void suffixAutomatonDemo() {
        Console.section("Fast Substring Existence Check (Suffix Automaton)");
        MatchReport r = pickReport();
        if (r == null) return;
        SuffixAutomaton sam = new SuffixAutomaton(r.getBody().toLowerCase());
        System.out.print("Enter a phrase to test for existence: ");
        String phrase = sc.nextLine().trim().toLowerCase();
        boolean found = sam.contains(phrase);
        Console.kv("Phrase found", found ? "YES" : "NO");
        Console.kv("Automaton state count", sam.numberOfStates());
    }

    private String truncate(String s, int len) { return s.length() <= len ? s : s.substring(0, len - 3) + "..."; }
}
