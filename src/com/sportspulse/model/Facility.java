package com.sportspulse.model;

/** A training facility / slot with limited capacity, used by network-flow scheduling. */
public class Facility {
    private final String id;
    private final String name;
    private final int capacity;   // max players/teams it can serve concurrently
    private final String sportSpecialty; // "ANY" or a specific sport name

    public Facility(String id, String name, int capacity, String sportSpecialty) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.sportSpecialty = sportSpecialty;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    public String getSportSpecialty() { return sportSpecialty; }

    @Override
    public String toString() {
        return String.format("%-6s %-22s cap=%-3d specialty=%s", id, name, capacity, sportSpecialty);
    }
}
