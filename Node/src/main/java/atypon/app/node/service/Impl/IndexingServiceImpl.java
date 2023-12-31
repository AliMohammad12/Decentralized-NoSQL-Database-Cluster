package atypon.app.node.service.Impl;

import atypon.app.node.indexing.IndexObject;
import atypon.app.node.indexing.Property;
import atypon.app.node.indexing.bplustree.BPlusTree;
import atypon.app.node.model.Collection;
import atypon.app.node.model.Database;
import atypon.app.node.model.Node;
import atypon.app.node.service.services.IndexingService;
import atypon.app.node.service.services.JsonService;
import atypon.app.node.utility.DiskOperations;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
/*
    1- Index Creation: Create Tree in the specified collection and set it UP
    then serialize the tree into the Indexing directory inside the collection.
    then add the tree to indexRegistry (main memory to be ready to use)

    2- Index Deletion: Delete Tree serialized file in the indexing directory,
    and clear it from the main memory.

    3- Connect: Initialize indexing by deserialization of all the indexing in the files and
    putting them in the main memory.

    4- Disconnect: Clear Indexing from main memory and serialize the trees again.

    ...................................................................
    -I had two options
    1- deserialize the tree from the disk each time we use indexing (Reads/Deletes by property) - Many disk operations => Slow/Memory Efficient.
    2- deserialize the trees and store them in main memory when the user connects, so they can be ready any time the user => Fast/Bad Memory.


    Imagine we have one user, that connects from 20 devices, let's say that it's a company that registered in our database.
    Each device in that company connects to the database BUT SAME USER and does one operation:

    -We choose 1:
    The number of disk operations will be NUMBER_OF_DEVICES * NUMBER_DISK_OPERATIONS_FOR_EACH_QUERY.

    -We choose 2:
    The number of disk operations => 1 (When the first device connects, we set up the trees).
    and this one is extremely fast because B+Trees deserialization are fast to deserialize.
    then even if we have a million devices connected they will have the same speed exactly!


    I'm choosing 2!
 */
