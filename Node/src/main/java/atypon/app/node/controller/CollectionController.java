package atypon.app.node.controller;

import atypon.app.node.kafka.KafkaService;
import atypon.app.node.kafka.TopicType;
import atypon.app.node.kafka.event.collection.CreateCollectionEvent;
import atypon.app.node.kafka.event.collection.DeleteCollectionEvent;
import atypon.app.node.kafka.event.collection.UpdateCollectionEvent;
import atypon.app.node.model.Collection;
import atypon.app.node.request.collection.CollectionRequest;
import atypon.app.node.request.collection.CollectionUpdateRequest;
import atypon.app.node.request.collection.CreateCollectionRequest;
import atypon.app.node.response.ValidatorResponse;
import atypon.app.node.schema.CollectionSchema;
import atypon.app.node.service.services.CollectionService;
import atypon.app.node.service.services.ValidatorService;
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
    private final KafkaService kafkaService;
    @Autowired
    public CollectionController(CollectionService collectionService,
                                ValidatorService validatorService,
                                KafkaService kafkaService) {
        this.collectionService = collectionService;
        this.validatorService = validatorService;
        this.kafkaService = kafkaService;
    }
    @RequestMapping("/create")
    public ResponseEntity<?> createCollection(@RequestBody CreateCollectionRequest request)  {
        CollectionSchema collectionSchema = request.getCollectionSchema();
        Collection collection = collectionSchema.getCollection();
        ValidatorResponse validatorResponse = validatorService.isCollectionExists(collection.getDatabase().getName(), collection.getName());
        if (validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validatorResponse.getMessage());
        }
        kafkaService.broadCast(TopicType.Create_Collection, new CreateCollectionEvent(request));
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
    @PostMapping(value = "/read-fields")
    public ResponseEntity<?> readCollectionFields(@RequestBody Collection collection) throws IOException {
        ValidatorResponse collectionValidator = validatorService.isCollectionExists(collection.getDatabase().getName(), collection.getName());
        if (!collectionValidator.isValid()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(collectionValidator.getMessage());
        }
        return ResponseEntity.ok(collectionService.readCollectionFields(collection));
    }
    @PostMapping("/update")
    public ResponseEntity<?> updateCollection(@RequestBody CollectionUpdateRequest request) {
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
        kafkaService.broadCast(TopicType.Update_Collection, new UpdateCollectionEvent(request));
        return ResponseEntity.ok("Collection name has been updated successfully!");
    }
    @RequestMapping("/delete")  // todo: must clear the B+ Tree (Don't forget)
    public ResponseEntity<?> deleteCollection(@RequestBody CollectionRequest request)  {
        Collection collection = request.getCollection();
        String databaseName = collection.getDatabase().getName();
        ValidatorResponse collectionValidator = validatorService.isCollectionExists(databaseName, collection.getName());
        if (!collectionValidator.isValid()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(collectionValidator.getMessage());
        }
        kafkaService.broadCast(TopicType.Delete_Collection, new DeleteCollectionEvent(request));
        return ResponseEntity.ok("Collection has been deleted successfully!");
    }
}
