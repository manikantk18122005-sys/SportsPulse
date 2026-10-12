package com.sportspulse.model;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private final String id;
    private final String name;
    private final Sport sport;
    private final List<Player> roster = new ArrayList<>();

    public Team(String id, String name, Sport sport) {
        this.id = id;
        this.name = name;
        this.sport = sport;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Sport getSport() { return sport; }
    public List<Player> getRoster() { return roster; }
    public void addPlayer(Player p) { roster.add(p); }

    @Override
    public String toString() { return name + " (" + sport.getName() + ")"; }
}
