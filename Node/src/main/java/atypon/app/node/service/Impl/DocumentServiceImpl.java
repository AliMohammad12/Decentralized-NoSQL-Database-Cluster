package atypon.app.node.service.Impl;

import atypon.app.node.indexing.IndexObject;
import atypon.app.node.indexing.Property;
import atypon.app.node.model.Collection;
import atypon.app.node.model.Database;
import atypon.app.node.model.Node;
import atypon.app.node.request.document.DocumentRequest;
import atypon.app.node.request.document.DocumentUpdateRequest;
import atypon.app.node.request.document.DocumentRequestByProperty;
import atypon.app.node.service.services.*;
import atypon.app.node.utility.FileOperations;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.common.protocol.types.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLOutput;
import java.util.Iterator;
import java.util.Map;

@Service
public class DocumentServiceImpl implements DocumentService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);
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
    public void addDocument(DocumentRequest request) throws JsonProcessingException {
        JsonNode documentRequest = request.getDocumentNode();
        String collectionName = documentRequest.get("CollectionName").asText();
        String databaseName = documentRequest.get("DatabaseName").asText();
        String nodeNameIndexingUpdate = documentRequest.get("NodeName").asText();
        JsonNode document = documentRequest.get("data");

        System.out.println(documentRequest.toPrettyString());

        String jsonString = jsonService.convertJsonToString(document);
        Path path = getPath().resolve(databaseName).resolve("Collections").resolve(collectionName).resolve("Documents");
        FileOperations.writeJsonAtLocation(jsonString, path.toString(), document.get("id").asText() + ".json");

        if (nodeNameIndexingUpdate.equals(Node.getName())) {
            indexingService.indexDocumentPropertiesIfExists(databaseName, collectionName, document);
        }
        logger.info("Successfully created the document: \n" + document.toPrettyString());
    }
    @Override
    public ArrayNode readDocumentProperty(DocumentRequestByProperty request) throws IOException {
        logger.info("Reading documents with property '" + request.getProperty() + "' in database '" +
                "" + request.getDatabase() +"' in collection '" + request.getCollection() + "' !");

        String collectionName = request.getCollection();
        String databaseName = request.getDatabase();
        Property property = request.getProperty();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();

        IndexObject indexObject = new IndexObject(user.getUsername(), databaseName, collectionName, property.getName());
        if (indexingService.isIndexed(indexObject)) {
            return indexingService.readDocumentsByProperty(databaseName, collectionName, property);
        }

        Path path = getPath().resolve(databaseName)
                .resolve("Collections")
                .resolve(collectionName)
                .resolve("Documents");

        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode documentsArray = objectMapper.createArrayNode();
        ArrayNode arrayNode = jsonService.readJsonArray(path.toString());
        for (JsonNode element : arrayNode) {
            JsonNode propertyNode = element.get(property.getName());
            if (propertyNode.isBoolean() && property.isBooleanValue()) {
                boolean value = propertyNode.asBoolean();
                if (property.getValue().equals(value)) {
                    documentsArray.add(element);
                }
            } else if (propertyNode.isDouble() && property.isDoubleValue()) {
                double value = propertyNode.asDouble();
                if (property.getValue().equals(value)) {
                    documentsArray.add(element);
                }
            } else if (propertyNode.isInt() && property.isIntegerValue()) {
                int value = propertyNode.asInt();
                if (property.getValue().equals(value)) {
                    documentsArray.add(element);
                }
            } else if (propertyNode.isTextual() && property.isStringValue()) {
                String value = propertyNode.asText();
                if (property.getValue().equals(value)) {
                    documentsArray.add(element);
                }
            }
        }
        return documentsArray;
    }
    @Override
    public void deleteDocumentByProperty(DocumentRequestByProperty request) throws IOException { // DocumentRequestByProperty
        logger.info("Deleting documents with property '" + request.getProperty() + "' in database '" +
                "" + request.getDatabase() +"' in collection '" + request.getCollection() + "' !");
        String collectionName = request.getCollection();
        String databaseName = request.getDatabase();
        Property property = request.getProperty();
        String nodeNameIndexingUpdate = request.getNodeNameIndexingUpdate();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();

        IndexObject indexObject = new IndexObject(user.getUsername(), databaseName, collectionName, property.getName());
        if (nodeNameIndexingUpdate.equals(Node.getName())) {
            if (indexingService.isIndexed(indexObject)) {
                indexingService.deleteDocumentByProperty(databaseName, collectionName, property);
                return;
            }
        }

        Path path = getPath().resolve(databaseName)
                .resolve("Collections")
                .resolve(collectionName)
                .resolve("Documents");


        // todo: must remove the deleted ID's from the tree --> lazy deletion
        //  when we read a document from the tree we iterate at the list and
        //  delete the id's that don't exist. (DONE)

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
    public JsonNode readDocumentById(String database, String collection, JsonNode document) throws IOException {
        String id = document.get("id").asText();
        logger.info("Reading document with id '" + id + "' in " +
                "database '" + database + "' in collection '" + collection + "' !");

        Path path = getPath().resolve(database)
                .resolve("Collections")
                .resolve(collection)
                .resolve("Documents")
                .resolve(id + ".json");
        return jsonService.readJsonNode(path.toString());
    }
    @Override
    public void deleteDocumentById(String database, String collection, JsonNode document) throws IOException {
        String id = document.get("id").asText();
        logger.info("Deleting document with id '" + id + "' in " +
                "database '" + database + "' in collection '" + collection + "' !");

        Path path = getPath().resolve(database)
                .resolve("Collections")
                .resolve(collection)
                .resolve("Documents")
                .resolve(id + ".json");
        FileOperations.deleteFile(path.toString());
    }
    @Override
    public void updateDocument(DocumentUpdateRequest request) throws IOException { // DocumentUpdateRequest
        JsonNode updateRequest = request.getUpdateRequest();
        String collection = updateRequest.get("CollectionName").asText();
        String database = updateRequest.get("DatabaseName").asText();
        JsonNode documentInfo = updateRequest.get("info");
        JsonNode documentBeforeUpdate = readDocumentById(database, collection, documentInfo);

        int versionNumber = documentBeforeUpdate.get("version").asInt();
        int requestVersionNumber = documentInfo.get("version").asInt();
        if (versionNumber == requestVersionNumber) {
            // - check for each property if it's indexed, if it's indexed we need to fix our bPlusTree
            JsonNode documentData = updateRequest.get("data");
            String id = documentInfo.get("id").asText();
            String nodeNameIndexingUpdate = documentInfo.get("NodeName").asText();
            logger.info("Updating the document with id '" + id + "' !");
            Iterator<Map.Entry<String, JsonNode>> fieldsIterator = documentData.fields();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails user = (UserDetails) authentication.getPrincipal();
            String username = user.getUsername();
            while (fieldsIterator.hasNext()) {
                Map.Entry<String, JsonNode> field = fieldsIterator.next();
                String fieldName = field.getKey();
                JsonNode fieldValue = field.getValue();
                IndexObject indexObject = new IndexObject(username, database, collection, fieldName);

                if (nodeNameIndexingUpdate.equals(Node.getName())) {
                    if (indexingService.isIndexed(indexObject)) {
                        indexingService.updateIndexing(id, fieldValue, documentBeforeUpdate.get(fieldName), indexObject);
                    }
                }
                ((ObjectNode) documentBeforeUpdate).put(fieldName, fieldValue);
            }
            ((ObjectNode) documentBeforeUpdate).put("version", versionNumber + 1);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);  // pretty-printing
            Path path = getPath()
                    .resolve(database)
                    .resolve("Collections")
                    .resolve(collection)
                    .resolve("Documents")
                    .resolve(id + ".json");
            objectMapper.writeValue(new File(path.toString()), documentBeforeUpdate);
        } else {
            throw new OptimisticLockingFailureException("Concurrent update detected for document: '\n" + documentBeforeUpdate.toPrettyString());
        }
    }

}
