package atypon.app.node.service.Impl;

import atypon.app.node.indexing.IndexObject;
import atypon.app.node.model.Collection;
import atypon.app.node.model.Node;
import atypon.app.node.schema.CollectionSchema;
import atypon.app.node.service.services.CollectionService;
import atypon.app.node.service.services.IndexingService;
import atypon.app.node.service.services.JsonService;
import atypon.app.node.utility.DiskOperations;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;

@Service
public class CollectionServiceImpl implements CollectionService {
    private static final Logger logger = LoggerFactory.getLogger(CollectionService.class);
    private final JsonService jsonService;
    private final IndexingService indexingService;
    @Autowired
    public CollectionServiceImpl(JsonService jsonService,
                                 IndexingService indexingService) {
        this.jsonService = jsonService;
        this.indexingService = indexingService;
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
        if (!DiskOperations.isDirectoryExists(path.toString())) {
            DiskOperations.createDirectory(getPath().resolve(databaseName).toString(), "Collections");
        }
        DiskOperations.createDirectory(path.toString(), collection.getName());
        DiskOperations.createDirectory(path.resolve(collection.getName()).toString(), "Documents");
        DiskOperations.writeToFile(schemaJsonString, path.resolve(collection.getName()).toString(), "schema.json");
        logger.info("Successfully created collection schema '" + collection.getName() + "' within '" +
                databaseName + "' database!, schema: \n" + schemaJsonString);
    }
    @Override
    public ArrayNode readCollection(Collection collection) throws IOException {
        logger.info("Reading the collection with the name '" + collection.getName() + "' !");
        Path path = getPath().
                resolve(collection.getDatabase().getName()).
                resolve("Collections").
                resolve(collection.getName()).
                resolve("Documents");
        return jsonService.readJsonArray(path.toString());
    }
    @Override
    public JsonNode readCollectionFields(Collection collection) throws IOException {
        String database = collection.getDatabase().getName();
        String collectionName = collection.getName();
        Path path = getPath().resolve(database).resolve("Collections").resolve(collectionName);
        JsonNode schemaNode = jsonService.readJsonNode(path.resolve("schema.json").toString());
        JsonNode properties = schemaNode.get("properties");
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode fields = objectMapper.createObjectNode();

        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = properties.fields();
        while (fieldsIterator.hasNext()) {
            Map.Entry<String, JsonNode> fieldEntry = fieldsIterator.next();
            String fieldName = fieldEntry.getKey();
            JsonNode fieldTypeNode = fieldEntry.getValue().path("type");
            String fieldType = fieldTypeNode.isTextual() ? fieldTypeNode.asText() : "Unknown";

            fields.put(fieldName, fieldType);
        }
        return fields;
    }
    // todo: indexing file content should change too + tree!
    @Override
    public void updateCollectionName(String databaseName, String oldCollectionName, String newCollectionName) {
        Path path = getPath().
                resolve(databaseName).
                resolve("Collections");
        DiskOperations.updateDirectoryName(path.toString(), oldCollectionName, newCollectionName);

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
        JsonNode jsonNode = readCollectionFields(collection);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();

        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = jsonNode.fields();
        while (fieldsIterator.hasNext()) {
            Map.Entry<String, JsonNode> fieldEntry = fieldsIterator.next();
            String fieldName = fieldEntry.getKey();

            IndexObject indexObject = new IndexObject(user.getUsername(), collection.getDatabase().getName(),
                    collection.getName(), fieldName);
            if (indexingService.isIndexed(indexObject)) {
                indexingService.deleteIndexing(indexObject);
            }
        }

        DiskOperations.deleteDirectory(path.toString());
        logger.info("Successfully deleted the collection '" + collection.getName() +"' within '" +
                collection.getDatabase().getName() + "' database!");
    }
}
