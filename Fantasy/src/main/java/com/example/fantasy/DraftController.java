package com.example.fantasy;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@Controller
public class DraftController {

    @ModelAttribute("allPlayers")
    public List<Player> getAllPlayers() {
        return Draft.allPlayers;
    }

    @GetMapping("/players")
    public String players(Model model) {
        return "players";
    }
}
