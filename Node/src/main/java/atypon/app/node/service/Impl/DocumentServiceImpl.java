package atypon.app.node.service.Impl;

import atypon.app.node.indexing.IndexObject;
import atypon.app.node.indexing.Property;
import atypon.app.node.model.Collection;
import atypon.app.node.model.Database;
import atypon.app.node.model.Node;
import atypon.app.node.request.document.DocumentRequestByProperty;
import atypon.app.node.service.services.CollectionService;
import atypon.app.node.service.services.DocumentService;
import atypon.app.node.service.services.IndexingService;
import atypon.app.node.service.services.JsonService;
import atypon.app.node.utility.FileOperations;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class DocumentServiceImpl implements DocumentService {
    private final JsonService jsonService;
    private final IndexingService indexingService;
    private final CollectionService collectionService;
    @Autowired
    public DocumentServiceImpl(JsonService jsonService
                               ,IndexingService indexingService
                                ,CollectionService collectionService) {
        this.jsonService = jsonService;
        this.indexingService = indexingService;
        this.collectionService = collectionService;
    }
    private static Path getPath() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        Path path = Path.of("Storage", Node.getName(), "Users", user.getUsername(), "Databases");
        return path;
    }
    @Override
    public void addDocument(String databaseName, String collectionName, JsonNode document) throws JsonProcessingException {
        ObjectNode objectNode = (ObjectNode) document;
        if (!objectNode.has("id")) {
            String uniqueId = java.util.UUID.randomUUID().toString();
            objectNode.put("id", uniqueId);
        }
        String jsonString = jsonService.convertJsonToString(document);
        Path path = getPath().resolve(databaseName).resolve("Collections").resolve(collectionName).resolve("Documents");
        FileOperations.writeJsonAtLocation(jsonString, path.toString(), objectNode.get("id").asText() + ".json");
        indexingService.addDocument(databaseName, collectionName, objectNode);
    }
    @Override
    public JsonNode readDocument(String database, String collection, String id) throws IOException {
        Path path = getPath().resolve(database).resolve("Collections").resolve(collection).resolve("Documents").resolve(id+".json");
        return jsonService.readJsonNode(path.toString());
    }
    @Override
    public void deleteDocument(DocumentRequestByProperty request) throws IOException {
        String collectionName = request.getCollection();
        String databaseName = request.getDatabase();
        Property property = request.getProperty();

        // if it's indexed
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();

        IndexObject indexObject = new IndexObject(user.getUsername(), databaseName, collectionName, property.getName());
        if (indexingService.isIndexed(indexObject)) {
            System.out.println("Inside indexing!!!");
            indexingService.deleteDocument(databaseName, collectionName, property);
            return;
        }

        Path path = getPath().resolve(databaseName)
                .resolve("Collections")
                .resolve(collectionName)
                .resolve("Documents");


        // todo: must remove the deleted ID's from the tree --> solved: lazy deletion
        //  when we add, or remove a document through the tree.

        Collection collection = new Collection(collectionName, new Database(databaseName));
        ArrayNode jsonArray = collectionService.readCollection(collection);
        for (JsonNode element : jsonArray) {
            JsonNode propertyNode = element.get(property.getName());
            String id = element.get("id").asText();

            if (property.isBooleanValue()) {
                boolean value = propertyNode.asBoolean();
                if (property.getValue().equals(value)) {
                    FileOperations.deleteFile(path.resolve(id+".json").toString());
                }
            } else if (property.isDoubleValue()) {
                double value = propertyNode.asDouble();
                if (property.getValue().equals(value)) {
                    FileOperations.deleteFile(path.resolve(id+".json").toString());
                }

            } else if (property.isIntegerValue()) {
                int value = propertyNode.asInt();
                if (property.getValue().equals(value)) {
                    FileOperations.deleteFile(path.resolve(id+".json").toString());
                }

            } else if (property.isStringValue()) {
                String value = propertyNode.asText();
                if (property.getValue().equals(value)) {
                    FileOperations.deleteFile(path.resolve(id+".json").toString());
                }
            }
        }
    }
    @Override
    public void updateDocument() {
        // 1- take the directory from indexing
        // 2- check if newDocument follows the schema (validateDocument)
        // 3- delete old document, and the new

    }
}
