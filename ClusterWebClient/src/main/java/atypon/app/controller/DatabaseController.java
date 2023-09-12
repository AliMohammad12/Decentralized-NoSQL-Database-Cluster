package atypon.app.controller;

import atypon.app.model.Database;
import atypon.app.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/database")
public class DatabaseController {
    private final DatabaseService databaseService;
    @Autowired
    public DatabaseController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }
    @GetMapping("/list")
    public String listDatabases(Model model) {
        List<Database> databases = databaseService.readAllDatabases();
        model.addAttribute("databases", databases);
        return "databases";
    }
    @PostMapping("/delete")
    public String deleteDatabase(@RequestParam String databaseName) {


        return "redirect:/database/list";
    }
    @PostMapping("/view")
    public String viewDatabase(@RequestParam String databaseName, Model model) {


        return "database_view";
    }
    @PostMapping("/create")
    public String createDatabase(@RequestParam String databaseName) {


        return "redirect:/database/list";
    }
}
