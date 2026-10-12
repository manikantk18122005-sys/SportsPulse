package com.sportspulse.data;

import com.sportspulse.model.*;

import java.util.*;

/** Builds a self-contained in-memory dataset spanning Football, Basketball and Cricket. */
public final class MockData {

    public final List<Player> allPlayers = new ArrayList<>();
    public final List<Team> allTeams = new ArrayList<>();
    public final List<Match> allMatches = new ArrayList<>();
    public final List<MatchReport> allReports = new ArrayList<>();
    public final List<Facility> allFacilities = new ArrayList<>();

    public MockData() {
        buildFootball();
        buildBasketball();
        buildCricket();
        buildFacilities();
    }

    private void buildFootball() {
        Team riverside = new Team("T-FB-01", "Riverside United", Sports.FOOTBALL);
        Team ironclad = new Team("T-FB-02", "Ironclad FC", Sports.FOOTBALL);

        String[][] riversidePlayers = {
                {"Arjun Mehta", "GK", "40", "72", "3"},
                {"Devesh Rao", "DEF", "55", "76", "5"},
                {"Kabir Singh", "DEF", "58", "74", "4"},
                {"Manikanta Reddy", "MID", "70", "81", "8"},
                {"Nikhil Verma", "MID", "68", "79", "7"},
                {"Farhan Ali", "FWD", "85", "88", "9"},
                {"Rohit Sharma", "FWD", "80", "84", "6"},
        };
        String[][] ironcladPlayers = {
                {"Karan Bhatt", "GK", "42", "70", "2"},
                {"Suresh Iyer", "DEF", "50", "73", "4"},
                {"Tarun Das", "DEF", "52", "72", "3"},
                {"Vivek Nair", "MID", "65", "77", "6"},
                {"Yusuf Khan", "MID", "62", "75", "5"},
                {"Zaid Ansari", "FWD", "78", "83", "8"},
                {"Aakash Pillai", "FWD", "75", "80", "7"},
        };
        addSquad(riverside, "P-FB", riversidePlayers);
        addSquad(ironclad, "P-FB", ironcladPlayers);
        allTeams.add(riverside);
        allTeams.add(ironclad);

        Match m = new Match("M-FB-101", riverside, ironclad);
        m.addEvent(new MatchEvent(3, "FOUL", "P-FB-08", "Suresh Iyer fouls Farhan Ali near the box"));
        m.addEvent(new MatchEvent(12, "GOAL", "P-FB-06", "Farhan Ali finishes a low cross to open the scoring"));
        m.addEvent(new MatchEvent(18, "YELLOW_CARD", "P-FB-10", "Vivek Nair booked for a late tackle"));
        m.addEvent(new MatchEvent(29, "CORNER", "P-FB-04", "Manikanta Reddy wins a corner on the right"));
        m.addEvent(new MatchEvent(35, "GOAL", "P-FB-12", "Zaid Ansari equalizes with a header from the corner"));
        m.addEvent(new MatchEvent(41, "OFFSIDE", "P-FB-07", "Rohit Sharma flagged offside chasing a through ball"));
        m.addEvent(new MatchEvent(52, "GOAL", "P-FB-06", "Farhan Ali completes his brace with a curling strike"));
        m.addEvent(new MatchEvent(60, "SUBSTITUTION", "P-FB-09", "Rohit Sharma replaced by a fresh forward"));
        m.addEvent(new MatchEvent(70, "FOUL", "P-FB-02", "Devesh Rao concedes a foul in midfield"));
        m.addEvent(new MatchEvent(77, "RED_CARD", "P-FB-13", "Aakash Pillai sent off for a second bookable offence"));
        m.addEvent(new MatchEvent(88, "GOAL", "P-FB-04", "Manikanta Reddy seals it with a stoppage-time strike"));
        allMatches.add(m);

        String report = "Riverside United produced a commanding display at home, with Farhan Ali the star of the show. "
                + "Farhan Ali finishes a low cross to open the scoring in the twelfth minute, and the crowd erupted. "
                + "Ironclad FC responded well as Zaid Ansari equalizes with a header from the corner just before the half hour. "
                + "The midfield battle was intense, and Manikanta Reddy wins a corner on the right that led directly to the goal. "
                + "Farhan Ali finishes a low cross to open the scoring remains the moment fans will remember, but it was "
                + "Manikanta Reddy who had the final say, sealing it with a stoppage-time strike deep into injury time. "
                + "Ironclad FC will rue the red card shown late on, reducing them to ten men at the worst possible moment. "
                + "Overall a thrilling end-to-end contest between Riverside United and Ironclad FC.";
        allReports.add(new MatchReport(m.getId(), "Riverside United 3-1 Ironclad FC: Farhan Ali brace seals derby win", report));
    }

