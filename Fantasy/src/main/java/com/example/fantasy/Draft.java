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

    static int count = 0;

    public Draft(JdbcTemplate jdbc) {
        this.jdbcTemplate = jdbc;
    }

    public void start() {
        // Get the users team name
        System.out.print("Enter your team name: ");
        String n = input.nextLine();
        while (n.length() > 24) {
            System.out.println("Error: Team name too long, must be 24 characters or less\n Please enter new name: ");
            n = input.nextLine();
        }
        // Get the teams from the database and add them to a list
        for (int i = 1; i < 12; i++) {
            var sql = "SELECT name FROM teams WHERE id=" + i;
            teams.add(new Team(i, jdbcTemplate.queryForObject(sql, String.class)));
        }

        // Create the users team object and add it to the database and list
        jdbcTemplate.update("INSERT INTO Teams(name) VALUES ?",  n);
        user = new Team(jdbcTemplate.queryForObject("SELECT id FROM teams WHERE name='" + n + "'", int.class), n);
        teams.add(user);

        // Start the draft
        // Randomize the order
        Collections.shuffle(teams);
        for (Team t: teams)
            System.out.println(t.getName());
        // Variables to keep track of the round, current pick, and direction of the increment for snake draft
        int r = 1, cur = 0, inc = 1;

        // Make first pick then start the while loop for the rest
        System.out.println("Draft starting");
        pick(teams.get(0));
        cur++;
        System.out.println("Round: " + r + " Pick: " + cur);
        // While loop that runs the draft for r rounds
        while (r <= 4) {
            System.out.println("Round: " + r + " Pick: " + cur);
            pick(teams.get(cur));
            cur += inc;
            if (cur == 12) {
                inc = -1;
                cur = 11;
                r++;
            } else if (cur == -1) {
                inc = 1;
                cur = 0;
                r++;
            }
        }
        System.out.println("The draft has ended: to see a summary of the picks go to this link in a web browser: ");
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
            // Max value of the id column to make it easier to check if the id the user gives is a valid player
            long maxVal = jdbcTemplate.queryForObject("SELECT MAX(id) FROM Players", Long.class);
            // Ask the user to make their pick and make sure the id entered is valid
            // ie is a long, is positive, has a player assigned to that id, and not already drafted
            System.out.print("Enter the id of the user you would like to draft: ");
            String userInput = "";
            boolean valid = false;
            while (!valid) {
                try {
                    userInput = input.nextLine();
                    long userDraft = Long.parseLong(userInput);
                    if (userDraft > maxVal || userDraft <= 0 || drafted.contains(userDraft))
                        throw new Exception();
                } catch (Exception e) {
                    System.out.print("Error: Invalid id, please enter another id: ");
                    continue;
                }
                valid = true;
            }
            System.out.println("Nice draft");
            // id of the player drafted
            long id = Long.parseLong(userInput);
            // Insert drafted player into Roster table
            jdbcTemplate.update("INSERT INTO Rosters(team_id, player_id) VALUES(?, ?)", user.getId(), id);
            jdbcTemplate.update(("UPDATE Player_Info SET drafted=true WHERE player_id=?"), id);
            drafted.add(id);
            setRoster(user, id);
            System.out.println("Congrats! You drafted Player: " + id);
        } else {
            // Set of needs for the auto drafting team
            HashSet<String> needs = new HashSet<>(t.roster.needs());
            // Temp Lists to hold positions
            ArrayList<String> temp = new ArrayList<>();
            for (Iterator<String> it = needs.iterator(); it.hasNext();) {
                String n = it.next();
                // Add positions if one of the needs is util, mid, or corner infielder
                switch (n) {
                    case "util" -> {
                        it.remove();
                        temp.addAll(List.of("1B", "2B", "3B", "SS", "C", "OF"));
                    }
                    case "mid" -> {
                        it.remove();
                        temp.addAll(List.of("2B", "SS"));
                    }
                    case "corner" -> {
                        it.remove();
                        temp.addAll(List.of("1B", "3B"));
                    }
                }
            }
            // Add positions back to hashset
            needs.addAll(temp);
            // List to hold the ids of the best player at each position of need
            ArrayList<Long> players = new ArrayList<>();
            for (String n : needs) {
                // Query for lowest ranked player
                long player = jdbcTemplate.queryForObject("SELECT min(player_id) FROM Player_Info Where " +
                        "drafted=false AND position='" + n + "'", Long.class);
                players.add(player);
            }

            // Make the draft pick with the min id from the list of needs
            long player = Collections.min(players);
            jdbcTemplate.update("INSERT INTO Rosters(team_id, player_id) VALUES(?, ?)", t.getId(), player);
            jdbcTemplate.update("UPDATE Player_info SET drafted=true WHERE player_id=?", player);
            drafted.add(player);
            setRoster(t, player);
            System.out.println(t.getName() + " drafts player " + player);
        }
    }

    public void setRoster(Team team, Long id) {
        // Get the players position(s)
        //List<String> pos = List.of(jdbcTemplate.queryForObject(
          //      "SELECT position FROM Player_Info WHERE player_id=" + id, String.class));
        //.split("/"));
        List<String> pos = List.of(jdbcTemplate.queryForObject("SELECT position FROM Player_Info " +
                "WHERE player_id=" + id, String.class));

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
