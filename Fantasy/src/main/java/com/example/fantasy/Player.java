package com.example.fantasy;

import java.util.List;

public class Player {
    private long id;

    private long rank;

    private String name;

    private String proTeam;

    private List<String> position;

    private char hand;

    private int teamID;

    public Player(long id, long rank, String name, String proTeam, char hand, List<String> position) {
        this.id = id;
        this.rank = rank;
        this.name = name;
        this.proTeam = proTeam;
        this.hand = hand;
        this.position = position;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRank() {
        return rank;
    }

    public void setRank(long rank) {
        this.rank = rank;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProTeam() {
        return proTeam;
    }

    public void setProTeam(String proTeam) {
        this.proTeam = proTeam;
    }

    public List<String> getPosition() {
        return position;
    }

    public void setPosition(List<String> position) {
        this.position = position;
    }

    public char getHand() {
        return hand;
    }

    public void setHand(char hand) {
        this.hand = hand;
    }

    public int getTeamID() {
        return teamID;
    }

    public void setTeamID(int teamID) {
        this.teamID = teamID;
    }

}