    private void buildBasketball() {
        Team thunder = new Team("T-BB-01", "Coastal Thunder", Sports.BASKETBALL);
        Team hawks = new Team("T-BB-02", "Metro Hawks", Sports.BASKETBALL);

        String[][] thunderPlayers = {
                {"Aditya Kulkarni", "PG", "60", "78", "6"},
                {"Bhavesh Menon", "SG", "58", "76", "5"},
                {"Chirag Malhotra", "SF", "65", "80", "7"},
                {"Dhruv Kapoor", "PF", "70", "82", "8"},
                {"Eshan Trivedi", "C", "68", "81", "7"},
        };
        String[][] hawksPlayers = {
                {"Faisal Qureshi", "PG", "55", "75", "5"},
                {"Girish Shetty", "SG", "57", "74", "5"},
                {"Harshad Joshi", "SF", "62", "77", "6"},
                {"Imran Siddiqui", "PF", "66", "79", "7"},
                {"Jatin Chawla", "C", "64", "78", "6"},
        };
        addSquad(thunder, "P-BB", thunderPlayers);
        addSquad(hawks, "P-BB", hawksPlayers);
        allTeams.add(thunder);
        allTeams.add(hawks);

        Match m = new Match("M-BB-201", thunder, hawks);
        m.addEvent(new MatchEvent(2, "THREE_POINTER", "P-BB-03", "Chirag Malhotra opens with a three from the wing"));
        m.addEvent(new MatchEvent(5, "STEAL", "P-BB-06", "Faisal Qureshi picks the pocket at half-court"));
        m.addEvent(new MatchEvent(9, "TWO_POINTER", "P-BB-09", "Imran Siddiqui scores off a post move"));
        m.addEvent(new MatchEvent(14, "REBOUND", "P-BB-04", "Dhruv Kapoor grabs the offensive rebound"));
        m.addEvent(new MatchEvent(20, "THREE_POINTER", "P-BB-01", "Aditya Kulkarni drains a deep three at the buzzer"));
        m.addEvent(new MatchEvent(26, "FOUL", "P-BB-10", "Jatin Chawla picks up his third foul"));
        m.addEvent(new MatchEvent(31, "ASSIST", "P-BB-01", "Aditya Kulkarni finds Eshan Trivedi for an easy dunk"));
        m.addEvent(new MatchEvent(35, "BLOCK", "P-BB-05", "Eshan Trivedi rejects the shot at the rim"));
        m.addEvent(new MatchEvent(40, "TURNOVER", "P-BB-07", "Harshad Joshi loses the handle under pressure"));
        m.addEvent(new MatchEvent(44, "THREE_POINTER", "P-BB-03", "Chirag Malhotra hits another three to seal it"));
        allMatches.add(m);

        String report = "Coastal Thunder closed out a tight contest against Metro Hawks behind a hot shooting night. "
                + "Chirag Malhotra opens with a three from the wing to set the tone early in the first quarter. "
                + "Metro Hawks hung around thanks to Imran Siddiqui scores off a post move on back-to-back possessions. "
                + "The defining sequence came when Aditya Kulkarni finds Eshan Trivedi for an easy dunk, swinging momentum. "
                + "Late in the game, Chirag Malhotra hits another three to seal it, silencing the road crowd for good. "
                + "Eshan Trivedi rejects the shot at the rim was the defensive highlight of the night. "
                + "A statement win for Coastal Thunder over Metro Hawks heading into the playoffs.";
        allReports.add(new MatchReport(m.getId(), "Coastal Thunder outlasts Metro Hawks in shootout", report));
    }

