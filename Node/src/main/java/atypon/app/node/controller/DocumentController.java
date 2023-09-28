package atypon.app.node.controller;

import atypon.app.node.kafka.KafkaService;
import atypon.app.node.kafka.TopicType;
import atypon.app.node.kafka.event.document.CreateDocumentEvent;
import atypon.app.node.kafka.event.document.DeleteDocumentByIdEvent;
import atypon.app.node.kafka.event.document.DeleteDocumentsByPropertyEvent;
import atypon.app.node.kafka.event.document.UpdateDocumentEvent;
import atypon.app.node.locking.DistributedLocker;
import atypon.app.node.locking.LockExecutionResult;
import atypon.app.node.caching.RedisCachingService;
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
    private final DistributedLocker distributedLocker;
    private final RedisCachingService redisCachingService;
    @Autowired
    public DocumentController(DocumentService documentService,
                              ValidatorService validatorService,
                              KafkaService kafkaService,
                              DistributedLocker distributedLocker,
                              RedisCachingService redisCachingService) {
        this.documentService = documentService;
        this.validatorService = validatorService;
        this.kafkaService = kafkaService;
        this.distributedLocker = distributedLocker;
        this.redisCachingService = redisCachingService;
    }
    @PostMapping("/create")
    public ResponseEntity<String> createDocument(@RequestBody DocumentRequest request) {
        JsonNode document = request.getDocumentNode();
        String collectionName = document.get("CollectionName").asText();
        String databaseName = document.get("DatabaseName").asText();
        JsonNode documentData = document.get("data");
        String uniqueId = java.util.UUID.randomUUID().toString();
        JsonNode docToValidate = documentData.deepCopy();
        ObjectNode objectNode = (ObjectNode) documentData;
        objectNode.put("id", uniqueId);
        objectNode.put("version", 1);
        try {
            LockExecutionResult<?> result = distributedLocker.documentWriteLock(databaseName ,
                    collectionName, uniqueId, 10, 5, () -> {
                ValidatorResponse collectionValidatorResponse = validatorService.isCollectionExists(databaseName, collectionName);
                if (!collectionValidatorResponse.isValid()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(collectionValidatorResponse.getMessage());
                }
                ValidatorResponse documentValidatorResponse = validatorService.isDocumentValid(databaseName, collectionName, docToValidate);
                if (!documentValidatorResponse.isValid()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(documentValidatorResponse.getMessage());
                }
                kafkaService.broadCast(TopicType.Create_Document, new CreateDocumentEvent(request));
                return ResponseEntity.status(HttpStatus.OK).body(uniqueId);
            });
            return (ResponseEntity<String>) result.resultIfLockAcquired;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(e.getMessage() + ", Request timeout, Please try again!");
        }
    }
    @RequestMapping("/read-property")
    public ResponseEntity<?> readDocumentsByProperty(@RequestBody DocumentRequestByProperty request) {
        String database = request.getDatabase();
        String collection = request.getCollection();
        try {
            LockExecutionResult<?> result = distributedLocker.collectionReadLock(database, collection, 15, 10, () -> {
                ValidatorResponse documentValidatorResponse = validatorService.isDocumentRequestValid(request);
                if (!documentValidatorResponse.isValid()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(documentValidatorResponse.getMessage());
                }
                return ResponseEntity.ok(documentService.readDocumentProperty(request));
            });
            return (ResponseEntity<String>) result.resultIfLockAcquired;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(e.getMessage() + ", Request timeout, Please try again!");
        }
    }
    @PostMapping("/read-id")
    public ResponseEntity<?> readDocumentById(@RequestBody DocumentRequest request){
        JsonNode document = request.getDocumentNode();
        String collection = document.get("CollectionName").asText();
        String database = document.get("DatabaseName").asText();
        JsonNode documentData = document.get("data");
        String id = documentData.get("id").asText();
        if (redisCachingService.isCached(id)) {
            JsonNode cachedDocument = (JsonNode) redisCachingService.getCachedValue(id);
            redisCachingService.cache(id, cachedDocument, 60);
            return ResponseEntity.ok(cachedDocument);
        }
        try {
            LockExecutionResult<?> result = distributedLocker.documentReadLock(database ,
                    collection, id, 10, 5, () -> {
                ValidatorResponse validatorResponse = validatorService.isDocumentExists(database, collection, documentData);
                if (!validatorResponse.isValid()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validatorResponse.getMessage());
                }
                return ResponseEntity.ok( documentService.readDocumentById(database, collection, documentData));
            });
            return (ResponseEntity<?>) result.resultIfLockAcquired;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(e.getMessage() + ", Request timeout, Please try again!");
        }
    }
    @PostMapping("/delete-property")
    public ResponseEntity<?> deleteDocumentsByProperty(@RequestBody DocumentRequestByProperty request) {
        String database = request.getDatabase();
        String collection = request.getCollection();
        try {
            LockExecutionResult<?> result = distributedLocker.collectionWriteLock(database, collection, 15, 10, () -> {
                ValidatorResponse documentValidatorResponse = validatorService.isDocumentRequestValid(request);
                if (!documentValidatorResponse.isValid()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(documentValidatorResponse.getMessage());
                }
                kafkaService.broadCast(TopicType.Delete_Documents_ByProperty, new DeleteDocumentsByPropertyEvent(request));
                return ResponseEntity.ok("Documents have been deleted successfully!");
            });
            return (ResponseEntity<String>) result.resultIfLockAcquired;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(e.getMessage() + ", Request timeout, Please try again!");
        }
    }
    @RequestMapping("/delete-id")
    public ResponseEntity<?> deleteDocumentById(@RequestBody DocumentRequest request) {
        JsonNode document = request.getDocumentNode();
        String collection = document.get("CollectionName").asText();
        String database = document.get("DatabaseName").asText();
        JsonNode documentData = document.get("data");
        String id = documentData.get("id").asText();
        try {
            LockExecutionResult<?> result = distributedLocker.documentWriteLock(database,
                    collection, id, 10, 5, () -> {
                ValidatorResponse validatorResponse = validatorService.isDocumentExists(database, collection, documentData);
                if (!validatorResponse.isValid()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validatorResponse.getMessage());
                }
                kafkaService.broadCast(TopicType.Delete_Document_ById, new DeleteDocumentByIdEvent(request));
                return ResponseEntity.ok("Document has been deleted successfully!");
            });
            return (ResponseEntity<String>) result.resultIfLockAcquired;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(e.getMessage() + ", Request timeout, Please try again!");
        }
    }
    @PostMapping("/update")
    public ResponseEntity<?> updateDocument(@RequestBody DocumentUpdateRequest request) {
        JsonNode document = request.getUpdateRequest();
        String collection = document.get("CollectionName").asText();
        String database = document.get("DatabaseName").asText();
        JsonNode documentInfo = document.get("info");
        JsonNode documentData = document.get("data");
        String id = documentInfo.get("id").asText();
        try {
            LockExecutionResult<?> result = distributedLocker.documentWriteLock(database, collection, id, 10, 5, () -> {
                ValidatorResponse validatorResponse = validatorService.isDocumentUpdateRequestValid(database, collection, documentData, documentInfo);
                if (!validatorResponse.isValid()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validatorResponse.getMessage());
                }

                kafkaService.broadCast(TopicType.Update_Document,
                        new UpdateDocumentEvent(request));

                return ResponseEntity.ok("Document has been updated successfully!");
            });
            return (ResponseEntity<String>) result.resultIfLockAcquired;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(e.getMessage() + ", Request timeout, Please try again!");
        }
    }
}
