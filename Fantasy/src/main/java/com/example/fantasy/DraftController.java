package com.example.fantasy;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.HashMap;
import java.util.List;

@Controller
public class DraftController {

    @ModelAttribute("undraftedPlayers")
    public List<Player> getPlayers() {
        return d.getUndraftedPlayers();
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
