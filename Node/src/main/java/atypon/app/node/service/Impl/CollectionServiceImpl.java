package atypon.app.node.service.Impl;

import atypon.app.node.model.Collection;
import atypon.app.node.model.Node;
import atypon.app.node.schema.CollectionSchema;
import atypon.app.node.service.services.CollectionService;
import atypon.app.node.service.services.DatabaseService;
import atypon.app.node.service.services.JsonService;
import atypon.app.node.utility.FileOperations;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class CollectionServiceImpl implements CollectionService {
    private static final Logger logger = LoggerFactory.getLogger(CollectionService.class);
    private final JsonService jsonService;
    @Autowired
    public CollectionServiceImpl(JsonService jsonService) {
        this.jsonService = jsonService;
    }
    private static Path getPath() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        Path path = Path.of("Storage", Node.getName(), "Users", user.getUsername(), "Databases");
        return path;
    }
    @Override
    public void createCollection(CollectionSchema collectionSchema) throws JsonProcessingException {
        JsonNode jsonSchema = jsonService.generateJsonSchema2(collectionSchema.getFields());
        Collection collection = collectionSchema.getCollection();
        String databaseName = collection.getDatabase().getName();
        String schemaJsonString = jsonService.convertJsonToString(jsonSchema);
        Path path = getPath().
                resolve(databaseName)
                .resolve("Collections");
        if (!FileOperations.isDirectoryExists(path.toString())) {
            FileOperations.createDirectory(getPath().resolve(databaseName).toString(), "Collections");
        }
        FileOperations.createDirectory(path.toString(), collection.getName());
        FileOperations.createDirectory(path.resolve(collection.getName()).toString(), "Documents");
        FileOperations.writeJsonAtLocation(schemaJsonString, path.resolve(collection.getName()).toString(), "schema.json");

        logger.info("Successfully created collection schema '" +collection.getName() +"' within '" +
                databaseName + "' database!, schema: \n" + schemaJsonString);
    }
    @Override
    public ArrayNode readCollection(Collection collection) {
        logger.info("Reading the collection with the name '" + collection.getName() + "' !");
        Path path = getPath().
                resolve(collection.getDatabase().getName()).
                resolve("Collections").
                resolve(collection.getName()).
                resolve("Documents");
        return jsonService.readJsonArray(path.toString());
    }
    @Override
    public void updateCollectionName(String databaseName, String oldCollectionName, String newCollectionName) {
        Path path = getPath().
                resolve(databaseName).
                resolve("Collections");
        FileOperations.updateDirectoryName(path.toString(), oldCollectionName, newCollectionName);

        logger.info("Successfully updated the name of '" + oldCollectionName
                + "' collection to '" + newCollectionName + "' within '" + databaseName + "' database!");
    }


    // todo: must clear the B+ Tree (Don't forget)
    @Override
    public void deleteCollection(Collection collection) throws IOException {
        Path path = getPath().
                resolve(collection.getDatabase().getName()).
                resolve("Collections").
                resolve(collection.getName());
        FileOperations.deleteDirectory(path.toString());
        logger.info("Successfully deleted the collection '" + collection.getName() +"' within '" +
                collection.getDatabase().getName() + "' database!");
    }
}
