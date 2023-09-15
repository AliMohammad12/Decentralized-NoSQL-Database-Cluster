package atypon.app.controller;

import atypon.app.model.CollectionData;
import atypon.app.model.FieldInfo;
import atypon.app.model.IndexObject;
import atypon.app.model.UserInfo;
import atypon.app.service.CollectionService;
import atypon.app.service.DocumentService;
import atypon.app.service.IndexingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/document")
@SessionAttributes("collectionData")
public class DocumentController {
    private final CollectionService collectionService;
    private final DocumentService documentService;
    private final IndexingService indexingService;
    @Autowired
    public DocumentController(CollectionService collectionService,
                              DocumentService documentService,
                              IndexingService indexingService) {
        this.collectionService = collectionService;
        this.documentService = documentService;
        this.indexingService = indexingService;
    }
    @GetMapping("/list")
    public String documentList(@SessionAttribute("collectionData") CollectionData collectionData,
                               Model model) {
        String databaseName = collectionData.getDatabaseName();
        String collectionName = collectionData.getCollectionName();
        List<FieldInfo> fieldInfoList = collectionData.getFieldInfoList();
        ArrayNode data = collectionService.readCollection(databaseName, collectionName);
        List<Map<String, Object>> dataList = new ArrayList<>();

        for (JsonNode node : data) {
            Map<String, Object> dataMap = new HashMap<>();
            for (FieldInfo field : fieldInfoList) {
                dataMap.put(field.getFieldName(), node.get(field.getFieldName()).asText());
            }
            dataMap.put("id", node.get("id").asText());
            dataMap.put("version", node.get("version").asInt());
            dataList.add(dataMap);
        }

        for (FieldInfo fieldInfo : fieldInfoList) {
            IndexObject indexObject = new IndexObject(UserInfo.getUsername(),
                    databaseName, collectionName, fieldInfo.getFieldName());
            fieldInfo.setIndexed(indexingService.isIndexed(indexObject));
        }
        model.addAttribute("database", collectionData.getDatabaseName());
        model.addAttribute("fieldInfoList", fieldInfoList);
        model.addAttribute("data", dataList);
        model.addAttribute("collection", collectionName);

        return "documents";
    }
    @PostMapping("/update")
    public String updateDocument(@RequestParam("id") String id,
                                 @RequestParam("version") int version,
                                 @RequestParam Map<String, Object> newProperties,
                                 @SessionAttribute("collectionData") CollectionData collectionData,
                                 RedirectAttributes redirectAttributes) {
        String message = documentService.updateDocument(id, version, newProperties, collectionData);
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/document/list";
    }
    @PostMapping("/delete")
    public String deleteDocument(@RequestParam("id") String id,
                                 @SessionAttribute("collectionData") CollectionData collectionData,
                                 RedirectAttributes redirectAttributes) {
        String message = documentService.deleteDocumentById(id, collectionData);
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/document/list";
    }
}
