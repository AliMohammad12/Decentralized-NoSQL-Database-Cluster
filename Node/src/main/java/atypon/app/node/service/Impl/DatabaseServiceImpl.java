package atypon.app.node.service.Impl;

import atypon.app.node.model.Database;
import atypon.app.node.model.Node;
import atypon.app.node.service.services.DatabaseService;
import atypon.app.node.utility.FileOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static Path getPath() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        Path path = Path.of("Storage", Node.getName(), "Users", user.getUsername(), "Databases");
        return path;
    }
    @Override
    public void createDatabase(Database database) {
        Path path = getPath();
        FileOperations.createDirectory(path.toString(), database.getName());
        logger.info("Database with the name '" + database.getName() + "' have been successfully created!");
    }
    @Override
    public List<String> readDatabases() {
        Path path = getPath();
        return FileOperations.readDirectories(path.toString());
    }
    @Override
    public List<String> readDatabase(Database database) {
        Path path = getPath().resolve(database.getName()).resolve("Collections");
        return FileOperations.readDirectories(path.toString());
    }
    @Override
    public void updateDatabaseName(String oldDatabaseName, String newDatabaseName) {
        Path path = getPath();
        FileOperations.updateDirectoryName(path.toString(), oldDatabaseName, newDatabaseName);
        logger.info("Database with the name '" + oldDatabaseName + "' have been successfully updated to '" + newDatabaseName + "' !");

    }
    @Override
    public void deleteDatabase(Database database) throws IOException {
        Path path = getPath().resolve(database.getName());
        FileOperations.deleteDirectory(path.toString());
        logger.info("Database with the name '" + database.getName() + "' have been successfully deleted!");
    }
}
