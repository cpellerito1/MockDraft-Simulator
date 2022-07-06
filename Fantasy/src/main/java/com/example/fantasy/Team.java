package com.example.fantasy;

public class Team {
    private int id;

    private String name;

    private Roster roster;

    private int wins;
    private int losses;
    private int ties;

    public Team(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Roster getRoster() {
        return roster;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getTies() {
        return ties;
    }
}
