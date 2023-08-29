package atypon.app.node.service.services;

import java.io.IOException;
import java.util.List;

public interface DatabaseService {
    void createDatabase(String databaseName);
    void updateDatabaseName(String oldDatabaseName, String newDatabaseName);
    void deleteDatabase(String databaseName) throws IOException;
    List<String> readDatabases();
    List<String> readDatabase(String databaseName);
}
