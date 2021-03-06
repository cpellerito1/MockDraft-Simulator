package com.example.fantasy;

public class Team {
    private int id;

    private String name;

    public Roster roster = new Roster();

    public Team(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Team() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRoster(Roster roster) {
        this.roster = roster;
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
}
