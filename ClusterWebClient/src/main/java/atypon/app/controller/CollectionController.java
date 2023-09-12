package atypon.app.controller;

import atypon.app.service.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/collection")
public class CollectionController {
    private String database;
    private final CollectionService collectionService;
    @Autowired
    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }
    @GetMapping("/list")
    public String collectionList(@RequestParam(name = "databaseName") String databaseName, Model model) {
        this.database = databaseName;
        List<String> collections = collectionService.readAllCollections(databaseName);
        model.addAttribute("collections", collections);
        return "collections";
    }
    @PostMapping("/create")
    public String createCollection(@RequestParam("collectionName") String collectionName,
                                   @RequestParam("fieldName") List<String> fieldNames,
                                   @RequestParam("fieldType") List<String> fieldTypes) {
        collectionService.createCollection(database, collectionName, fieldNames, fieldTypes);
        return "redirect:/collection/list?databaseName=" + database;
    }
    @PostMapping("/delete")
    public String deleteCollection(@RequestParam("collectionName") String collectionName) {
        collectionService.deleteCollection(database, collectionName);
        return "redirect:/collection/list?databaseName=" + database;
    }
    @PostMapping("/update")
    public String updateCollection(
            @RequestParam("oldCollectionName") String oldCollectionName,
            @RequestParam("newCollectionName") String newCollectionName) {
        collectionService.updateCollection(oldCollectionName, newCollectionName, database);
        return "redirect:/collection/list?databaseName=" + database;
    }

}
