package atypon.app.node.service.Impl;

import atypon.app.node.model.Node;
import atypon.app.node.service.services.DocumentService;
import atypon.app.node.service.services.JsonService;
import atypon.app.node.utility.FileOperations;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
    @Autowired
    public DocumentServiceImpl(JsonService jsonService) {
        this.jsonService = jsonService;
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
    }
    @Override
    public JsonNode readDocument(String database, String collection, String id) throws IOException {
        Path path = getPath().resolve(database).resolve("Collections").resolve(collection).resolve("Documents").resolve(id+".json");
        return jsonService.readJsonNode(path.toString());
    }
    @Override
    public void deleteDocument(String database, String collection, String id) throws IOException {
        Path path = getPath().resolve(database).resolve("Collections").resolve(collection).resolve("Documents").resolve(id+".json");
        FileOperations.deleteFile(path.toString());
    }
    @Override
    public void updateDocument() {
        // 1- take the directory from indexing
        // 2- check if newDocument follows the schema (validateDocument)
        // 3- delete old document, and the new

    }
}
