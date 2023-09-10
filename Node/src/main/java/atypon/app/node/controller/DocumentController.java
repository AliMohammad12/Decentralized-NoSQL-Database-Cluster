package atypon.app.node.controller;

import atypon.app.node.request.document.DocumentUpdateRequest;
import atypon.app.node.request.document.DocumentRequest;
import atypon.app.node.request.document.DocumentRequestByProperty;
import atypon.app.node.response.ValidatorResponse;
import atypon.app.node.service.services.*;
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
@RequestMapping("/document")
public class DocumentController {
    private final DocumentService documentService;
    private final ValidatorService validatorService;
    private final BroadcastService broadcastService;
    @Autowired
    public DocumentController(DocumentService documentService,
                              ValidatorService validatorService,
                              BroadcastService broadcastService) {
        this.documentService = documentService;
        this.validatorService = validatorService;
        this.broadcastService = broadcastService;
    }
    @PostMapping("/create")
    public ResponseEntity<String> createDocument(@RequestBody DocumentRequest request) throws JsonProcessingException {
        JsonNode document = request.getDocumentNode();
        String collectionName = document.get("CollectionName").asText();
        String databaseName = document.get("DatabaseName").asText();
        JsonNode documentData = document.get("data");
        if (!request.isBroadcast()) {
            ValidatorResponse collectionValidatorResponse = validatorService.isCollectionExists(databaseName, collectionName);
            if (!collectionValidatorResponse.isValid()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(collectionValidatorResponse.getMessage());
            }
            ValidatorResponse documentValidatorResponse = validatorService.isDocumentValid(databaseName, collectionName, documentData);
            if (!documentValidatorResponse.isValid()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(documentValidatorResponse.getMessage());
            }
        }
        documentService.addDocument(databaseName, collectionName, documentData);
      //  broadcastService.ProtectedBroadcast(request, "/document/create");
        return ResponseEntity.status(HttpStatus.OK).body("Document has been added successfully!");
    }
    @RequestMapping("/read-property") // Fully Okay
    public ResponseEntity<?> readDocumentsByProperty(@RequestBody DocumentRequestByProperty request) throws IOException {
        if (!request.isBroadcast()) {
            ValidatorResponse documentValidatorResponse = validatorService.isDocumentRequestValid(request);
            if (!documentValidatorResponse.isValid()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(documentValidatorResponse.getMessage());
            }
        }
        return ResponseEntity.ok(documentService.readDocumentProperty(request));
    }
    @RequestMapping("/read-id") // Fully Ok
    public ResponseEntity<?> readDocumentById(@RequestBody DocumentRequest request) throws IOException {
        JsonNode document = request.getDocumentNode();
        String collection = document.get("CollectionName").asText();
        String database = document.get("DatabaseName").asText();
        JsonNode documentData = document.get("data");
        ValidatorResponse validatorResponse = validatorService.isDocumentExists(database, collection, documentData);
        if (!validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(validatorResponse.getMessage());
        }
        return ResponseEntity.ok(documentService.readDocumentById(database, collection, documentData));
    }
    @PostMapping("/delete-property") // Fully Okay!
    public ResponseEntity<?> deleteDocumentsByProperty(@RequestBody DocumentRequestByProperty request) throws IOException {
        if (!request.isBroadcast()) {
            ValidatorResponse documentValidatorResponse = validatorService.isDocumentRequestValid(request);
            if (!documentValidatorResponse.isValid()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(documentValidatorResponse.getMessage());
            }
        }
        documentService.deleteDocumentByProperty(request);
        return ResponseEntity.ok("Document has been deleted successfully!");
    }
    @RequestMapping("/delete-id") // Fully Ok
    public ResponseEntity<?> deleteDocumentById(@RequestBody DocumentRequest request) throws IOException {
        JsonNode document = request.getDocumentNode();
        String collection = document.get("CollectionName").asText();
        String database = document.get("DatabaseName").asText();
        JsonNode documentData = document.get("data");
        ValidatorResponse validatorResponse = validatorService.isDocumentExists(database, collection, documentData);
        if (!validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(validatorResponse.getMessage());
        }
        documentService.deleteDocumentById(database, collection, documentData);
        return ResponseEntity.ok("Document has been deleted successfully!");
    }
    @PostMapping("/update")
    public ResponseEntity<?> updateDocument(@RequestBody DocumentUpdateRequest request) throws IOException {
        JsonNode document = request.getUpdateRequest();
        String collection = document.get("CollectionName").asText();
        String database = document.get("DatabaseName").asText();
        JsonNode documentInfo = document.get("info");
        JsonNode documentData = document.get("data");
        ValidatorResponse validatorResponse = validatorService
                .isDocumentUpdateRequestValid(database, collection, documentData, documentInfo);
        if (!validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validatorResponse.getMessage());
        }
        documentService.updateDocument(request);
        return ResponseEntity.ok("Document has been updated successfully!");
    }
}
