package com.example.fantasy;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashMap;
import java.util.List;

@Controller
public class DraftController {

    @GetMapping("/home")
    public String teamForm(Model model) {
        model.addAttribute("team", new Team());
        return "home";
    }

    @PostMapping("/home")
    public String addTeam(@ModelAttribute Team team, Model model) {
        model.addAttribute("team", team);
        Draft.teams.add(team);
        return "draft";
    }
    

    @ModelAttribute("allPlayers")
    public List<Player> getPlayers() {
        return Draft.allPlayers;
    }

    @ModelAttribute("draftHistory")
    public HashMap<Long, Team> getDraftHistory() {
        return Draft.draftHistory;
    }

    @GetMapping("/players")
    public String players(Model model) {
        return "players";
    }

    @GetMapping("/history")
    public String history(Model model) {
        return "history";
    }

}
