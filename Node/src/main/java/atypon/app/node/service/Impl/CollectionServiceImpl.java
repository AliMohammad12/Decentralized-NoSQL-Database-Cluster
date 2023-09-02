package atypon.app.node.service.Impl;

import atypon.app.node.model.Collection;
import atypon.app.node.model.Node;
import atypon.app.node.schema.CollectionSchema;
import atypon.app.node.service.services.CollectionService;
import atypon.app.node.service.services.JsonService;
import atypon.app.node.utility.FileOperations;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class CollectionServiceImpl implements CollectionService {
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
        FileOperations.createDirectory(path.toString(), collection.getName());
        FileOperations.createDirectory(path.resolve(collection.getName()).toString(), "Documents");
        FileOperations.writeJsonAtLocation(schemaJsonString, path.resolve(collection.getName()).toString(), "schema.json");
    }
    @Override
    public JsonNode readCollection(Collection collection) {
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
    }
    @Override
    public void deleteCollection(Collection collection) throws IOException {
        Path path = getPath().
                resolve(collection.getDatabase().getName()).
                resolve("Collections").
                resolve(collection.getName());
        FileOperations.deleteDirectory(path.toString());
    }
}
