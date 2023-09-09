package atypon.app.node.controller;

import atypon.app.node.model.Document;
import atypon.app.node.request.DocumentUpdateRequest;
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
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(collectionValidatorResponse.getMessage());
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
    @RequestMapping("/read")
    public ResponseEntity<?> readDocument(@RequestBody Document document) throws IOException {
        ValidatorResponse validatorResponse = validatorService.isDocumentExists(document.getDbName(), document.getCollectionName(), document.getId());
        if (!validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(validatorResponse.getMessage());
        }
        return ResponseEntity.ok(documentService.readDocument(document.getDbName(), document.getCollectionName(), document.getId()));
    }
    @PostMapping("/delete")
    public ResponseEntity<?> deleteDocumentBy(@RequestBody DocumentRequestByProperty request) throws IOException {
        if (!request.isBroadcast()) {
            ValidatorResponse documentValidatorResponse = validatorService.isDocumentRequestValid(request);
            if (!documentValidatorResponse.isValid()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(documentValidatorResponse.getMessage());
            }
        }
        documentService.deleteDocument(request);
        return ResponseEntity.ok("Document has been deleted successfully!");
    }

//    @PostMapping("/delete") // implement this to delete the document directly
//    public ResponseEntity<?> deleteDocument(@RequestBody DocumentRequestByProperty request) throws IOException {
//        if (!request.isBroadcast()) {
//            ValidatorResponse documentValidatorResponse = validatorService.isDocumentRequestValid(request);
//            if (!documentValidatorResponse.isValid()) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(documentValidatorResponse.getMessage());
//            }
//        }
//        // delete document where property = X
//        documentService.deleteDocument(request);
//        return ResponseEntity.ok("Document has been deleted successfully!");
//    }
//

    @PostMapping("/update")
    public ResponseEntity<?> updateDocument(@RequestBody DocumentUpdateRequest documentUpdateRequest) {
        String id = documentUpdateRequest.getId();
        JsonNode newDocumentNode = documentUpdateRequest.getDocument();
        // 1- check if id exists by using indexing
        /// -- wait a little

        documentService.updateDocument();
        return ResponseEntity.ok("Document has been deleted successfully!");
    }
}
