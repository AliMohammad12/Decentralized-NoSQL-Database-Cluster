package atypon.app.node.service.services;

import atypon.app.node.model.Database;

import java.io.IOException;
import java.util.List;

public interface DatabaseService {
    void createDatabase(Database database);
    void updateDatabaseName(String oldDatabaseName, String newDatabaseName);
    void deleteDatabase(Database database) throws IOException;
    List<String> readDatabases();
    List<String> readDatabase(Database database);
}
