package com.sportspulse.model;

import java.util.HashMap;
import java.util.Map;

public class Player {
    private final String id;              // e.g. P-FB-001 (used by Rabin-Karp ID lookups)
    private String name;
    private final Sport sport;
    private String position;
    private double cost;                  // budget cost, used in roster selection DP
    private double skill;                 // 0-100 skill rating
    private double synergyBase;           // baseline synergy contribution
    private boolean available = true;     // availability flag for scheduling/matching
    private final Map<String, Double> stats = new HashMap<>(); // e.g. goals, points, wickets

    public Player(String id, String name, Sport sport, String position,
                  double cost, double skill, double synergyBase) {
        this.id = id;
        this.name = name;
        this.sport = sport;
        this.position = position;
        this.cost = cost;
        this.skill = skill;
        this.synergyBase = synergyBase;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Sport getSport() { return sport; }
    public String getPosition() { return position; }
    public double getCost() { return cost; }
    public double getSkill() { return skill; }
    public double getSynergyBase() { return synergyBase; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean a) { this.available = a; }
    public Map<String, Double> getStats() { return stats; }
    public void addStat(String key, double value) {
        stats.merge(key, value, Double::sum);
    }

    @Override
    public String toString() {
        return String.format("%-8s %-18s %-10s %-12s skill=%-5.1f cost=%-6.1f", 
                id, name, sport.getName(), position, skill, cost);
    }
}
