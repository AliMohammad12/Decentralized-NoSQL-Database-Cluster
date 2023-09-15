package atypon.app.controller;

import atypon.app.model.CollectionData;
import atypon.app.model.FieldInfo;
import atypon.app.service.CollectionService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/collection")
@SessionAttributes("database")
public class CollectionController {
    private final CollectionService collectionService;
    @Autowired
    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }
    @GetMapping("/list")
    public String collectionList(@RequestParam(name = "databaseName") String databaseName, Model model) {
        List<String> collections = collectionService.readAllCollections(databaseName);
        model.addAttribute("collections", collections);
        model.addAttribute("database", databaseName);
        return "collections";
    }
    @PostMapping("/create")
    public String createCollection(@RequestParam("collectionName") String collectionName,
                                   @RequestParam("fieldName") List<String> fieldNames,
                                   @RequestParam("fieldType") List<String> fieldTypes,
                                   @ModelAttribute("database") String databaseName,
                                   RedirectAttributes redirectAttributes) {
        String message = collectionService.createCollection(databaseName, collectionName, fieldNames, fieldTypes);
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/collection/list?databaseName=" + databaseName;
    }
    @PostMapping("/delete")
    public String deleteCollection(@RequestParam("collectionName") String collectionName,
                                   @ModelAttribute("database") String databaseName,
                                   RedirectAttributes redirectAttributes) {
        String message = collectionService.deleteCollection(databaseName, collectionName);
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/collection/list?databaseName=" + databaseName;
    }
    @PostMapping("/update")
    public String updateCollection(@RequestParam("oldCollectionName") String oldCollectionName,
                                   @RequestParam("newCollectionName") String newCollectionName,
                                   @ModelAttribute("database") String databaseName,
                                   RedirectAttributes redirectAttributes) {
        String message = collectionService.updateCollection(oldCollectionName, newCollectionName, databaseName);
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/collection/list?databaseName=" + databaseName;
    }
    @PostMapping("/view")
    public String viewCollection(@RequestParam("collectionName") String collectionName,
                                   @ModelAttribute("database") String databaseName,
                                 RedirectAttributes redirectAttributes,
                                 HttpSession session) {
        JsonNode jsonNode = collectionService.readFields(databaseName, collectionName);

        List<FieldInfo> fieldInfoList = new ArrayList<>();
        Iterator<String> fields = jsonNode.fieldNames();
        while (fields.hasNext()) {
            String fieldName = fields.next();
            String fieldType = jsonNode.get(fieldName).asText();
            fieldInfoList.add(new FieldInfo(fieldName, fieldType, false));
        }

        CollectionData collectionData = new CollectionData();
        collectionData.setDatabaseName(databaseName);
        collectionData.setCollectionName(collectionName);
        collectionData.setFieldInfoList(fieldInfoList);
        redirectAttributes.addFlashAttribute("collectionData", collectionData);
        session.setAttribute("collectionData", collectionData);

        return "redirect:/document/list";
    }
}
