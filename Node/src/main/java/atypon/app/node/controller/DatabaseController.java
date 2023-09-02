package atypon.app.node.controller;


import atypon.app.node.model.Database;
import atypon.app.node.request.DatabaseUpdateRequest;
import atypon.app.node.response.APIResponse;
import atypon.app.node.response.ValidatorResponse;
import atypon.app.node.service.Impl.BroadcastingService;
import atypon.app.node.service.services.DatabaseService;
import atypon.app.node.service.services.ValidatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;


@RestController
public class DatabaseController {
    private final DatabaseService databaseService;
    private final ValidatorService validatorService;
    private final BroadcastingService broadcastingService;

    @Autowired
    public DatabaseController(DatabaseService databaseService,
                              ValidatorService validatorService,
                              BroadcastingService broadcastingService) {
        this.databaseService = databaseService;
        this.validatorService = validatorService;
        this.broadcastingService = broadcastingService;
    }

    @RequestMapping(value = "/create-database")
    public ResponseEntity<?> createDatabase(@RequestBody Database database) {
        ValidatorResponse validatorResponse = validatorService.isDatabaseExists(database.getName());
        if (validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validatorResponse.getMessage());
        }
        databaseService.createDatabase(database);
        return ResponseEntity.ok("Database created successfully!");
    }
    @RequestMapping(value = "/read-databases")
    public ResponseEntity<List<String>> readDatabases() {
        List<String> databasesList = databaseService.readDatabases();
        return ResponseEntity.ok(databasesList);
    }
    @RequestMapping("/read-database")
    public ResponseEntity<?> readDatabase(@RequestBody Database database) {
        ValidatorResponse validatorResponse = validatorService.isDatabaseExists(database.getName());
        if (!validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(validatorResponse.getMessage());
        }
        List<String> collectionsList = databaseService.readDatabase(database);
        return ResponseEntity.ok(collectionsList);
    }
    @RequestMapping("/update-database")
    public ResponseEntity<?> updateDatabase(@RequestBody DatabaseUpdateRequest request) {
        String oldDatabaseName = request.getOldDatabaseName();
        String newDatabaseName = request.getNewDatabaseName();
        ValidatorResponse validatorResponse = validatorService.isDatabaseExists(oldDatabaseName);
        if (!validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(validatorResponse.getMessage());
        }
        if (validatorService.isDatabaseExists(newDatabaseName).isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validatorResponse.getMessage());
        }
        databaseService.updateDatabaseName(oldDatabaseName, newDatabaseName);
        return ResponseEntity.ok("Database name updated successfully!");
    }
    @RequestMapping("/delete-database")
    public ResponseEntity<?> deleteDatabase(@RequestBody Database database) throws IOException {
        ValidatorResponse validatorResponse = validatorService.isDatabaseExists(database.getName());
        if (!validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(validatorResponse.getMessage());
        }
        databaseService.deleteDatabase(database);
        return ResponseEntity.ok("Database deleted successfully!");
    }
}
