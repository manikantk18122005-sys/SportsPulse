package com.sportspulse.data;

import com.sportspulse.model.Sport;
import java.util.List;

/** Central registry of supported sports and their metadata (extensible). */
public final class Sports {
    public static final Sport FOOTBALL = new Sport(
            "Football",
            List.of("GOAL", "FOUL", "YELLOW_CARD", "RED_CARD", "CORNER", "OFFSIDE", "SUBSTITUTION"),
            List.of("GK", "DEF", "MID", "FWD"),
            11
    );

    public static final Sport BASKETBALL = new Sport(
            "Basketball",
            List.of("TWO_POINTER", "THREE_POINTER", "FOUL", "REBOUND", "ASSIST", "STEAL", "BLOCK", "TURNOVER"),
            List.of("PG", "SG", "SF", "PF", "C"),
            5
    );

    public static final Sport CRICKET = new Sport(
            "Cricket",
            List.of("RUN", "FOUR", "SIX", "WICKET", "WIDE", "NO_BALL", "MAIDEN_OVER"),
            List.of("BATSMAN", "BOWLER", "ALL_ROUNDER", "WICKET_KEEPER"),
            11
    );

    public static final List<Sport> ALL = List.of(FOOTBALL, BASKETBALL, CRICKET);

    private Sports() {}
}
