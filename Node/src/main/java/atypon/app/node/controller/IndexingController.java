package atypon.app.node.controller;

import atypon.app.node.indexing.IndexObject;
import atypon.app.node.response.ValidatorResponse;
import atypon.app.node.service.services.BroadcastService;
import atypon.app.node.service.services.IndexingService;
import atypon.app.node.service.services.ValidatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/indexing")
public class IndexingController {
    private final ValidatorService validatorService;
    private final IndexingService indexingService;
    @Autowired
    public IndexingController(ValidatorService validatorService,
                              IndexingService indexingService) {
        this.validatorService = validatorService;
        this.indexingService = indexingService;
    }
    @PostMapping("/create")
    public ResponseEntity<?> createIndexing(@RequestBody IndexObject indexObject) throws IOException {
        ValidatorResponse validatorResponse = validatorService.isIndexCreationAllowed(indexObject);
        if (!validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validatorResponse.getMessage());
        }
        indexingService.createIndexing(indexObject);
        return ResponseEntity.ok(validatorResponse.getMessage() +
                " Index for property '" + indexObject.getProperty() + "' has been created successfully!");
    }
    @PostMapping("/delete")
    public ResponseEntity<?> deleteIndexing(@RequestBody IndexObject indexObject) throws IOException {
        ValidatorResponse validatorResponse = validatorService.IsIndexDeletionAllowed(indexObject);
        if (!validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validatorResponse.getMessage());
        }
        indexingService.deleteIndexing(indexObject);
        return ResponseEntity.ok(validatorResponse.getMessage() +
                " Index for property '" + indexObject.getProperty() + "' has been deleted successfully!");
    }
}
