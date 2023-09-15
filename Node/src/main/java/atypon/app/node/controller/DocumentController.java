package atypon.app.node.controller;

import atypon.app.node.kafka.KafkaService;
import atypon.app.node.kafka.TopicType;
import atypon.app.node.kafka.event.document.CreateDocumentEvent;
import atypon.app.node.kafka.event.document.DeleteDocumentByIdEvent;
import atypon.app.node.kafka.event.document.DeleteDocumentsByPropertyEvent;
import atypon.app.node.kafka.event.document.UpdateDocumentEvent;
import atypon.app.node.request.document.DocumentUpdateRequest;
import atypon.app.node.request.document.DocumentRequest;
import atypon.app.node.request.document.DocumentRequestByProperty;
import atypon.app.node.response.ValidatorResponse;
import atypon.app.node.service.services.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    private final KafkaService kafkaService;
    @Autowired
    public DocumentController(DocumentService documentService,
                              ValidatorService validatorService,
                              KafkaService kafkaService) {
        this.documentService = documentService;
        this.validatorService = validatorService;
        this.kafkaService = kafkaService;
    }
    @PostMapping("/create")
    public ResponseEntity<String> createDocument(@RequestBody DocumentRequest request) {
        JsonNode document = request.getDocumentNode();
        String collectionName = document.get("CollectionName").asText();
        String databaseName = document.get("DatabaseName").asText();
        JsonNode documentData = document.get("data");
        ValidatorResponse collectionValidatorResponse = validatorService.isCollectionExists(databaseName, collectionName);
        if (!collectionValidatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(collectionValidatorResponse.getMessage());
        }
        ValidatorResponse documentValidatorResponse = validatorService.isDocumentValid(databaseName, collectionName, documentData);
        if (!documentValidatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(documentValidatorResponse.getMessage());
        }

        // todo: make file operations do this
        ObjectNode objectNode = (ObjectNode) documentData;
        String uniqueId = java.util.UUID.randomUUID().toString();
        objectNode.put("id", uniqueId);
        objectNode.put("version", 1);
        documentData = objectNode;

        kafkaService.broadCast(TopicType.Create_Document, new CreateDocumentEvent(request));
        return ResponseEntity.status(HttpStatus.OK).body(uniqueId);
    }
    @RequestMapping("/read-property") // Fully Okay
    public ResponseEntity<?> readDocumentsByProperty(@RequestBody DocumentRequestByProperty request) throws IOException {
        ValidatorResponse documentValidatorResponse = validatorService.isDocumentRequestValid(request);
        if (!documentValidatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(documentValidatorResponse.getMessage());
        }
        return ResponseEntity.ok(documentService.readDocumentProperty(request));
    }
    @PostMapping("/read-id") // Fully Ok
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
    public ResponseEntity<?> deleteDocumentsByProperty(@RequestBody DocumentRequestByProperty request) {
        ValidatorResponse documentValidatorResponse = validatorService.isDocumentRequestValid(request);
        if (!documentValidatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(documentValidatorResponse.getMessage());
        }
        kafkaService.broadCast(TopicType.Delete_Documents_ByProperty, new DeleteDocumentsByPropertyEvent(request));
        return ResponseEntity.ok("Document has been deleted successfully!");
    }

    @RequestMapping("/delete-id") // Fully Ok
    public ResponseEntity<?> deleteDocumentById(@RequestBody DocumentRequest request) {
        JsonNode document = request.getDocumentNode();
        String collection = document.get("CollectionName").asText();
        String database = document.get("DatabaseName").asText();
        JsonNode documentData = document.get("data");
        ValidatorResponse validatorResponse = validatorService.isDocumentExists(database, collection, documentData);
        if (!validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(validatorResponse.getMessage());
        }
        kafkaService.broadCast(TopicType.Delete_Document_ById, new DeleteDocumentByIdEvent(request));
        return ResponseEntity.ok("Document has been deleted successfully!");
    }
    @PostMapping("/update")
    public ResponseEntity<?> updateDocument(@RequestBody DocumentUpdateRequest request) {
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
        kafkaService.broadCast(TopicType.Update_Document, new UpdateDocumentEvent(request));
        return ResponseEntity.ok("Document has been updated successfully!");
    }

}
