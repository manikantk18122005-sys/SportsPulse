package com.sportspulse.model;

import java.util.List;

/**
 * Describes a sport's metadata: scoring model + valid event types.
 * New sports can be added here without touching engine code (open/closed).
 */
public class Sport {
    private final String name;
    private final List<String> eventTypes;   // e.g. GOAL, FOUL, WICKET, THREE_POINTER
    private final List<String> positions;    // valid player positions for this sport
    private final int squadSize;             // players needed on a starting roster

    public Sport(String name, List<String> eventTypes, List<String> positions, int squadSize) {
        this.name = name;
        this.eventTypes = eventTypes;
        this.positions = positions;
        this.squadSize = squadSize;
    }

    public String getName() { return name; }
    public List<String> getEventTypes() { return eventTypes; }
    public List<String> getPositions() { return positions; }
    public int getSquadSize() { return squadSize; }

    @Override
    public String toString() { return name; }
}
