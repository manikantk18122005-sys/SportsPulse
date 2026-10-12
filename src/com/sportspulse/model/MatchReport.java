package com.sportspulse.model;

/** A written match report / commentary transcript, used by the text-indexing subsystem. */
public class MatchReport {
    private final String matchId;
    private final String title;
    private final String body; // full commentary text

    public MatchReport(String matchId, String title, String body) {
        this.matchId = matchId;
        this.title = title;
        this.body = body;
    }

    public String getMatchId() { return matchId; }
    public String getTitle() { return title; }
    public String getBody() { return body; }

    @Override
    public String toString() { return matchId + " - " + title + " (" + body.length() + " chars)"; }
}
