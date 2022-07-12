package com.example.fantasy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Random;

@SpringBootApplication
public class FantasyApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FantasyApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(FantasyApplication.class, args);
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... strings) throws Exception {

        log.info("Creating Tables...");

        // Create tables
        jdbcTemplate.execute("DROP TABLE Players, Team, Player_Info IF EXISTS");
        jdbcTemplate.execute(("CREATE TABLE Teams(id INT NOT NULL AUTO_INCREMENT, name VARCHAR(24), PRIMARY KEY(id))"));
        jdbcTemplate.execute("CREATE TABLE Players(id INT NOT NULL AUTO_INCREMENT, rank INT, name VARCHAR(24), " +
                "pro_team VARCHAR(24),pos VARCHAR(24), hand CHAR, team_id INT, drafted BIT DEFAULT 0, PRIMARY KEY(id), " +
                "FOREIGN KEY(team_id) REFERENCES Teams(id))");
        jdbcTemplate.execute("CREATE TABLE Player_Info(player_id INT, position VARCHAR(2), " +
                "FOREIGN KEY(player_id) REFERENCES Players(id))");

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
            jdbcTemplate.update("INSERT INTO Players(name) VALUES ?", "name" + i);
        }
        Random r = new Random();
        for (int i = 1; i < 800; i++) {
            jdbcTemplate.update("INSERT INTO Player_Info(player_id, position) VALUES(?, ?)", i, poses[r.nextInt(8)]);
        }

        //System.out.println(jdbcTemplate.queryForList("SELECT * FROM Player_Info"));

        Draft d = new Draft(jdbcTemplate);
        d.start();


    }

}
