package atypon.app.node.controller;

import atypon.app.node.model.Document;
import atypon.app.node.request.DocumentUpdateRequest;
import atypon.app.node.response.APIResponse;
import atypon.app.node.response.ValidatorResponse;
import atypon.app.node.service.services.DocumentService;
import atypon.app.node.service.services.JsonService;
import atypon.app.node.service.services.ValidatorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class DocumentController {
    private final DocumentService documentService;
    private final ValidatorService validatorService;
    @Autowired
    public DocumentController(DocumentService documentService, ValidatorService validatorService) {
        this.documentService = documentService;
        this.validatorService = validatorService;
    }
    @RequestMapping("/create-document")
    public ResponseEntity<String> addDocument(@RequestBody JsonNode document) throws JsonProcessingException {
        String collectionName = document.get("CollectionName").asText();
        String databaseName = document.get("DatabaseName").asText();
        JsonNode documentData = document.get("data");

        ValidatorResponse collectionValidatorResponse = validatorService.isCollectionExists(databaseName, collectionName);
        if (!collectionValidatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(collectionValidatorResponse.getMessage());
        }
        ValidatorResponse documentValidatorResponse = validatorService.isDocumentValid(databaseName, collectionName, documentData);
        if (!documentValidatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(documentValidatorResponse.getMessage());
        }
        documentService.addDocument(databaseName, collectionName, documentData);
        return ResponseEntity.status(HttpStatus.OK).body("Document has been added successfully!");
    }
    @RequestMapping("/read-document")
    public ResponseEntity<?> readDocument(@RequestBody Document document) throws IOException {
        ValidatorResponse validatorResponse = validatorService.isDocumentExists(document.getDbName(), document.getCollectionName(), document.getId());
        if (!validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(validatorResponse.getMessage());
        }
        return ResponseEntity.ok(documentService.readDocument(document.getDbName(), document.getCollectionName(), document.getId()));
    }
    @RequestMapping("/delete-document")
    public ResponseEntity<?> deleteDocument(@RequestBody Document document) throws IOException {
        ValidatorResponse validatorResponse = validatorService.isDocumentExists(document.getDbName(), document.getCollectionName(), document.getId());
        if (!validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(validatorResponse.getMessage());
        }
        documentService.deleteDocument(document.getDbName(), document.getCollectionName(), document.getId());
        return ResponseEntity.ok("Document has been deleted successfully!");
    }
    @RequestMapping("/update-document")
    public ResponseEntity<?> updateDocument(@RequestBody DocumentUpdateRequest documentUpdateRequest) {
        String id = documentUpdateRequest.getId();
        JsonNode newDocumentNode = documentUpdateRequest.getDocument();
        // 1- check if id exists by using indexing
        /// -- wait a little

        documentService.updateDocument();
        return ResponseEntity.ok("Document has been deleted successfully!");
    }
}
