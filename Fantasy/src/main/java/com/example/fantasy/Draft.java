package com.example.fantasy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Draft {
    private static final Logger log = LoggerFactory.getLogger(FantasyApplication.class);

    JdbcTemplate jdbcTemplate;

    public static List<Player> allPlayers = new ArrayList<>();

    // Create the scanner for getting user input
    Scanner input = new Scanner(System.in);

    // Arraylist to store the teams from the database
    static ArrayList<Team> teams = new ArrayList<>();
    // Hashset to store id of drafted players
    HashSet<Long> drafted = new HashSet<>();

    // Users Team object
    Team user;

    static int count = 0;

    public Draft(JdbcTemplate j) {
        this.jdbcTemplate = j;
    }

    public void load() {
        log.info("Creating Tables...");

        // Create tables
        jdbcTemplate.execute("DROP TABLE Players, Team, Player_Info, Roster IF EXISTS");
        jdbcTemplate.execute("CREATE TABLE Teams(id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(24))");
        jdbcTemplate.execute("CREATE TABLE Players(id INT PRIMARY KEY AUTO_INCREMENT, rank INT NOT NULL AUTO_INCREMENT," +
                " name VARCHAR(24), pro_team VARCHAR(24), hand CHAR)");
        jdbcTemplate.execute("CREATE TABLE Player_Info(player_id INT REFERENCES Players(id), position VARCHAR(2), " +
                "drafted Boolean DEFAULT false)");
        jdbcTemplate.execute("CREATE TABLE Rosters(team_id int REFERENCES Teams(id), " +
                "player_id int REFERENCES Players(id))");

        log.info("Filling tables...");
        // Default teams
        for (int i = 1; i < 12; i++) {
            jdbcTemplate.update("INSERT INTO Teams(name) VALUES ?", "Team " + i);
        }

        // Players
        // Test data
        int count = 0;
        String[] poses = new String[]{"OF", "1B", "2B", "3B", "SS", "C", "RP", "SP"};
        for (int i = 1; i < 800; i++) {
            jdbcTemplate.update("INSERT INTO Players(name,pro_team, hand) VALUES (?, ?, ?)", "name" + i, "mets", 'r');
        }
        Random r = new Random();
        for (int i = 1; i < 800; i++) {
            jdbcTemplate.update("INSERT INTO Player_Info(player_id, position) VALUES(?, ?)", i, poses[r.nextInt(8)]);
        }

        List<Map<String, Object>> tempPlayers = jdbcTemplate.queryForList("SELECT * FROM Players join Player_Info " +
                "where Players.id=Player_Info.player_id");
        tempPlayers.forEach((P)-> allPlayers.add(new Player(Long.parseLong(P.get("id").toString()),
                Long.parseLong(P.get("rank").toString()), P.get("name").toString(), P.get("pro_team").toString(),
                P.get("hand").toString().charAt(0), List.of(P.get("position").toString()))));
    }

    public void start() {
        // Load the database
        load();
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
