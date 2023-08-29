package atypon.app.node.controller;


import atypon.app.node.model.Node;
import atypon.app.node.request.DatabaseUpdateRequest;
import atypon.app.node.response.APIResponse;
import atypon.app.node.response.ValidatorResponse;
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
    @Autowired
    public DatabaseController(DatabaseService databaseService, ValidatorService validatorService) {
        this.databaseService = databaseService;
        this.validatorService = validatorService;
    }
    @RequestMapping(value = "/create-database")
    public ResponseEntity<APIResponse> createDatabase(@RequestBody String databaseName) { // consider making this take Database class
        System.out.println(Node.getNodeId());  // todo: remember that we used this just for testing, revert it later.
        return ResponseEntity.ok(new APIResponse("Database created successfully!", 200));

//        ValidatorResponse validatorResponse = validatorService.isDatabaseExists(databaseName);
//        if (validatorResponse.isValid()) {
//            return ResponseEntity.ok(new APIResponse(validatorResponse.getMessage(), 400));
//        }
//        databaseService.createDatabase(databaseName);
//        return ResponseEntity.ok(new APIResponse("Database created successfully!", 200));
    }
    @RequestMapping("/read-databases")
    public String readDatabases() {
        System.out.println(Node.getNodeId());
        System.out.println(Node.getNodeName());
        String a = "x";
        return a;
        //List<String> databasesList = databaseService.readDatabases();
      //  return ResponseEntity.ok(databasesList);
    }
    @RequestMapping("/read-database") // read specific database by displaying its collections
    public ResponseEntity<?> readDatabase(@RequestBody String databaseName) { // consider making this take Database class
        ValidatorResponse validatorResponse = validatorService.isDatabaseExists(databaseName);
        if (!validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(validatorResponse.getMessage());
        }
        List<String> collectionsList = databaseService.readDatabase(databaseName);
        return ResponseEntity.ok(collectionsList);
    }
    @RequestMapping("/update-database")
    public ResponseEntity<APIResponse> updateDatabase(@RequestBody DatabaseUpdateRequest request) {
        String oldDatabaseName = request.getOldDatabaseName();
        String newDatabaseName = request.getNewDatabaseName();
        ValidatorResponse validatorResponse = validatorService.isDatabaseExists(oldDatabaseName);
        if (!validatorResponse.isValid()) {
            return ResponseEntity.ok(new APIResponse(validatorResponse.getMessage(), 404));
        }
        if (validatorService.isDatabaseExists(newDatabaseName).isValid()) {
            return ResponseEntity.ok(new APIResponse("Database with the name " + newDatabaseName + " already exists.", 400));
        }
        databaseService.updateDatabaseName(oldDatabaseName, newDatabaseName);
        return ResponseEntity.ok(new APIResponse("Database name updated successfully!", 200));
    }
    @RequestMapping("/delete-database")
    public ResponseEntity<APIResponse> deleteDatabase(@RequestBody String databaseName) throws IOException { // consider making this take Database class
        ValidatorResponse validatorResponse = validatorService.isDatabaseExists(databaseName);
        if (!validatorResponse.isValid()) {
            return ResponseEntity.ok(new APIResponse(validatorResponse.getMessage(), 404));
        }
        databaseService.deleteDatabase(databaseName);
        return ResponseEntity.ok(new APIResponse("Database deleted successfully!", 200));
    }
}
