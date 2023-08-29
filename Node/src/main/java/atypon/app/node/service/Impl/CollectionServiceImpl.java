package atypon.app.node.service.Impl;

import atypon.app.node.schema.CollectionSchema;
import atypon.app.node.service.services.CollectionService;
import atypon.app.node.service.services.JsonService;
import atypon.app.node.utility.FileOperations;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class CollectionServiceImpl implements CollectionService {
    private final JsonService jsonService;
    @Autowired
    public CollectionServiceImpl(JsonService jsonService) {
        this.jsonService = jsonService;
    }
    @Override
    public void createCollection(CollectionSchema collectionSchema) throws JsonProcessingException {
        JsonNode jsonSchema = jsonService.generateJsonSchema2(collectionSchema.getFields());
        String collectionName = collectionSchema.getCollectionName();
        String databaseName = collectionSchema.getDatabaseName();

        String schemaJsonString = jsonService.convertJsonToString(jsonSchema);
//        Authentication auth  = SecurityContextHolder.get();
//        auth.getname()
        String path = "Databases/" + databaseName + "/Collections/";
        FileOperations.createDirectory(path, collectionName);
        FileOperations.createDirectory(path + "/" + collectionName, "Documents");
        FileOperations.writeJsonAtLocation(schemaJsonString, path + "/" + collectionName, "schema.json");
    }
    @Override
    public JsonNode readCollection(String databaseName, String collectionName) {
        String collectionPath = "Databases/"+databaseName+"/Collections/"+collectionName+"/Documents";
        return jsonService.readJsonArray(collectionPath);
    }
    @Override
    public void updateCollectionName(String databaseName, String oldCollectionName, String newCollectionName) {
        String path = "Databases/"+databaseName+"/Collections/";
        FileOperations.updateDirectoryName(path, oldCollectionName, newCollectionName);
    }
    @Override
    public void deleteCollection(String databaseName, String collectionName) throws IOException {
        FileOperations.deleteDirectory("Databases/"+databaseName+"/Collections/"+collectionName);
    }
}
