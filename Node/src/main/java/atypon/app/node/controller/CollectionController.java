package atypon.app.node.controller;

import atypon.app.node.model.Collection;
import atypon.app.node.request.collection.CollectionRequest;
import atypon.app.node.request.collection.CollectionUpdateRequest;
import atypon.app.node.request.collection.CreateCollectionRequest;
import atypon.app.node.response.ValidatorResponse;
import atypon.app.node.schema.CollectionSchema;
import atypon.app.node.service.services.BroadcastService;
import atypon.app.node.service.services.CollectionService;
import atypon.app.node.service.services.ValidatorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/collection")
public class CollectionController {
    private final CollectionService collectionService;
    private final ValidatorService validatorService;
    private final BroadcastService broadcastService;
    @Autowired
    public CollectionController(CollectionService collectionService,
                                ValidatorService validatorService,
                                BroadcastService broadcastingService) {
        this.collectionService = collectionService;
        this.validatorService = validatorService;
        this.broadcastService = broadcastingService;
    }
    @RequestMapping("/create")
    public ResponseEntity<?> createCollection(@RequestBody CreateCollectionRequest request) throws JsonProcessingException {
        CollectionSchema collectionSchema = request.getCollectionSchema();
        Collection collection = collectionSchema.getCollection();
        if (!request.isBroadcast()) {
            ValidatorResponse validatorResponse = validatorService.isCollectionExists(collection.getDatabase().getName(), collection.getName());
            if (validatorResponse.isValid()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validatorResponse.getMessage());
            }
        }
        collectionService.createCollection(collectionSchema);
        broadcastService.ProtectedBroadcast(request, "/collection/create");
        return ResponseEntity.ok("Collection created successfully!");
    }
    @PostMapping(value = "/read")
    public ResponseEntity<?> readCollection(@RequestBody Collection collection) {
        ValidatorResponse collectionValidator = validatorService.isCollectionExists(collection.getDatabase().getName(), collection.getName());
        if (!collectionValidator.isValid()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(collectionValidator.getMessage());
        }
        return ResponseEntity.ok(collectionService.readCollection(collection));
    }
    @PostMapping("/update")
    public ResponseEntity<?> updateCollection(@RequestBody CollectionUpdateRequest request) {
//        if (request.isBroadcast()) { todo: consider doing something like this to make it more efficient
//            collectionService.updateCollectionName(databaseName, oldCollectionName, newCollectionName);
//            return ResponseEntity.ok("Collection name has been updated successfully!");
//        }
        String oldCollectionName = request.getOldCollectionName();
        String newCollectionName = request.getNewCollectionName();
        String databaseName = request.getDatabaseName();
        if (!request.isBroadcast()) {
            ValidatorResponse oldCollectionValidator = validatorService.isCollectionExists(databaseName, oldCollectionName);
            if (!oldCollectionValidator.isValid()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(oldCollectionValidator.getMessage());
            }
            ValidatorResponse newCollectionValidator = validatorService.isCollectionExists(databaseName, newCollectionName);
            if (newCollectionValidator.isValid()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(newCollectionValidator.getMessage());
            }
        }
        collectionService.updateCollectionName(databaseName, oldCollectionName, newCollectionName);
        broadcastService.ProtectedBroadcast(request, "/collection/update");
        return ResponseEntity.ok("Collection name has been updated successfully!");
    }
    @RequestMapping("/delete")
    public ResponseEntity<?> deleteCollection(@RequestBody CollectionRequest request) throws IOException {
        Collection collection = request.getCollection();
        String databaseName = collection.getDatabase().getName();
        if (!request.isBroadcast()) {
            ValidatorResponse collectionValidator = validatorService.isCollectionExists(databaseName, collection.getName());
            if (!collectionValidator.isValid()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(collectionValidator.getMessage());
            }
        }
        collectionService.deleteCollection(collection);
        broadcastService.ProtectedBroadcast(request, "/collection/delete");
        return ResponseEntity.ok("Collection has been deleted successfully!");
    }
}
