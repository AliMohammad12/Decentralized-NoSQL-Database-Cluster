package atypon.app.node.service.Impl;

import atypon.app.node.indexing.IndexObject;
import atypon.app.node.indexing.bplustree.BPlusTree;
import atypon.app.node.model.Collection;
import atypon.app.node.model.Database;
import atypon.app.node.model.Node;
import atypon.app.node.model.User;
import atypon.app.node.response.ValidatorResponse;
import atypon.app.node.service.services.CollectionService;
import atypon.app.node.service.services.IndexingService;
import atypon.app.node.service.services.JsonService;
import atypon.app.node.utility.FileOperations;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class IndexingServiceImpl implements IndexingService {
    private HashMap<IndexObject, BPlusTree> indexRegistry;
    private final CollectionService collectionService;
    private final JsonService jsonService;
    @Autowired
    public IndexingServiceImpl(@Qualifier("indexRegistry") HashMap<IndexObject, BPlusTree> indexRegistry,
                               CollectionService collectionService,
                               JsonService jsonService) {
        this.indexRegistry = indexRegistry;
        this.collectionService = collectionService;
        this.jsonService = jsonService;
    }
    private static Path getPath() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        Path path = Path.of("Storage", Node.getName(), "Users", user.getUsername());
        return path;
    }
    @Override
    public void createIndexing(IndexObject indexObject) throws IOException {
        String type = jsonService.getPropertyTypeFromSchema(indexObject);
        BPlusTree<?, List<String>> bPlusTree = createBPlusTreeWithType(type);
        indexRegistry.put(indexObject, bPlusTree);
        setupIndexing(indexObject, bPlusTree, type);
        if (!indexExistsInFile(indexObject)) {
            appendToIndexingFile(getPath(), indexObject);
        }
    }
    @Override
    public void indexingInitializer() throws IOException {
        JsonNode jsonNode = jsonService.readJsonNode(getPath().resolve("Indexing.json").toString());
        for (JsonNode objNode : jsonNode) {
            String username = objNode.get("username").asText();
            String database = objNode.get("database").asText();
            String collection = objNode.get("collection").asText();
            String property = objNode.get("property").asText();
            IndexObject indexObject = new IndexObject(username, database, collection, property);
            createIndexing(indexObject);
        }
    }
    @Override
    public void setupIndexing(IndexObject indexObject, BPlusTree bPlusTree, String type) {
        Collection collection = new Collection(indexObject.getCollection(), new Database(indexObject.getDatabase()));
        ArrayNode jsonArray = collectionService.readCollection(collection);
        String property = indexObject.getProperty();
        for (JsonNode element : jsonArray) {
            JsonNode propertyNode = element.get(property);
            String id = element.get("id").asText();
            if (type.equals("string")) {
                String value = propertyNode.asText();
                List<String> list = (List<String>) bPlusTree.search(value);
                if (list == null) {
                    list = new ArrayList<>();
                    list.add(id);
                    bPlusTree.insert(value, list);
                } else {
                    list.add(id);
                }
            }  else if (type.equals("number")) {
                double value = Double.valueOf(propertyNode.numberValue().doubleValue());
                List<String> list = (List<String>) bPlusTree.search(value);
                if (list == null) {
                    list = new ArrayList<>();
                    list.add(id);
                    bPlusTree.insert(value, list);
                } else {
                    list.add(id);
                }
            } else if (type.equals("integer")) {
                int value = propertyNode.intValue();
                List<String> list = (List<String>) bPlusTree.search(value);
                if (list == null) {
                    list = new ArrayList<>();
                    list.add(id);
                    bPlusTree.insert(value, list);
                } else {
                    list.add(id);
                }
            } else if (type.equals("boolean")) {
                boolean value = propertyNode.asBoolean();
                List<String> list = (List<String>) bPlusTree.search(value);
                if (list == null) {
                    list = new ArrayList<>();
                    list.add(id);
                    bPlusTree.insert(value, list);
                } else {
                    list.add(id);
                }
            }
        }
    }
    public BPlusTree<?, List<String>> createBPlusTreeWithType(String type) {
        if ("string".equalsIgnoreCase(type)) {
            return new BPlusTree<String, List<String>>();
        } else if ("boolean".equalsIgnoreCase(type)) {
            return new BPlusTree<Boolean, List<String>>();
        } else if ("number".equalsIgnoreCase(type)) {
            return new BPlusTree<Double, List<String>>();
        } else if ("integer".equalsIgnoreCase(type)) {
            return new BPlusTree<Integer, List<String>>();
        } else {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }
    public void appendToIndexingFile(Path path, IndexObject indexObject) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, IndexObject.class);
        if (!FileOperations.isFileExists(path.resolve("Indexing.json").toString())) {
            String content = "[]";
            FileOperations.writeJsonAtLocation(content, path.toString(), "Indexing.json");
        }
        List<IndexObject> indexObjects;
        File jsonFile = new File(path.resolve("Indexing.json").toString());
        if (jsonFile.exists() && jsonFile.length() > 0) {
            indexObjects = objectMapper.readValue(jsonFile, listType);
        } else {
            indexObjects = new ArrayList<>();
        }
        indexObjects.add(indexObject);
        objectMapper.writeValue(jsonFile, indexObjects);
    }
    private boolean indexExistsInFile(IndexObject indexObject) throws IOException {
        JsonNode jsonNode = jsonService.readJsonNode(getPath().resolve("Indexing.json").toString());
        for (JsonNode objNode : jsonNode) {
            String database = objNode.get("database").asText();
            String collection = objNode.get("collection").asText();
            String property = objNode.get("property").asText();
            if (indexObject.getDatabase().equals(database) &&
                    indexObject.getCollection().equals(collection) &&
                    indexObject.getProperty().equals(property)) {
                return true;
            }
        }
        return false;
    }
}
