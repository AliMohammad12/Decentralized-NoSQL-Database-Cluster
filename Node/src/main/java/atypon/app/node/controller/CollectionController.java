package atypon.app.node.controller;

import atypon.app.node.model.Collection;
import atypon.app.node.request.CollectionUpdateRequest;
import atypon.app.node.response.ValidatorResponse;
import atypon.app.node.schema.CollectionSchema;
import atypon.app.node.service.services.CollectionService;
import atypon.app.node.service.services.ValidatorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class CollectionController {
    private final CollectionService collectionService;
    private final ValidatorService validatorService;
    @Autowired
    public CollectionController(CollectionService collectionService, ValidatorService validatorService) {
        this.collectionService = collectionService;
        this.validatorService = validatorService;
    }
    @PostMapping(value = "/create-collection")
    public ResponseEntity<?> createCollection(@RequestBody CollectionSchema collectionSchema) throws JsonProcessingException {
        Collection collection = collectionSchema.getCollection();
        ValidatorResponse validatorResponse = validatorService.isCollectionExists(collection.getDatabase().getName(), collection.getName());
        if (validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validatorResponse.getMessage());
        }
        collectionService.createCollection(collectionSchema);
        return ResponseEntity.ok("Collection created successfully!");
    }
    @PostMapping(value = "/read-collection")
    public ResponseEntity<?> readCollection(@RequestBody Collection collection) {
        ValidatorResponse collectionValidator = validatorService.isCollectionExists(collection.getDatabase().getName(), collection.getName());
        if (!collectionValidator.isValid()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(collectionValidator.getMessage());
        }
        return ResponseEntity.ok(collectionService.readCollection(collection));
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
    @RequestMapping("/delete-collection")
    public ResponseEntity<?> deleteCollection(@RequestBody Collection collection) throws IOException {
        String databaseName = collection.getDatabase().getName();
        ValidatorResponse collectionValidator = validatorService.isCollectionExists(databaseName, collection.getName());
        if (!collectionValidator.isValid()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(collectionValidator.getMessage());
        }
        collectionService.deleteCollection(collection);
        return ResponseEntity.ok("Collection has been deleted successfully!");
    }
}