@Service
public class IndexingServiceImpl implements IndexingService {
    private static final Logger logger = LoggerFactory.getLogger(IndexingService.class);
    private final HashMap<IndexObject, BPlusTree> indexRegistry;
    private final JsonService jsonService;
    @Autowired
    public IndexingServiceImpl(@Qualifier("indexRegistry") HashMap<IndexObject, BPlusTree> indexRegistry,
                               JsonService jsonService) {
        this.indexRegistry = indexRegistry;
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
        logger.info("Creating indexing: " + indexObject.toString());
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
        logger.info("Initializing indexing!");
        JsonNode jsonNode = jsonService.readJsonNode(getPath().resolve("Databases").resolve("Indexing.json").toString());
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
        logger.info("Deleting indexing: " + indexObject.toString());
        JsonNode jsonNode = jsonService.readJsonNode(getPath().resolve("Databases").resolve("Indexing.json").toString());
        File jsonFile = new File(getPath().resolve("Databases").resolve("Indexing.json").toString());
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
        logger.info("Destructing indexing tree!");
        String username = getUsername();
        Iterator<Map.Entry<IndexObject, BPlusTree>> iterator = indexRegistry.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<IndexObject, BPlusTree> entry = iterator.next();
            IndexObject indexObject = entry.getKey();
            if (indexObject.getUsername().equals(username)) {
                logger.info("Clearing Index: " + indexObject + " from B+Tree!");
                iterator.remove();
            }
        }
    }
    @Override
    public void indexDocumentPropertiesIfExists(String database, String collection, JsonNode document) {
        String username = getUsername();
        String id = document.get("id").asText();
        Iterator<String> iterator = document.fieldNames();
        while (iterator.hasNext()) {
            String field = iterator.next();
            if (field.equals("id")) continue;
            if (field.equals("version")) continue;
            IndexObject indexObject = new IndexObject(username, database, collection, field);
            String type = jsonService.getPropertyTypeFromSchema(indexObject);
            JsonNode node = document.get(field);

            if (isIndexed(indexObject)) {
                BPlusTree bPlusTree = indexRegistry.get(indexObject);
                addDocumentToTree(bPlusTree, id, type, node, field);
            }
        }
    }
    @Override
    public boolean isIndexed(IndexObject indexObject) {
        return indexRegistry.containsKey(indexObject);
    }

    @Override // Fixed
    public List<String> retrieveAndRemoveByProperty(String database, String collection, Property property)  {
        logger.info("Retrieving documents and removing them from tree " +
                "in database '" + database + "' within collection '"
                + collection + "' with property '" + property.toString() + "' !" );
        String username = getUsername();
        IndexObject indexObject = new IndexObject(username, database, collection, property.getName());
        BPlusTree bPlusTree = indexRegistry.get(indexObject);

        List<String> documentList = null;
        if (property.isIntegerValue()) {
            int value = (int) property.getValue();
            documentList = (List<String>) bPlusTree.search(value);
            if (documentList != null) {
                logger.info("Deleting B+Tree Node with value: " + value);
                bPlusTree.delete(value);
            }
        } else if (property.isDoubleValue()) {
            double value = (double) property.getValue();
            documentList = (List<String>) bPlusTree.search(value);
            if (documentList != null) {
                logger.info("Deleting B+Tree Node with value: " + value);
                bPlusTree.delete(value);
            }
        } else if (property.isStringValue()) {
            String value = (String) property.getValue();
            documentList = (List<String>) bPlusTree.search(value);
            if (documentList != null) {
                logger.info("Deleting B+Tree Node with value: " + value);
                bPlusTree.delete(value);
            }
        } else if (property.isBooleanValue()) {
            boolean value = (boolean) property.getValue();
            documentList = (List<String>) bPlusTree.search(value);
            if (documentList != null) {
                logger.info("Deleting B+Tree Node with value: " + value);
                bPlusTree.delete(value);
            }
        }
        return documentList;
    }
    @Override
    public List<String> retrieveByProperty(String database, String collection, Property property){
        logger.info("Retrieving documents in database '" + database + "' within collection '"
                + collection + "' with property '" + property.toString() + "' !" );
        String username = getUsername();
        IndexObject indexObject = new IndexObject(username, database, collection, property.getName());
        BPlusTree bPlusTree = indexRegistry.get(indexObject);
        List<String> documentList =  null;
        if (property.isIntegerValue()) {
            int value = (int) property.getValue();
            documentList = (List<String>) bPlusTree.search(value);

        } else if (property.isDoubleValue()) {
            double value = (double) property.getValue();
            documentList = (List<String>) bPlusTree.search(value);

        } else if (property.isStringValue()) {
            String value = (String) property.getValue();
            documentList = (List<String>) bPlusTree.search(value);

        } else if (property.isBooleanValue()) {
            boolean value = (boolean) property.getValue();
            documentList = (List<String>) bPlusTree.search(value);
        }
        return documentList;
    }

    @Override
    public void updateIndexing(String id, JsonNode newValue, JsonNode oldValue, IndexObject indexObject){
        logger.info("Updating B+Tree!");
        BPlusTree bPlusTree = indexRegistry.get(indexObject);
        String type = jsonService.getPropertyTypeFromSchema(indexObject);

        List<String> listNew = null;
        List<String> listOld = null;
        switch (type) {
            case "string":
                String newValueString = newValue.asText();
                String oldValueString = oldValue.asText();

                logger.info("Removing document with the id '" + id +"' from " +
                        "Tree's Node with value '" + oldValueString+"' and " +
                        "adding it to Tree's Node with value '" + newValueString + "' !");

                listNew = (List<String>) bPlusTree.search(newValueString);
                listOld = (List<String>) bPlusTree.search(oldValueString);

                listOld.remove(id);
                if (listOld.isEmpty()) {
                    bPlusTree.delete(oldValueString);
                }
                if (listNew == null) {
                    listNew = new ArrayList<>();
                    listNew.add(id);
                    bPlusTree.insert(newValueString, listNew);
                } else {
                    listNew.add(id);
                }
                break;
            case "integer":
                int newValueInt = newValue.asInt();
                int oldValueInt = oldValue.asInt();

                logger.info("Removing document with the id '" + id +"' from " +
                        "Tree's Node with value '" + oldValueInt +"' and " +
                        "adding it to Tree's Node with value '" + newValueInt + "' !");

                listNew = (List<String>) bPlusTree.search(newValueInt);
                listOld = (List<String>) bPlusTree.search(oldValueInt);

                listOld.remove(id);
                if (listOld.isEmpty()) {
                    bPlusTree.delete(oldValueInt);
                }
                if (listNew == null) {
                    listNew = new ArrayList<>();
                    listNew.add(id);
                    bPlusTree.insert(newValueInt, listNew);
                } else {
                    listNew.add(id);
                }
                break;
            case "number":
                double newValueDouble = newValue.asDouble();
                double oldValueDouble = oldValue.asDouble();

                logger.info("Removing document with the id '" + id +"' from " +
                        "Tree's Node with value '" + oldValueDouble +"' and " +
                        "adding it to Tree's Node with value '" + newValueDouble + "' !");

                listNew = (List<String>) bPlusTree.search(newValueDouble);
                listOld = (List<String>) bPlusTree.search(oldValueDouble);

                listOld.remove(id);
                if (listOld.isEmpty()) {
                    bPlusTree.delete(oldValueDouble);
                }
                if (listNew == null) {
                    listNew = new ArrayList<>();
                    listNew.add(id);
                    bPlusTree.insert(newValueDouble, listNew);
                } else {
                    listNew.add(id);
                }
                break;
            case "boolean":
                boolean newValueBoolean = newValue.asBoolean();
                boolean oldValueBoolean = oldValue.asBoolean();

                logger.info("Removing document with the id '" + id +"' from " +
                        "Tree's Node with value '" + oldValueBoolean +"' and " +
                        "adding it to Tree's Node with value '" + newValueBoolean + "' !");

                listNew = (List<String>) bPlusTree.search(newValueBoolean);
                listOld = (List<String>) bPlusTree.search(oldValueBoolean);

                listOld.remove(id);
                if (listOld.isEmpty()) {
                    bPlusTree.delete(oldValueBoolean);
                }
                if (listNew == null) {
                    listNew = new ArrayList<>();
                    listNew.add(id);
                    bPlusTree.insert(newValueBoolean, listNew);
                } else {
                    listNew.add(id);
                }
                break;
        }
    }
    @Override
    public void setupIndexing(IndexObject indexObject, BPlusTree bPlusTree, String type) throws IOException {
        Collection collection = new Collection(indexObject.getCollection(), new Database(indexObject.getDatabase()));
        ArrayNode jsonArray = readCollection(collection);
        String property = indexObject.getProperty();
        logger.info("Setting up indexing for the property '" + property + "'!");
        for (JsonNode element : jsonArray) {
            JsonNode propertyNode = element.get(property);
            String id = element.get("id").asText();
            addDocumentToTree(bPlusTree, id, type, propertyNode, property);
        }
    }
    @Override
    public ArrayNode readCollection(Collection collection) throws IOException {
        logger.info("Reading the collection with the name '" + collection.getName() + "' !");
        Path path = getPath().
                resolve("Databases").
                resolve(collection.getDatabase().getName()).
                resolve("Collections").
                resolve(collection.getName()).
                resolve("Documents");
        return jsonService.readJsonArray(path.toString());
    }
    private void addDocumentToTree(BPlusTree bPlusTree, String id, String type, JsonNode node, String propertyName) {
        logger.info("Adding id '" + id + " to the '" + propertyName + "' B+Tree inside " +
                "the node with value '" + node.asText() + "' !");
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
        File jsonFile = new File(path.resolve("Databases").resolve("Indexing.json").toString());
        if (jsonFile.exists() && jsonFile.length() > 0) {
            indexObjects = objectMapper.readValue(jsonFile, listType);
        } else {
            indexObjects = new ArrayList<>();
        }
        indexObjects.add(indexObject);
        objectMapper.writeValue(jsonFile, indexObjects);
    }
    private boolean indexExistsInFile(IndexObject indexObject) throws IOException {
        JsonNode jsonNode = jsonService.readJsonNode(getPath().resolve("Databases").resolve("Indexing.json").toString());
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
