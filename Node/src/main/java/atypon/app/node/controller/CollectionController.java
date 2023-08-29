package atypon.app.node.controller;

import atypon.app.node.request.CollectionUpdateRequest;
import atypon.app.node.response.APIResponse;
import atypon.app.node.response.ValidatorResponse;
import atypon.app.node.schema.CollectionSchema;
import atypon.app.node.service.services.CollectionService;
import atypon.app.node.service.services.ValidatorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class CollectionController {
    private final CollectionService collectionService;
    private final ValidatorService validatorService;
    @Autowired
    public CollectionController(CollectionService collectionService, ValidatorService validatorService) {
        this.collectionService = collectionService;
        this.validatorService = validatorService;
    }
    @RequestMapping("/create-collection")
    public ResponseEntity<?> createCollection(@RequestBody CollectionSchema collectionSchema) throws JsonProcessingException {
        String collectionName = collectionSchema.getCollectionName();
        String databaseName = collectionSchema.getDatabaseName();
        ValidatorResponse validatorResponse = validatorService.isCollectionExists(databaseName, collectionName);
        if (validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.FOUND).body(validatorResponse.getMessage());
        }
        collectionService.createCollection(collectionSchema);
        return ResponseEntity.ok("Collection created successfully!");
    }
    @RequestMapping("/read-collection") // maybe make this method take collection Class instead
    public ResponseEntity<?> readCollection(@RequestBody String databaseName, @RequestBody String collectionName) throws IOException {
        ValidatorResponse collectionValidator = validatorService.isCollectionExists(databaseName, collectionName);
        if (!collectionValidator.isValid()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(collectionValidator.getMessage());
        }
        return ResponseEntity.ok(collectionService.readCollection(databaseName, collectionName));
    }
    @RequestMapping("/update-collection")
    public ResponseEntity<?> updateCollection(@RequestBody CollectionUpdateRequest request)  {
        String oldCollectionName = request.getOldCollectionName();
        String newCollectionName = request.getNewCollectionName();
        String databaseName = request.getDatabaseName();

        ValidatorResponse oldCollectionValidator = validatorService.isCollectionExists(databaseName, oldCollectionName);
        if (!oldCollectionValidator.isValid()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(oldCollectionValidator.getMessage());
        }

        ValidatorResponse newCollectionValidator = validatorService.isCollectionExists(databaseName, newCollectionName);
        if (newCollectionValidator.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(newCollectionValidator.getMessage());
        }

        collectionService.updateCollectionName(databaseName, oldCollectionName, newCollectionName);
        return ResponseEntity.ok("Collection name has been updated successfully!");
    }
    @RequestMapping("/delete-collection") // maybe make this method take collection Class instead
    public ResponseEntity<?> deleteCollection(@RequestBody String databaseName, @RequestBody String collectionName) throws IOException {
        ValidatorResponse collectionValidator = validatorService.isCollectionExists(databaseName, collectionName);
        if (!collectionValidator.isValid()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(collectionValidator.getMessage());
        }
        collectionService.deleteCollection(databaseName, collectionName);
        return ResponseEntity.ok("Collection has been deleted successfully!");
    }
}
