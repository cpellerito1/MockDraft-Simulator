package com.example.fantasy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Draft {
    private static final Logger log = LoggerFactory.getLogger(FantasyApplication.class);

    JdbcTemplate jdbcTemplate;

    // HashMap to keep track of every pick and who it was made by
    public static HashMap<Long, Team> draftHistory = new HashMap<>();
    // List of all the players in the database to be output to browser
    public static ArrayList<Player> allPlayers = new ArrayList<>();

    // Create the scanner for getting user input
    Scanner input = new Scanner(System.in);

    // Arraylist to store the teams from the database
    static ArrayList<Team> teams = new ArrayList<>();

    // Users Team object
    Team user;

    static int count = 0;

    public Draft(JdbcTemplate j) {
        this.jdbcTemplate = j;
    }

    /**
     * This method runs the draft. It starts by loading the database then outputting the link to the list of players
     * and getting the users team name. After that it begins the draft using a while loop. The while loop works by
     * keeping track of the round and the pick. Since there are only 12 teams in this draft, when the pick hits
     * 12 (11 in the code because it is being indexed from a list) the incrementer value gets changed from 1 to -1 to
     * accommodate the snaking nature of drafts. Then this process is repeated when the pick gets to 1 (0 because index)
     * except the increment value is changed from -1 to 1. Whenever the incrementer gets changed, this is the sign of a
     * new round so the round counter gets incremented.
     */
    public void start() {
        // Load the database
        load();
        // Give link to user to view players
        System.out.printf("%s%s%n", "To view the list of draft-able players, visit this link in a web browser: ",
                "http://localhost:8080/players");

        // Press enter to continue
        System.out.println("Press Enter to continue");
        try { input.nextLine(); } catch (Exception ignored) {}

//        // Get the users team name
//        System.out.print("Enter your team name: ");
//        String n = input.nextLine();
//        while (n.length() > 24) {
//            System.out.println("Error: Team name too long, must be 24 characters or less\n Please enter new name: ");
//            n = input.nextLine();
//        }
        // Get the teams from the database and add them to a list
        for (int i = 1; i < 12; i++) {
            var sql = "SELECT name FROM teams WHERE id=" + i;
            teams.add(new Team(i, jdbcTemplate.queryForObject(sql, String.class)));
        }

//        // Create the users team object and add it to the database and list
//        jdbcTemplate.update("INSERT INTO Teams(name) VALUES ?",  n);
//        user = new Team(jdbcTemplate.queryForObject("SELECT id FROM teams WHERE name='" + n + "'", int.class), n);
//        teams.add(user);

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
        while (r <= 27) {
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
        for (int i = 1; i < 5000; i++) {
            jdbcTemplate.update("INSERT INTO Players(name,pro_team, hand) VALUES (?, ?, ?)", "name" + i, "mets", 'r');
        }
        Random r = new Random();
        for (int i = 1; i < 5000; i++) {
            jdbcTemplate.update("INSERT INTO Player_Info(player_id, position) VALUES(?, ?)", i, poses[r.nextInt(8)]);
        }

        List<Map<String, Object>> tempPlayers = jdbcTemplate.queryForList("SELECT * FROM Players join Player_Info " +
                "where Players.id=Player_Info.player_id");

        tempPlayers.forEach((P)-> allPlayers.add(new Player(Long.parseLong(P.get("id").toString()),
                Long.parseLong(P.get("rank").toString()), P.get("name").toString(), P.get("pro_team").toString(),
                P.get("hand").toString().charAt(0), List.of(P.get("position").toString()))));
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
            System.out.print("Enter the id of the user you would like to draft: ");
            String userInput = "";
            boolean valid = false;
            while (!valid) {
                try {
                    userInput = input.nextLine();
                    long userDraft = Long.parseLong(userInput);
                    // Make sure it is a valid pick
                    if (!validPick(user, userDraft))
                        throw new Exception();
                } catch (Exception e) {
                    System.out.print("Please enter another id: ");
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
            setRoster(user, id);
            draftHistory.put(id, user);
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
            long player = 0;
            if (needs.isEmpty()) {
                player = jdbcTemplate.queryForObject("SELECT min(player_id) FROM Player_Info WHERE drafted=false",
                        Long.class);
            }
            else {
                // List to hold the ids of the best player at each position of need
                ArrayList<Long> players = new ArrayList<>();
                for (String n : needs) {
                    // Query for lowest ranked player
                    player = jdbcTemplate.queryForObject("SELECT min(player_id) FROM Player_Info WHERE " +
                            "drafted=false AND position='" + n + "'", Long.class);
                    players.add(player);
                }
                // Make the draft pick with the min id from the list of needs
                player = Collections.min(players);
            }

            jdbcTemplate.update("INSERT INTO Rosters(team_id, player_id) VALUES(?, ?)", t.getId(), player);
            jdbcTemplate.update("UPDATE Player_info SET drafted=true WHERE player_id=?", player);
            setRoster(t, player);
            draftHistory.put(player, t);
            System.out.println(t.getName() + " drafts player " + player);
        }
    }


    /**
     * This method checks to make sure the users draft pick is valid. It first checks to make sure the pick is a real
     * player. After that it checks if the users bench is full. If it is, get the users needs and the players positions
     * and check if the players position is a need for the team. If the player does not fill a need, the user is not
     * allowed to draft that player until all of their starting positions have been filled.
     * @param team team drafting
     * @param id id of the player they are trying to draft
     * @return true if valid, false if not
     */
    public boolean validPick(Team team, long id) {
        // Max value of the id column to make it easier to check if the id the user gives is a valid player
        long maxVal = jdbcTemplate.queryForObject("SELECT MAX(id) FROM Players", Long.class);
        if (id > maxVal || id <= 0) {
            System.out.println("Error: ID does not belong to a player");
            return false;
        }
        else if (new HashSet<>(jdbcTemplate.queryForList("SELECT player_id FROM Player_Info " +
                "WHERE drafted=true", Long.class)).contains(id)) {
            System.out.println("Error: Player already drafted");
            return false;
        }
        else if (team.roster.getBench().size() >= 7) {
            HashSet<String> needs = new HashSet<>(team.roster.needs());
            ArrayList<String> positions = new ArrayList<>(jdbcTemplate.queryForList("SELECT " +
                    "position FROM Player_Info WHERE player_id=" + id, String.class));

            if (needs.contains("util") && !(positions.contains("SP") || positions.contains("RP")))
                return true;
            else if (needs.contains("mid") && (positions.contains("SS") || positions.contains("2B")))
                return true;
            else if (needs.contains("corner") && (positions.contains("1B") || positions.contains("3B")))
                return true;

            for (String p: positions) {
                if (needs.contains(p))
                    return true;
            }

            System.out.println("Error: Selected player must fill a positional need");
            return false;
        }

        return true;
    }

    public void setRoster(Team team, Long id) {
        // Get the players position(s)
        List<String> pos = jdbcTemplate.queryForList("SELECT position FROM Player_Info " +
                "WHERE player_id=" + id, String.class);

        // Get the positions of need for team
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
