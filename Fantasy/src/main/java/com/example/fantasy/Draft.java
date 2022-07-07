package com.example.fantasy;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;


public class Draft {
    // Create the scanner for getting user input
    Scanner input = new Scanner(System.in);

    // jdbc template from the main class
    JdbcTemplate jdbcTemplate;

    // Arraylist to store the teams from the database
    static ArrayList<Team> teams = new ArrayList<>();

    // Users Team object
    Team user;

    long maxVal = jdbcTemplate.queryForObject("SELECT MAX(id) FROM Players", Long.class);

    public Draft(JdbcTemplate jdbc) {
        this.jdbcTemplate = jdbc;
    }

    public void start() {
        // Get the users team name
        System.out.println("Enter your team name: ");
        String n = input.nextLine();
        while (n.length() > 24) {
            System.out.println("Error: Team name too long, must be 24 characters or less\n Please enter new name: ");
            n = input.nextLine();
        }
        // Create the users team object and add it to the database
        user = new Team(n);
        jdbcTemplate.update("INSERT INTO Teams(name) VALUES ?",  n);

        // Get the teams from the database and add them to a list
        for (int i = 1; i < 13; i++) {
            var sql = "SELECT name FROM teams WHERE id=" + i;
            teams.add(new Team(jdbcTemplate.queryForObject(sql, String.class)));
        }

        // Start the draft
        // Randomize the order
        Collections.shuffle(teams);
        // Variables to keep track of the round, current pick, and direction of the increment for snake draft
        int r = 0, cur = 0, inc = 1;

        // Make first pick then start the while loop for the rest
        pick(teams.get(cur));
        cur++;
        // While loop that runs the draft for r rounds
        while (r <= 4) {
            pick(teams.get(cur));
            if (cur == 11) {
                inc = -1;
                r++;
            } else if (cur == 0) {
                inc = 1;
                r++;
            } else
                cur += inc;
        }
    }

    /**
     * This is the method that actually makes the pick and updates everything. After a player gets drafted,
     * their team_id column in the database gets updated, the id of the player is then added to the roster
     * of the team that drafted them. Then the player will be removed from the draft board.
     * @param t current team drafting
     */
    public void pick(Team t) {
        // First check if the team drafting is the users team
        if (t.equals(user)) {
            // Ask the user to make their pick and make sure their input is a long
            System.out.println("Enter the id of the user you would like to draft: ");
            while (!input.hasNextLong() || input.nextLong() > maxVal || input.nextLong() <= 0) {
                System.out.println("Error: invalid id, please enter another id: ");
                input.next();
            }

            // id of the player drafted
            long id = input.nextLong();
        }

    }
}
