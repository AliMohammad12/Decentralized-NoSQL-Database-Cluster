package atypon.app.node.controller;

import atypon.app.node.kafka.KafkaService;
import atypon.app.node.kafka.TopicType;
import atypon.app.node.kafka.event.collection.CreateCollectionEvent;
import atypon.app.node.kafka.event.collection.DeleteCollectionEvent;
import atypon.app.node.kafka.event.collection.UpdateCollectionEvent;
import atypon.app.node.locking.DistributedLocker;
import atypon.app.node.locking.LockExecutionResult;
import atypon.app.node.caching.RedisCachingService;
import atypon.app.node.model.Collection;
import atypon.app.node.model.Database;
import atypon.app.node.request.collection.CollectionRequest;
import atypon.app.node.request.collection.CollectionUpdateRequest;
import atypon.app.node.request.collection.CreateCollectionRequest;
import atypon.app.node.response.ValidatorResponse;
import atypon.app.node.schema.CollectionSchema;
import atypon.app.node.service.services.CollectionService;
import atypon.app.node.service.services.ValidatorService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/collection")
public class CollectionController {
    private final CollectionService collectionService;
    private final ValidatorService validatorService;
    private final KafkaService kafkaService;
    private final DistributedLocker distributedLocker;
    private final RedisCachingService redisCachingService;
    @Autowired
    public CollectionController(CollectionService collectionService,
                                ValidatorService validatorService,
                                KafkaService kafkaService,
                                DistributedLocker distributedLocker,
                                RedisCachingService redisCachingService) {
        this.collectionService = collectionService;
        this.validatorService = validatorService;
        this.kafkaService = kafkaService;
        this.distributedLocker = distributedLocker;
        this.redisCachingService = redisCachingService;
    }
    @RequestMapping("/create")
    public ResponseEntity<?> createCollection(@RequestBody CreateCollectionRequest request)  {
        CollectionSchema collectionSchema = request.getCollectionSchema();
        Collection collection = collectionSchema.getCollection();
        Database database = collection.getDatabase();
        try {
            LockExecutionResult<?> result = distributedLocker.collectionWriteLock(database.getName() , collection.getName(), 10, 5, () -> {
                ValidatorResponse validatorResponse = validatorService.isCollectionExists(database.getName(), collection.getName());
                if (validatorResponse.isValid()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validatorResponse.getMessage());
                }
                kafkaService.broadCast(TopicType.Create_Collection, new CreateCollectionEvent(request));
                return ResponseEntity.ok("Collection created successfully!");
            });
            return (ResponseEntity<String>) result.resultIfLockAcquired;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(e.getMessage() + ", Request timeout, Please try again!");
        }
    }
    @PostMapping(value = "/read")
    public ResponseEntity<?> readCollection(@RequestBody Collection collection) {
        Database database = collection.getDatabase();
        String collectionCacheKey = database.getName()+"/"+collection.getName();
        if (redisCachingService.isCached(collectionCacheKey)) {
            Object cachedValue = redisCachingService.getCachedValue(collectionCacheKey);
            redisCachingService.cache(collectionCacheKey, cachedValue, 90);
            return ResponseEntity.ok(cachedValue);
        }
        try {
            LockExecutionResult<?> result = distributedLocker.collectionReadLock(database.getName(), collection.getName(), 10, 5, () -> {
                ValidatorResponse collectionValidator = validatorService.isCollectionExists(database.getName(), collection.getName());
                if (!collectionValidator.isValid()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(collectionValidator.getMessage());
                }
                return ResponseEntity.ok(collectionService.readCollection(collection));
            });
            return (ResponseEntity<?>)result.resultIfLockAcquired;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(e.getMessage() + ", Request timeout, Please try again!");
        }
    }
    @PostMapping(value = "/read-fields")
    public ResponseEntity<?> readCollectionFields(@RequestBody Collection collection)  {
        Database database = collection.getDatabase();
        String fieldsCacheKey = database.getName() +"/fields/"+collection.getName();
        if (redisCachingService.isCached(fieldsCacheKey)) {
            Object cachedValue = redisCachingService.getCachedValue(fieldsCacheKey);
            redisCachingService.cache(fieldsCacheKey, cachedValue, 90);
            return ResponseEntity.ok(cachedValue);
        }
        try {
            LockExecutionResult<?> result = distributedLocker.collectionReadLock(database.getName(), collection.getName(), 10, 5, () -> {
                ValidatorResponse collectionValidator = validatorService.isCollectionExists(database.getName(), collection.getName());
                if (!collectionValidator.isValid()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(collectionValidator.getMessage());
                }
                return ResponseEntity.ok(collectionService.readCollectionFields(collection));
            });
            return (ResponseEntity<?>)result.resultIfLockAcquired;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(e.getMessage() + ", Request timeout, Please try again!");
        }
    }
    @PostMapping("/update")
    public ResponseEntity<?> updateCollection(@RequestBody CollectionUpdateRequest request) {
        String oldCollectionName = request.getOldCollectionName();
        String newCollectionName = request.getNewCollectionName();
        String databaseName = request.getDatabaseName();
        try {
            LockExecutionResult<?> result = distributedLocker.collectionWriteLock(databaseName, oldCollectionName, 10, 5, () -> {
                ValidatorResponse oldCollectionValidator = validatorService.isCollectionExists(databaseName, oldCollectionName);
                if (!oldCollectionValidator.isValid()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(oldCollectionValidator.getMessage());
                }
                ValidatorResponse newCollectionValidator = validatorService.isCollectionExists(databaseName, newCollectionName);
                if (newCollectionValidator.isValid()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(newCollectionValidator.getMessage());
                }
                kafkaService.broadCast(TopicType.Update_Collection, new UpdateCollectionEvent(request));
                return ResponseEntity.ok("Collection name has been updated successfully!");
            });
            return (ResponseEntity<String>) result.resultIfLockAcquired;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(e.getMessage() + ", Request timeout, Please try again!");
        }
    }
    @RequestMapping("/delete")
    public ResponseEntity<?> deleteCollection(@RequestBody CollectionRequest request)  {
        Collection collection = request.getCollection();
        String databaseName = collection.getDatabase().getName();
        try {
            LockExecutionResult<?> result = distributedLocker.collectionWriteLock(databaseName, collection.getName(), 10, 5, () -> {
                ValidatorResponse collectionValidator = validatorService.isCollectionExists(databaseName, collection.getName());
                if (!collectionValidator.isValid()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(collectionValidator.getMessage());
                }
                kafkaService.broadCast(TopicType.Delete_Collection, new DeleteCollectionEvent(request));
                return ResponseEntity.ok("Collection has been deleted successfully!");
            });
            return (ResponseEntity<String>) result.resultIfLockAcquired;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(e.getMessage() + ", Request timeout, Please try again!");
        }
    }
}