    private void buildCricket() {
        Team titans = new Team("T-CK-01", "Bay Titans", Sports.CRICKET);
        Team strikers = new Team("T-CK-02", "Valley Strikers", Sports.CRICKET);

        String[][] titansPlayers = {
                {"Aryan Choudhary", "BATSMAN", "50", "77", "6"},
                {"Bikram Sen", "BATSMAN", "48", "75", "5"},
                {"Chetan Rathore", "ALL_ROUNDER", "60", "80", "7"},
                {"Dinesh Kumar", "BOWLER", "45", "74", "5"},
                {"Eshwar Pillai", "WICKET_KEEPER", "40", "72", "4"},
        };
        String[][] strikersPlayers = {
                {"Fahad Mirza", "BATSMAN", "52", "76", "6"},
                {"Gaurav Oberoi", "BATSMAN", "46", "73", "5"},
                {"Harinder Brar", "ALL_ROUNDER", "58", "78", "6"},
                {"Ishaan Dutta", "BOWLER", "47", "75", "5"},
                {"Jagdish Rana", "WICKET_KEEPER", "38", "70", "4"},
        };
        addSquad(titans, "P-CK", titansPlayers);
        addSquad(strikers, "P-CK", strikersPlayers);
        allTeams.add(titans);
        allTeams.add(strikers);

        Match m = new Match("M-CK-301", titans, strikers);
        m.addEvent(new MatchEvent(1, "FOUR", "P-CK-01", "Aryan Choudhary opens with a crisp cover drive for four"));
        m.addEvent(new MatchEvent(3, "SIX", "P-CK-01", "Aryan Choudhary launches one over long-on for six"));
        m.addEvent(new MatchEvent(6, "WICKET", "P-CK-09", "Ishaan Dutta strikes, cleaning up the opener"));
        m.addEvent(new MatchEvent(9, "WIDE", "P-CK-09", "Ishaan Dutta strays down leg for a wide"));
        m.addEvent(new MatchEvent(12, "RUN", "P-CK-03", "Chetan Rathore rotates strike with a quick single"));
        m.addEvent(new MatchEvent(15, "MAIDEN_OVER", "P-CK-08", "Harinder Brar bowls a tidy maiden over"));
        m.addEvent(new MatchEvent(18, "WICKET", "P-CK-08", "Harinder Brar strikes, Chetan Rathore caught behind"));
        m.addEvent(new MatchEvent(20, "SIX", "P-CK-05", "Eshwar Pillai finishes the innings with a huge six"));
        allMatches.add(m);

        String report = "Bay Titans posted a competitive total against Valley Strikers in a rain-shortened contest. "
                + "Aryan Choudhary opens with a crisp cover drive for four to get things moving in the first over. "
                + "Aryan Choudhary launches one over long-on for six shortly after, putting the bowlers on notice. "
                + "Valley Strikers hit back immediately as Ishaan Dutta strikes, cleaning up the opener with a full delivery. "
                + "The middle overs were tight, with Harinder Brar bowls a tidy maiden over to slow the scoring rate. "
                + "Harinder Brar strikes, Chetan Rathore caught behind, turned the innings in the bowling side's favor. "
                + "But Eshwar Pillai finishes the innings with a huge six, a late flourish that lifted the total. "
                + "An entertaining passage of play between Bay Titans and Valley Strikers.";
        allReports.add(new MatchReport(m.getId(), "Bay Titans vs Valley Strikers: late six lifts the total", report));
    }

    private final java.util.Map<String, Integer> idCounters = new java.util.HashMap<>();

    private void addSquad(Team team, String idPrefix, String[][] rows) {
        int counter = idCounters.getOrDefault(idPrefix, 0) + 1;
        for (String[] row : rows) {
            String id = String.format("%s-%02d", idPrefix, counter++);
            Player p = new Player(id, row[0], team.getSport(), row[1],
                    Double.parseDouble(row[2]), Double.parseDouble(row[3]), Double.parseDouble(row[4]));
            allPlayers.add(p);
            team.addPlayer(p);
        }
        idCounters.put(idPrefix, counter - 1);
    }

    private void buildFacilities() {
        allFacilities.add(new Facility("F-01", "Main Football Pitch", 22, "Football"));
        allFacilities.add(new Facility("F-02", "Indoor Basketball Court", 10, "Basketball"));
        allFacilities.add(new Facility("F-03", "Cricket Nets", 6, "Cricket"));
        allFacilities.add(new Facility("F-04", "Gymnasium", 15, "ANY"));
        allFacilities.add(new Facility("F-05", "Recovery & Physio Suite", 5, "ANY"));
        allFacilities.add(new Facility("F-06", "Secondary Turf Pitch", 12, "ANY"));
    }

    public Player findById(String id) {
        for (Player p : allPlayers) if (p.getId().equals(id)) return p;
        return null;
    }

    public List<Player> playersBySport(com.sportspulse.model.Sport sport) {
        List<Player> list = new ArrayList<>();
        for (Player p : allPlayers) if (p.getSport() == sport) list.add(p);
        return list;
    }
}
