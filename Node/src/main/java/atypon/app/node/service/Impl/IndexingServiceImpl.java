package atypon.app.node.service.Impl;

import atypon.app.node.indexing.IndexObject;
import atypon.app.node.indexing.Property;
import atypon.app.node.indexing.bplustree.BPlusTree;
import atypon.app.node.model.Collection;
import atypon.app.node.model.Database;
import atypon.app.node.model.Node;
import atypon.app.node.service.services.CollectionService;
import atypon.app.node.service.services.IndexingService;
import atypon.app.node.service.services.JsonService;
import atypon.app.node.utility.FileOperations;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

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
    private static String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        String username = user.getUsername();
        return username;
    }
    @Override
    public void createIndexing(IndexObject indexObject) throws IOException {
        String type = jsonService.getPropertyTypeFromSchema(indexObject);
        BPlusTree<?, List<String>> bPlusTree = createBPlusTreeWithType(type);
        indexRegistry.put(indexObject, bPlusTree);
        setupIndexing(indexObject, bPlusTree, type);
        if (!indexExistsInFile(indexObject)) {
            appendToIndexingFile(indexObject);
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
    public void deleteIndexing(IndexObject indexObject) throws IOException {
        JsonNode jsonNode = jsonService.readJsonNode(getPath().resolve("Indexing.json").toString());
        File jsonFile = new File(getPath().resolve("Indexing.json").toString());
        List<IndexObject> indexObjects = new ArrayList<>();
        for (JsonNode objNode : jsonNode) {
            String username = objNode.get("username").asText();
            String database = objNode.get("database").asText();
            String collection = objNode.get("collection").asText();
            String property = objNode.get("property").asText();
            if (!username.equals(indexObject.getUsername())
                    || !database.equals(indexObject.getDatabase())
                    || !collection.equals(indexObject.getCollection())
                    || !property.equals(indexObject.getProperty())) {
                indexObjects.add(new IndexObject(username, database, collection, property));
            }
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(jsonFile, indexObjects);
        indexRegistry.remove(indexObject);
    }
    @Override
    public void IndexingFinalizer() {
        String username = getUsername();
        Iterator<Map.Entry<IndexObject, BPlusTree>> iterator = indexRegistry.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<IndexObject, BPlusTree> entry = iterator.next();
            IndexObject indexObject = entry.getKey();
            if (indexObject.getUsername().equals(username)) {
                iterator.remove();
            }
        }
    }
    @Override
    public void indexDocumentPropertiesIfExists(String database, String collection, ObjectNode document) {
        String username = getUsername();
        String id = document.get("id").asText();
        Iterator<String> iterator = document.fieldNames();
        while (iterator.hasNext()) {
            String field = iterator.next();
            if (field.equals("id")) continue;
            IndexObject indexObject = new IndexObject(username, database, collection, field);
            String type = jsonService.getPropertyTypeFromSchema(indexObject);
            JsonNode node = document.get(field);

            if (isIndexed(indexObject)) {
                BPlusTree bPlusTree = indexRegistry.get(indexObject);
                addDocumentToTree(bPlusTree, id, type, node);
            }
        }
    }
    @Override
    public void deleteDocumentByProperty(String database, String collection, Property property) throws IOException {
        String username = getUsername();
        IndexObject indexObject = new IndexObject(username, database, collection, property.getName());
        BPlusTree bPlusTree = indexRegistry.get(indexObject);
        Path path = getPath()
                .resolve("Databases")
                .resolve(database)
                .resolve("Collections")
                .resolve(collection)
                .resolve("Documents");

        List<String> documentList;
        if (property.isIntegerValue()) {
            int value = (int) property.getValue();
            documentList = (List<String>) bPlusTree.search(value);
            if (documentList != null) {
                for (String id : documentList) {
                    if (FileOperations.isFileExists(path.resolve(id + ".json").toString())) {
                        FileOperations.deleteFile(path.resolve(id + ".json").toString());
                    }
                }
                bPlusTree.delete(value);
            }
        } else if (property.isDoubleValue()) {
            double value = (double) property.getValue();
            documentList = (List<String>) bPlusTree.search(value);
            if (documentList != null) {
                for (String id : documentList) {
                    if (FileOperations.isFileExists(path.resolve(id + ".json").toString())) {
                        FileOperations.deleteFile(path.resolve(id + ".json").toString());
                    }                }
                bPlusTree.delete(value);
            }
        } else if (property.isStringValue()) {
            String value = (String) property.getValue();
            documentList = (List<String>) bPlusTree.search(value);
            if (documentList != null) {
                for (String id : documentList) {
                    if (FileOperations.isFileExists(path.resolve(id + ".json").toString())) {
                        FileOperations.deleteFile(path.resolve(id + ".json").toString());
                    }                }
                bPlusTree.delete(value);
            }
        } else if (property.isBooleanValue()) {
            boolean value = (boolean) property.getValue();
            documentList = (List<String>) bPlusTree.search(value);
            if (documentList != null) {
                for (String id : documentList) {
                    if (FileOperations.isFileExists(path.resolve(id + ".json").toString())) {
                        FileOperations.deleteFile(path.resolve(id + ".json").toString());
                    }                }
                bPlusTree.delete(value);
            }
        }
    }

    @Override
    public ArrayNode readDocumentsByProperty(String database, String collection, Property property) {

        return null;
    }

    @Override
    public boolean isIndexed(IndexObject indexObject) {
        return indexRegistry.containsKey(indexObject);
    }
    @Override
    public void setupIndexing(IndexObject indexObject, BPlusTree bPlusTree, String type) {
        Collection collection = new Collection(indexObject.getCollection(), new Database(indexObject.getDatabase()));
        ArrayNode jsonArray = collectionService.readCollection(collection);
        String property = indexObject.getProperty();
        for (JsonNode element : jsonArray) {
            JsonNode propertyNode = element.get(property);
            String id = element.get("id").asText();
            addDocumentToTree(bPlusTree, id, type, propertyNode);
        }
    }

    private void addDocumentToTree(BPlusTree bPlusTree, String id, String type, JsonNode node) {
        switch (type) {
            case "string" -> {
                String value = node.asText();
                List<String> list = (List<String>) bPlusTree.search(value);
                if (list == null) {
                    list = new ArrayList<>();
                    list.add(id);
                    bPlusTree.insert(value, list);
                } else {
                    list.add(id);
                }
            }
            case "number" -> {
                double value = Double.valueOf(node.numberValue().doubleValue());
                List<String> list = (List<String>) bPlusTree.search(value);
                if (list == null) {
                    list = new ArrayList<>();
                    list.add(id);
                    bPlusTree.insert(value, list);
                } else {
                    list.add(id);
                }
            }
            case "integer" -> {
                int value = node.intValue();
                List<String> list = (List<String>) bPlusTree.search(value);
                if (list == null) {
                    list = new ArrayList<>();
                    list.add(id);
                    bPlusTree.insert(value, list);
                } else {
                    list.add(id);
                }
            }
            case "boolean" -> {
                boolean value = node.asBoolean();
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
    public void appendToIndexingFile(IndexObject indexObject) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        Path path = getPath();
        CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, IndexObject.class);
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
