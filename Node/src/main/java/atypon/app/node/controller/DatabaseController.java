package atypon.app.node.controller;

import atypon.app.node.model.Database;
import atypon.app.node.request.database.DatabaseRequest;
import atypon.app.node.request.database.DatabaseUpdateRequest;
import atypon.app.node.response.ValidatorResponse;
import atypon.app.node.service.services.BroadcastService;
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
    private final BroadcastService broadcastService;
    @Autowired
    public DatabaseController(DatabaseService databaseService,
                              ValidatorService validatorService,
                              BroadcastService broadcastService) {
        this.databaseService = databaseService;
        this.validatorService = validatorService;
        this.broadcastService = broadcastService;
    }
    @PostMapping(value = "/create")
    public ResponseEntity<?> createDatabase(@RequestBody DatabaseRequest request) {
        Database database = request.getDatabase();
        if (!request.isBroadcast()) {
            ValidatorResponse validatorResponse = validatorService.isDatabaseExists(database.getName());
            if (validatorResponse.isValid()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validatorResponse.getMessage());
            }
        }
        databaseService.createDatabase(database);
        broadcastService.ProtectedBroadcast(request, "/database/create");
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
        if (!request.isBroadcast()) {
            ValidatorResponse oldDbValidatorResponse = validatorService.isDatabaseExists(oldDatabaseName);
            if (!oldDbValidatorResponse.isValid()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(oldDbValidatorResponse.getMessage());
            }
            ValidatorResponse newDbValidatorResponse = validatorService.isDatabaseExists(newDatabaseName);
            if (newDbValidatorResponse.isValid()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(newDbValidatorResponse.getMessage());
            }
        }
        databaseService.updateDatabaseName(oldDatabaseName, newDatabaseName);
        broadcastService.ProtectedBroadcast(request, "/database/update");
        return ResponseEntity.ok("Database name updated successfully!");
    }
    @PostMapping("/delete")
    public ResponseEntity<?> deleteDatabase(@RequestBody DatabaseRequest request) throws IOException {
        Database database = request.getDatabase();
        if (!request.isBroadcast()) {
            ValidatorResponse validatorResponse = validatorService.isDatabaseExists(database.getName());
            if (!validatorResponse.isValid()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(validatorResponse.getMessage());
            }
        }
        databaseService.deleteDatabase(database);
        broadcastService.ProtectedBroadcast(request, "/database/delete");
        return ResponseEntity.ok("Database deleted successfully!");
    }
}
