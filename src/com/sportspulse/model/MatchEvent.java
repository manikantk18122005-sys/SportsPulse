package com.sportspulse.model;

import com.sportspulse.util.Ansi;

import java.util.Set;

/** A single timestamped event in a live match stream (goal, foul, wicket, point, ...). */
public class MatchEvent {
    private final int minute;
    private final String type;      // must be one of Sport.eventTypes
    private final String playerId;
    private final String description;

    private static final Set<String> POSITIVE = Set.of(
            "GOAL", "THREE_POINTER", "TWO_POINTER", "DUNK", "ASSIST", "STEAL", "BLOCK",
            "FOUR", "SIX", "WICKET", "MAIDEN_OVER", "REBOUND");
    private static final Set<String> NEGATIVE = Set.of(
            "RED_CARD", "FOUL", "TURNOVER", "OFFSIDE");
    private static final Set<String> CAUTION = Set.of(
            "YELLOW_CARD", "WIDE", "NO_BALL");

    public MatchEvent(int minute, String type, String playerId, String description) {
        this.minute = minute;
        this.type = type;
        this.playerId = playerId;
        this.description = description;
    }

    public int getMinute() { return minute; }
    public String getType() { return type; }
    public String getPlayerId() { return playerId; }
    public String getDescription() { return description; }

    private String typeColor() {
        if (POSITIVE.contains(type)) return Ansi.BRIGHT_GREEN;
        if (NEGATIVE.contains(type)) return Ansi.BRIGHT_RED;
        if (CAUTION.contains(type)) return Ansi.BRIGHT_YELLOW;
        return Ansi.BRIGHT_CYAN;
    }

    @Override
    public String toString() {
        String coloredType = Ansi.paint(typeColor() + com.sportspulse.util.Ansi.BOLD, String.format("%-14s", type));
        return String.format("[%s%3d'%s] %s %-10s %s",
                Ansi.DIM, minute, Ansi.RESET, coloredType, playerId, description);
    }
}
