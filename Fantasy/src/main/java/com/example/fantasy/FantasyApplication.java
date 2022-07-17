package com.example.fantasy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@SpringBootApplication
public class FantasyApplication implements CommandLineRunner{

    public static void main(String[] args) {
        SpringApplication.run(FantasyApplication.class, args);
    }


    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... strings) throws Exception {
        // Start the draft
        Draft d = new Draft(jdbcTemplate);
        d.start();


    }

}
