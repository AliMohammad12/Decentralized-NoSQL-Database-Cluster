package atypon.app.controller;

import atypon.app.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/database")
public class DatabaseController {
    private final DatabaseService databaseService;
    @Autowired
    public DatabaseController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }
    @GetMapping("/list")
    public String databaseList(Model model) {
        List<String> databases = databaseService.readAllDatabases();
        model.addAttribute("databases", databases);
        return "databases";
    }
    @PostMapping("/view")
    public String viewDatabase(@RequestParam String databaseName,
                               RedirectAttributes redirectAttributes) {
        redirectAttributes.addAttribute("databaseName", databaseName);
        return "redirect:/collection/list";
    }
    @PostMapping("/delete")
    public String deleteDatabase(@RequestParam String databaseName) throws Exception {
        databaseService.deleteDatabase(databaseName);
        return "redirect:/database/list";
    }
    @PostMapping("/create")
    public String createDatabase(@RequestParam("databaseName") String databaseName) {
        databaseService.createDatabase(databaseName);
        return "redirect:/database/list";
    }
    @PostMapping("/update")
    public String updateDatabase(
            @RequestParam("oldDatabaseName") String oldDatabaseName,
            @RequestParam("newDatabaseName") String newDatabaseName){
        databaseService.updateDatabase(oldDatabaseName, newDatabaseName);
        return "redirect:/database/list";
    }
}
