package atypon.app.controller;

import atypon.app.model.CollectionData;
import atypon.app.model.FieldInfo;
import atypon.app.model.IndexObject;
import atypon.app.model.UserInfo;
import atypon.app.service.IndexingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/indexing")
@SessionAttributes("collectionData")
public class IndexingController {
    private final IndexingService indexingService;
    public IndexingController(IndexingService indexingService) {
        this.indexingService = indexingService;
    }
    @PostMapping("/create")
    public String createIndexing(@RequestParam("fieldName") String property,
                                 @SessionAttribute("collectionData") CollectionData collectionData) {
        IndexObject indexObject = new IndexObject(UserInfo.getUsername(),
                collectionData.getDatabaseName(), collectionData.getCollectionName(),
                property);
        indexingService.createIndexing(indexObject);
        return "redirect:/document/list";
    }
    @PostMapping("/delete")
    public String deleteIndexing(@RequestParam("fieldName") String property,
                                 @SessionAttribute("collectionData") CollectionData collectionData) {
        IndexObject indexObject = new IndexObject(UserInfo.getUsername(),
                collectionData.getDatabaseName(), collectionData.getCollectionName(),
                property);
        indexingService.deleteIndexing(indexObject);
        return "redirect:/document/list";
    }
}
