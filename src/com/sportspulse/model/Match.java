package com.sportspulse.model;

import java.util.ArrayList;
import java.util.List;

public class Match {
    private final String id;
    private final Team home;
    private final Team away;
    private final Sport sport;
    private final List<MatchEvent> events = new ArrayList<>();

    public Match(String id, Team home, Team away) {
        this.id = id;
        this.home = home;
        this.away = away;
        this.sport = home.getSport();
    }

    public String getId() { return id; }
    public Team getHome() { return home; }
    public Team getAway() { return away; }
    public Sport getSport() { return sport; }
    public List<MatchEvent> getEvents() { return events; }
    public void addEvent(MatchEvent e) { events.add(e); }

    @Override
    public String toString() {
        return id + ": " + home.getName() + " vs " + away.getName() + " (" + sport.getName() + ")";
    }
}
