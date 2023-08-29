package atypon.app.node.service.Impl;

import atypon.app.node.service.services.DatabaseService;
import atypon.app.node.utility.FileOperations;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DatabaseServiceImpl implements DatabaseService {
    @Override
    public void createDatabase(String databaseName) {
        FileOperations.createDatabaseDirectory(databaseName);
    }
    @Override
    public List<String> readDatabases() {
        return FileOperations.readDirectories("Databases");
    }
    @Override
    public List<String> readDatabase(String databaseName) {
        return FileOperations.readDirectories("Databases/" + databaseName + "/Collections");
    }
    @Override
    public void updateDatabaseName(String oldDatabaseName, String newDatabaseName) {
        FileOperations.updateDirectoryName("Databases/", oldDatabaseName, newDatabaseName);
    }
    @Override
    public void deleteDatabase(String databaseName) throws IOException {
        FileOperations.deleteDirectory("Databases/"+databaseName);
    }

}
