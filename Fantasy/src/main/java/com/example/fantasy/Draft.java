package com.example.fantasy;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;


public class Draft {
    // Create the scanner for getting user input
    Scanner input = new Scanner(System.in);

    // jdbc template from the main class
    JdbcTemplate jdbcTemplate;

    // Arraylist to store the teams from the database
    static ArrayList<Team> teams = new ArrayList<>();
    // Hashset to store id of drafted players
    HashSet<Long> drafted = new HashSet<>();

    // Users Team object
    Team user;

    // Max value of the id column to make it easier to check if the id the user gives is a valid player
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
            // Ask the user to make their pick and make sure the id entered is valid
            // ie is a long, is positive, has a player assigned to that id, and not already drafted
            System.out.println("Enter the id of the user you would like to draft: ");
            while (!input.hasNextLong() || input.nextLong() > maxVal ||
                    input.nextLong() <= 0 || drafted.contains(input.nextLong())) {
                System.out.println("Error: invalid id, please enter another id: ");
                input.next();
            }

            // id of the player drafted
            long id = input.nextLong();
            // Update the team_id column for the player drafted
            jdbcTemplate.update("UPDATE Players SET team_id=? WHERE id=?", user.getId(), id);
            drafted.add(id);
            setRoster(user, id);
        } else {
            // List of needs for the auto drafting team
            HashSet<String> needs = new HashSet<>(t.roster.needs());
            // List to hold the ids of the best player at each position of need
            ArrayList<Long> players = new ArrayList<>();
            for (String n: needs) {
                // Add positions if one of the needs is util, mid, or corner infielder
                switch (n) {
                    case "util" -> needs.addAll(List.of("1B", "2B", "3B", "SS", "C", "OF"));
                    case "mid" -> needs.addAll(List.of("2B", "SS"));
                    case "corner" -> needs.addAll(List.of("1B", "3B"));
                }
                // Query for lowest ranked player
                players.add(jdbcTemplate.queryForObject("SELECT min(id) FROM Players WHERE pos=" + n, Long.class));
            }
            
            // Make the draft pick with the min id from the list of needs
            setRoster(t, Collections.min(players));
        }
    }

    public void setRoster(Team team, Long id) {
        // Get the players position(s)
        List<String> pos = List.of(jdbcTemplate.queryForObject(
                "SELECT pos FROM Players WHERE id=" + id, String.class).split("/"));

        // Get the empty positions
        List<String> needs = team.roster.needs();
        // Check if any of the players positions fill one of the needs of the team
        for (String position: pos)
            if (needs.contains(position))
                team.roster.setPosition(position, id);
        // If they don't, check if they can be the utility player, mid, or corner infielder, if not add them to bench
        if (needs.contains("util"))
            team.roster.setUtil(id);
        else if (needs.contains("mid") && (pos.contains("SS") || pos.contains("2B")))
            team.roster.setMiddle(id);
        else if (needs.contains("corner") && (pos.contains("1B") || pos.contains("3B")))
            team.roster.setCorner(id);
        else
            team.roster.addBench(id);
    }
}
