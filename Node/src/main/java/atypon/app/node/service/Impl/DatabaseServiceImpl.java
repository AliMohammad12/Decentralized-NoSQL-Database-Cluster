package atypon.app.node.service.Impl;

import atypon.app.node.model.Collection;
import atypon.app.node.model.Database;
import atypon.app.node.model.Node;
import atypon.app.node.service.services.CollectionService;
import atypon.app.node.service.services.DatabaseService;
import atypon.app.node.utility.DiskOperations;
import io.lettuce.core.ScriptOutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
public class DatabaseServiceImpl implements DatabaseService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    private final CollectionService collectionService;
    @Autowired
    public DatabaseServiceImpl(CollectionService collectionService) {
        this.collectionService = collectionService;
    }
    private static Path getPath() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        Path path = Path.of("Storage", Node.getName(), "Users", user.getUsername(), "Databases");
        return path;
    }
    @Override
    public void createDatabase(Database database) {
        Path path = getPath();
        DiskOperations.createDirectory(path.toString(), database.getName());
        logger.info("Database with the name '" + database.getName() + "' have been successfully created!");
    }
    @Override
    public List<String> readDatabases() {
        logger.info("Reading all databases!");
        Path path = getPath();
        return DiskOperations.readDirectories(path.toString());
    }
    @Override
    public List<String> readDatabase(Database database) {
        logger.info("Reading the database with the name: '" + database.getName() + "' !");
        Path path = getPath().resolve(database.getName()).resolve("Collections");
        return DiskOperations.readDirectories(path.toString());
    }

    // todo: indexing file content should change too + TREE!!!!!
    @Override
    public void updateDatabaseName(String oldDatabaseName, String newDatabaseName) {
        Path path = getPath();
        DiskOperations.updateDirectoryName(path.toString(), oldDatabaseName, newDatabaseName);
        logger.info("Database with the name '" + oldDatabaseName + "' have been successfully updated to '" + newDatabaseName + "' !");
    }

    // todo: indexing file content should change too!!
    @Override
    public void deleteDatabase(Database database) throws IOException {
        List<String> collections = readDatabase(database);
        for (String name : collections) {
            Collection collection = new Collection();
            collection.setName(name);
            collection.setDatabase(database);
            collectionService.deleteCollection(collection);
        }
        Path path = getPath().resolve(database.getName());
        DiskOperations.deleteDirectory(path.toString());
        logger.info("Database with the name '" + database.getName() + "' have been successfully deleted!");
    }
}
