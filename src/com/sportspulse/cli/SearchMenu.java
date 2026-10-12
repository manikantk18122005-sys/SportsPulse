package com.sportspulse.cli;

import com.sportspulse.algo.strmatch.*;
import com.sportspulse.data.MockData;
import com.sportspulse.model.MatchReport;
import com.sportspulse.model.Player;
import com.sportspulse.util.Console;

import java.util.*;

public class SearchMenu {
    private final MockData data;
    private final Scanner sc;

    public SearchMenu(MockData data, Scanner sc) { this.data = data; this.sc = sc; }

    public void run() {
        boolean back = false;
        while (!back) {
            Console.header("SEARCH & LOOKUP");
            Console.bullet("1. Find player by exact/partial name  (KMP)");
            Console.bullet("2. Fuzzy name auto-correct  (Damerau-Levenshtein)");
            Console.bullet("3. Detect repeated commentary patterns  (Z-Function)");
            Console.bullet("4. Batch player/match ID lookup in text  (Rabin-Karp)");
            Console.bullet("5. Multi-keyword event scan  (Aho-Corasick)");
            Console.bullet("0. Back to main menu");
            Console.prompt("\nSelect an option: ");
            switch (sc.nextLine().trim()) {
                case "1" -> kmpSearch();
                case "2" -> fuzzyAutocorrect();
                case "3" -> zFunctionDemo();
                case "4" -> rabinKarpDemo();
                case "5" -> ahoCorasickDemo();
                case "0" -> back = true;
                default -> Console.error("Invalid option.");
            }
            if (!back) Console.pause(sc);
        }
    }

    private void kmpSearch() {
        Console.section("KMP Exact Search");
        System.out.print("Enter a name or word to search across all match reports: ");
        String query = sc.nextLine().trim();
        int totalHits = 0;
        for (MatchReport r : data.allReports) {
            List<Integer> hits = KMP.search(r.getBody().toLowerCase(), query.toLowerCase());
            if (!hits.isEmpty()) {
                totalHits += hits.size();
                System.out.printf("  [%s] %d occurrence(s) at char offsets %s%n", r.getMatchId(), hits.size(), hits);
            }
        }
        System.out.println(totalHits == 0 ? "No matches found." : "Total occurrences: " + totalHits);
    }

    private void fuzzyAutocorrect() {
        Console.section("Fuzzy Player Name Auto-correct (Damerau-Levenshtein)");
        System.out.print("Type a (possibly misspelled) player name: ");
        String query = sc.nextLine().trim();
        List<String> names = data.allPlayers.stream().map(Player::getName).toList();
        String best = com.sportspulse.algo.dp.EditDistance.closestMatch(query, names);
        int dist = com.sportspulse.algo.dp.EditDistance.damerauLevenshtein(query.toLowerCase(), best.toLowerCase());
        Console.kv("Closest match", best);
        Console.kv("Edit distance", dist);
        Player p = data.allPlayers.stream().filter(pl -> pl.getName().equals(best)).findFirst().orElse(null);
        if (p != null) Console.kv("Details", p);
    }

    private void zFunctionDemo() {
        Console.section("Repeated Commentary Phrase Detection (Z-Function)");
        System.out.println("Match reports available:");
        for (int i = 0; i < data.allReports.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + data.allReports.get(i));
        }
        System.out.print("Pick a report number: ");
        int idx = parseIndex(sc.nextLine().trim(), data.allReports.size());
        if (idx < 0) return;
        MatchReport r = data.allReports.get(idx);
        List<String> repeats = ZFunction.findRepeatedPhrases(r.getBody(), 5, 2);
        if (repeats.isEmpty()) {
            System.out.println("No 5-word phrase repeats 2+ times in this report.");
        } else {
            System.out.println("Repeated 5-word phrases found:");
            for (String s : repeats) Console.bullet(s);
        }
    }

    private void rabinKarpDemo() {
        Console.section("Batch Player ID Lookup (Rabin-Karp)");
        System.out.println("Sample player IDs available, e.g. P-FB-06, P-BB-01, P-CK-05 ...");
        System.out.print("Enter comma-separated IDs to batch-search across event descriptions: ");
        String[] ids = sc.nextLine().trim().split("\\s*,\\s*");
        StringBuilder haystack = new StringBuilder();
        for (var m : data.allMatches) {
            for (var e : m.getEvents()) haystack.append(e.getPlayerId()).append(" ").append(e.getDescription()).append(" | ");
        }
        Map<String, List<Integer>> results = RabinKarp.batchSearch(haystack.toString(), Arrays.asList(ids));
        for (var entry : results.entrySet()) {
            System.out.printf("  %-10s -> %d occurrence(s)%n", entry.getKey(), entry.getValue().size());
        }
    }

    private void ahoCorasickDemo() {
        Console.section("Multi-Keyword Event Scan (Aho-Corasick)");
        System.out.println("Scanning all match reports for simultaneous event keywords...");
        AhoCorasick ac = new AhoCorasick();
        List<String> keywords = List.of("goal", "foul", "yellow card", "red card", "wicket",
                "three", "dunk", "six", "four", "maiden");
        for (String k : keywords) ac.addKeyword(k);
        ac.build();
        for (MatchReport r : data.allReports) {
            Map<String, List<Integer>> hits = ac.search(r.getBody());
            if (!hits.isEmpty()) {
                System.out.println("  Report: " + r.getTitle());
                for (var e : hits.entrySet()) {
                    System.out.printf("     '%s' found %d time(s)%n", e.getKey(), e.getValue().size());
                }
            }
        }
    }

    private int parseIndex(String input, int size) {
        try {
            int i = Integer.parseInt(input) - 1;
            if (i < 0 || i >= size) { Console.warn("Out of range."); return -1; }
            return i;
        } catch (NumberFormatException e) { Console.error("Invalid number."); return -1; }
    }
}
