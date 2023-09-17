package atypon.app.node.controller;

import atypon.app.node.kafka.KafkaService;
import atypon.app.node.kafka.TopicType;
import atypon.app.node.kafka.event.database.CreateDatabaseEvent;
import atypon.app.node.kafka.event.database.DeleteDatabaseEvent;
import atypon.app.node.kafka.event.database.UpdateDatabaseEvent;
import atypon.app.node.locking.DistributedLocker;
import atypon.app.node.locking.LockExecutionResult;
import atypon.app.node.model.Database;
import atypon.app.node.request.database.DatabaseRequest;
import atypon.app.node.request.database.DatabaseUpdateRequest;
import atypon.app.node.response.ValidatorResponse;
import atypon.app.node.service.services.CollectionService;
import atypon.app.node.service.services.DatabaseService;
import atypon.app.node.service.services.ValidatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/database")
public class DatabaseController {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseController.class);
    private final DatabaseService databaseService;
    private final ValidatorService validatorService;
    private final KafkaService kafkaService;
    private final DistributedLocker distributedLocker;
    @Autowired
    public DatabaseController(DatabaseService databaseService,
                              ValidatorService validatorService,
                              KafkaService kafkaService,
                              DistributedLocker distributedLocker) {
        this.databaseService = databaseService;
        this.validatorService = validatorService;
        this.kafkaService = kafkaService;
        this.distributedLocker = distributedLocker;
    }
    @PostMapping(value = "/create")
    public ResponseEntity<?> createDatabase(@RequestBody DatabaseRequest request) {
        Database database = request.getDatabase();
        LockExecutionResult<?> result = null;
        try {
            result = distributedLocker.databaseWriteLock(database.getName(), 50, 55, () -> {
                ValidatorResponse validatorResponse = validatorService.isDatabaseExists(database.getName());
                if (validatorResponse.isValid()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validatorResponse.getMessage());
                }
                kafkaService.broadCast(TopicType.Create_Database, new CreateDatabaseEvent(request));
                return ResponseEntity.ok("Database created successfully!");
            });
            ResponseEntity<?> responseEntity = (ResponseEntity<?>) result.resultIfLockAcquired;
            logger.info("Database creation response -> " + responseEntity.getBody());
            return responseEntity;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(e.getMessage() + " " + "Request timeout, Please try again!");
        }
    }
    @RequestMapping(value = "/read/all")
    public ResponseEntity<List<String>> readDatabases() {
        List<String> databasesList = databaseService.readDatabases();
        return ResponseEntity.ok(databasesList);
    }
    @RequestMapping("/read/database")
    public ResponseEntity<?> readDatabase(@RequestBody Database database) {
        String databaseName = database.getName();
        LockExecutionResult<?> result = null;
        try {
            result = distributedLocker.databaseReadLock(database.getName(), 50, 55, () -> {
                ValidatorResponse validatorResponse = validatorService.isDatabaseExists(database.getName());
                if (!validatorResponse.isValid()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validatorResponse.getMessage());
                }
                return ResponseEntity.ok(databaseService.readDatabase(database));
            });
          //  ResponseEntity<?> responseEntity = (ResponseEntity<?>) result.resultIfLockAcquired;
         //   logger.info("Database reading response -> " + responseEntity.getBody());
            return (ResponseEntity<?>) result.resultIfLockAcquired;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(e.getMessage() + " " + "Request timeout, Please try again!");
        }
    }
    @PostMapping("/update")
    public ResponseEntity<?> updateDatabase(@RequestBody DatabaseUpdateRequest request) {
        String oldDatabaseName = request.getOldDatabaseName();
        String newDatabaseName = request.getNewDatabaseName();
        LockExecutionResult<?> result = null;
        try {
            result = distributedLocker.databaseWriteLock(oldDatabaseName, 12, 7, () -> {
                ValidatorResponse oldDbValidatorResponse = validatorService.isDatabaseExists(oldDatabaseName);
                if (!oldDbValidatorResponse.isValid()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(oldDbValidatorResponse.getMessage());
                }
                ValidatorResponse newDbValidatorResponse = validatorService.isDatabaseExists(newDatabaseName);
                if (newDbValidatorResponse.isValid()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(newDbValidatorResponse.getMessage());
                }
                kafkaService.broadCast(TopicType.Update_Database, new UpdateDatabaseEvent(request));
                return ResponseEntity.ok("Database name updated successfully!");
            });
            ResponseEntity<?> responseEntity = (ResponseEntity<?>) result.resultIfLockAcquired;
            logger.info("Database update response -> " + responseEntity.getBody());
            return responseEntity;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(e.getMessage() + " " + "Request timeout, Please try again!");
        }
    }
    @PostMapping("/delete")
    public ResponseEntity<?> deleteDatabase(@RequestBody DatabaseRequest request){
        Database database = request.getDatabase();
        LockExecutionResult<?> result = null;
        try {
            result = distributedLocker.databaseWriteLock(database.getName(), 15, 10, () -> {
                ValidatorResponse validatorResponse = validatorService.isDatabaseExists(database.getName());
                if (!validatorResponse.isValid()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(validatorResponse.getMessage());
                }
                kafkaService.broadCast(TopicType.Delete_Database, new DeleteDatabaseEvent(request));
                return ResponseEntity.ok("Database deleted successfully!");
            });
            ResponseEntity<?> responseEntity = (ResponseEntity<?>) result.resultIfLockAcquired;
            logger.info("Database deletion response -> " + responseEntity.getBody());
            return responseEntity;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(e.getMessage() + " " + "Request timeout, Please try again!");
        }
    }
}
