package atypon.app.node.controller;

import atypon.app.node.model.Collection;
import atypon.app.node.model.Database;
import atypon.app.node.request.collection.CollectionRequest;
import atypon.app.node.request.collection.CollectionUpdateRequest;
import atypon.app.node.request.collection.CreateCollectionRequest;
import atypon.app.node.request.database.DatabaseRequest;
import atypon.app.node.request.database.DatabaseUpdateRequest;
import atypon.app.node.request.document.AddDocumentRequest;
import atypon.app.node.response.ValidatorResponse;
import atypon.app.node.schema.CollectionSchema;
import atypon.app.node.service.services.CollectionService;
import atypon.app.node.service.services.DatabaseService;
import atypon.app.node.service.services.DocumentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/broadcast")
public class BroadcastController {
    private final CollectionService collectionService;
    private final DatabaseService databaseService;
    private final DocumentService documentService;
    @Autowired
    public BroadcastController(CollectionService collectionService,
                               DatabaseService databaseService,
                               DocumentService documentService) {
        this.collectionService = collectionService;
        this.databaseService = databaseService;
        this.documentService = documentService;
    }
    @PostMapping("/collection/create")
    public ResponseEntity<?> createCollection(@RequestBody CreateCollectionRequest request) throws JsonProcessingException {
        collectionService.createCollection(request.getCollectionSchema());
        return ResponseEntity.ok("Collection created successfully!");
    }
    @PostMapping("/collection/update")
    public ResponseEntity<?> updateCollection(@RequestBody CollectionUpdateRequest request) {
        String oldCollectionName = request.getOldCollectionName();
        String newCollectionName = request.getNewCollectionName();
        String databaseName = request.getDatabaseName();
        collectionService.updateCollectionName(databaseName, oldCollectionName, newCollectionName);
        return ResponseEntity.ok("Collection name has been updated successfully!");
    }
    @PostMapping("/collection/delete")
    public ResponseEntity<?> deleteCollection(@RequestBody CollectionRequest request) throws IOException {
        collectionService.deleteCollection(request.getCollection());
        return ResponseEntity.ok("Collection has been deleted successfully!");
    }



    @PostMapping("/database/create")
    public ResponseEntity<?> createDatabase(@RequestBody DatabaseRequest databaseRequest) {
        databaseService.createDatabase(databaseRequest.getDatabase());
        return ResponseEntity.ok("Database created successfully!");
    }
    @PostMapping("/database/update")
    public ResponseEntity<?> updateDatabase(@RequestBody DatabaseUpdateRequest request) {
        String oldDatabaseName = request.getOldDatabaseName();
        String newDatabaseName = request.getNewDatabaseName();
        databaseService.updateDatabaseName(oldDatabaseName, newDatabaseName);
        return ResponseEntity.ok("Database name updated successfully!");
    }
    @PostMapping("/database/delete")
    public ResponseEntity<?> deleteDatabase(@RequestBody DatabaseRequest request) throws IOException {
        databaseService.deleteDatabase(request.getDatabase());
        return ResponseEntity.ok("Database deleted successfully!");
    }



    @PostMapping("/document/create")
    public ResponseEntity<String> addDocument(@RequestBody AddDocumentRequest request) throws JsonProcessingException {
        JsonNode document = request.getDocumentNode();
        String collectionName = document.get("CollectionName").asText();
        String databaseName = document.get("DatabaseName").asText();
        JsonNode documentData = document.get("data");
        documentService.addDocument(databaseName, collectionName, documentData);
        return ResponseEntity.status(HttpStatus.OK).body("Document has been added successfully!");
    }
    // delete doc + update later
}
