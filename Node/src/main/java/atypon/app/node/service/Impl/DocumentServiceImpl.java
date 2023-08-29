package atypon.app.node.service.Impl;

import atypon.app.node.service.services.DocumentService;
import atypon.app.node.service.services.JsonService;
import atypon.app.node.utility.FileOperations;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

@Service
public class DocumentServiceImpl implements DocumentService {
    private final JsonService jsonService;
    @Autowired
    public DocumentServiceImpl(JsonService jsonService) {
        this.jsonService = jsonService;
    }
    @Override
    public void addDocument(String databaseName, String collectionName, JsonNode document) throws JsonProcessingException {
        ObjectNode objectNode = (ObjectNode) document;
        String uniqueId = java.util.UUID.randomUUID().toString();
        objectNode.put("id", uniqueId);
        String jsonString = jsonService.convertJsonToString(document);
        String path = "Databases/"+databaseName+"/Collections/"+collectionName+"/Documents";
        FileOperations.writeJsonAtLocation(jsonString, path, uniqueId + ".json");
    }
    @Override
    public JsonNode readDocument(String database, String collection, String id) throws IOException {
        return jsonService.readJsonNode("Databases/"+database+"/Collections/"+collection+"/Documents/"+id+".json");
    }
    @Override
    public void deleteDocument(String database, String collection, String id) throws IOException {
        FileOperations.deleteFile("Databases/"+database+"/Collections/"+collection+"/Documents/"+id+".json");
    }
    @Override
    public void updateDocument() {
        // 1- take the directory from indexing
        // 2- check if newDocument follows the schema (validateDocument)
        // 3- delete old document, and the new

    }

    public static void main(String[] args) {
        Path path = Path.of("db1","Collections","students");
        path.resolve("potato");
        System.out.println(path);
    }
}
