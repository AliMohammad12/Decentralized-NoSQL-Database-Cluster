package atypon.app.node.controller;

import atypon.app.node.indexing.IndexObject;
import atypon.app.node.indexing.bplustree.BPlusTree;
import atypon.app.node.model.User;
import atypon.app.node.request.UserRequest;
import atypon.app.node.response.ValidatorResponse;
import atypon.app.node.service.services.IndexingService;
import atypon.app.node.service.services.JsonService;
import atypon.app.node.service.services.ValidatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;

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
        indexingService.indexingInitializer();

        ValidatorResponse validatorResponse = validatorService.isIndexValid(indexObject);
        if (!validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validatorResponse.getMessage());
        }
        //indexingService.createIndexing(indexObject);
        return ResponseEntity.ok(validatorResponse.getMessage());
    }
}
