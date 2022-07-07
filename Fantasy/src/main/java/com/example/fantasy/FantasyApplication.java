package com.example.fantasy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

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
        jdbcTemplate.execute("DROP TABLE Players, Team IF EXISTS");
        jdbcTemplate.execute(("CREATE TABLE Teams(id INT NOT NULL AUTO_INCREMENT, name VARCHAR(24), PRIMARY KEY(id))"));
        jdbcTemplate.execute("CREATE TABLE Players(id INT, rank INT, name VARCHAR(24), pro_team VARCHAR(24)," +
                "pos VARCHAR(24), hand CHAR, team_id INT, PRIMARY KEY(id), FOREIGN KEY(team_id) REFERENCES Teams(id))");

        log.info("Filling tables...");
        // Default teams
        for (int i = 0; i < 12; i++) {
            jdbcTemplate.update("INSERT INTO Teams(name) VALUES ?", "Team " + i);
        }

        // Players


        Draft d = new Draft(jdbcTemplate);
        d.start();


    }

}
