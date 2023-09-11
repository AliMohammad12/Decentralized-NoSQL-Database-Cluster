package atypon.app.node.controller;

import atypon.app.node.kafka.KafkaService;
import atypon.app.node.kafka.TopicType;
import atypon.app.node.kafka.event.database.CreateDatabaseEvent;
import atypon.app.node.kafka.event.database.DeleteDatabaseEvent;
import atypon.app.node.kafka.event.database.UpdateDatabaseEvent;
import atypon.app.node.model.Database;
import atypon.app.node.request.database.DatabaseRequest;
import atypon.app.node.request.database.DatabaseUpdateRequest;
import atypon.app.node.response.ValidatorResponse;
import atypon.app.node.service.services.DatabaseService;
import atypon.app.node.service.services.ValidatorService;
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
    private final DatabaseService databaseService;
    private final ValidatorService validatorService;
    private final KafkaService kafkaService;
    @Autowired
    public DatabaseController(DatabaseService databaseService,
                              ValidatorService validatorService,
                              KafkaService kafkaService) {
        this.databaseService = databaseService;
        this.validatorService = validatorService;
        this.kafkaService = kafkaService;
    }
    @PostMapping(value = "/create")
    public ResponseEntity<?> createDatabase(@RequestBody DatabaseRequest request) {
        Database database = request.getDatabase();
        ValidatorResponse validatorResponse = validatorService.isDatabaseExists(database.getName());
        if (validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validatorResponse.getMessage());
        }
        kafkaService.broadCast(TopicType.Create_Database, new CreateDatabaseEvent(request));
        return ResponseEntity.ok("Database created successfully!");
    }
    @RequestMapping(value = "/read/all")
    public ResponseEntity<List<String>> readDatabases() {
        List<String> databasesList = databaseService.readDatabases();
        return ResponseEntity.ok(databasesList);
    }
    @RequestMapping("/read/database")
    public ResponseEntity<?> readDatabase(@RequestBody Database database) {
        ValidatorResponse validatorResponse = validatorService.isDatabaseExists(database.getName());
        if (!validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(validatorResponse.getMessage());
        }
        List<String> collectionsList = databaseService.readDatabase(database);
        return ResponseEntity.ok(collectionsList);
    }
    @PostMapping("/update")
    public ResponseEntity<?> updateDatabase(@RequestBody DatabaseUpdateRequest request) {
        String oldDatabaseName = request.getOldDatabaseName();
        String newDatabaseName = request.getNewDatabaseName();
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
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteDatabase(@RequestBody DatabaseRequest request) throws IOException {
        Database database = request.getDatabase();
        ValidatorResponse validatorResponse = validatorService.isDatabaseExists(database.getName());
        if (!validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(validatorResponse.getMessage());
        }
        kafkaService.broadCast(TopicType.Delete_Database, new DeleteDatabaseEvent(request));
        return ResponseEntity.ok("Database deleted successfully!");
    }
}
