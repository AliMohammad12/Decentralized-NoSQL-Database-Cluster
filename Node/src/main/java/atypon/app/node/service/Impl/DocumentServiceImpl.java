package atypon.app.node.service.Impl;

import atypon.app.node.caching.RedisCachingService;
import atypon.app.node.indexing.IndexObject;
import atypon.app.node.indexing.Property;
import atypon.app.node.locking.DistributedLocker;
import atypon.app.node.model.Node;
import atypon.app.node.request.document.DocumentRequest;
import atypon.app.node.request.document.DocumentUpdateRequest;
import atypon.app.node.request.document.DocumentRequestByProperty;
import atypon.app.node.service.services.*;
import atypon.app.node.utility.DiskOperations;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class DocumentServiceImpl implements DocumentService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);
    private final JsonService jsonService;
    private final IndexingService indexingService;
    private final RedisCachingService redisCachingService;
    @Autowired
    public DocumentServiceImpl(JsonService jsonService,
                               IndexingService indexingService,
                               RedisCachingService redisCachingService) {
        this.jsonService = jsonService;
        this.indexingService = indexingService;
        this.redisCachingService = redisCachingService;
    }
    private static Path getPath() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        Path path = Path.of("Storage", Node.getName(), "Users", user.getUsername(), "Databases");
        return path;
    }

    @Override // Done + Tested
    public void addDocument(DocumentRequest request) throws JsonProcessingException {
        JsonNode documentRequest = request.getDocumentNode();
        String collectionName = documentRequest.get("CollectionName").asText();
        String databaseName = documentRequest.get("DatabaseName").asText();
        String nodeNameIndexingUpdate = documentRequest.get("NodeName").asText();
        JsonNode document = documentRequest.get("data");

        String jsonString = jsonService.convertJsonToString(document);
        Path path = getPath().resolve(databaseName).resolve("Collections").resolve(collectionName).resolve("Documents");
        DiskOperations.writeToFile(jsonString, path.toString(), document.get("id").asText() + ".json");
        if (nodeNameIndexingUpdate.equals(Node.getName())) {
            indexingService.indexDocumentPropertiesIfExists(databaseName, collectionName, document);
        }
        redisCachingService.cache(document.get("id").asText() , document, 60);
        if (redisCachingService.isCached(databaseName+"/"+collectionName)) {
            redisCachingService.deleteCachedValue(databaseName+"/"+collectionName);
        }
        logger.info("Successfully created the document: \n" + document.toPrettyString());
    }

    @Override // DONE, Pending: Caching + locking test
    public ArrayNode readDocumentProperty(DocumentRequestByProperty request) throws IOException {
        logger.info("Reading documents with property '" + request.getProperty() + "' in database '" +
                "" + request.getDatabase() +"' in collection '" + request.getCollection() + "' !");

        String collectionName = request.getCollection();
        String databaseName = request.getDatabase();
        Property property = request.getProperty();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();

        Path path = getPath().resolve(databaseName)
                .resolve("Collections")
                .resolve(collectionName)
                .resolve("Documents");

        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode documentsArray = objectMapper.createArrayNode();
        IndexObject indexObject = new IndexObject(user.getUsername(), databaseName, collectionName, property.getName());
        if (indexingService.isIndexed(indexObject)) {
            List<String> documentsId = indexingService.retrieveByProperty(databaseName, collectionName, property);
            documentsArray = jsonService.readAsJsonArray(documentsId, path);
        } else {
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
        }
        return documentsArray;
    }
    @Override // DONE, Pending: Locking + Caching test!
    public void deleteDocumentByProperty(DocumentRequestByProperty request) throws IOException {
        logger.info("Deleting documents with property '" + request.getProperty() + "' in database '" +
                "" + request.getDatabase() +"' in collection '" + request.getCollection() + "' !");

        String collectionName = request.getCollection();
        String databaseName = request.getDatabase();
        Property property = request.getProperty();
        String nodeNameIndexingUpdate = request.getNodeNameIndexingUpdate();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();

        Path path = getPath().resolve(databaseName)
                .resolve("Collections")
                .resolve(collectionName)
                .resolve("Documents");

        List<String> documentsId = new ArrayList<>();
        IndexObject indexObject = new IndexObject(user.getUsername(), databaseName, collectionName, property.getName());
        if (nodeNameIndexingUpdate.equals(Node.getName()) && indexingService.isIndexed(indexObject)) {
            documentsId = indexingService.retrieveAndRemoveByProperty(databaseName, collectionName, property);
        } else {
            ArrayNode jsonArray = jsonService.readJsonArray(path.toString());
            for (JsonNode element : jsonArray) {
                JsonNode propertyNode = element.get(property.getName());
                String id = element.get("id").asText();

                if (property.isBooleanValue()) {
                    boolean value = propertyNode.asBoolean();
                    if (property.getValue().equals(value)) {
                        documentsId.add(id);
                    }
                } else if (property.isDoubleValue()) {
                    double value = propertyNode.asDouble();
                    if (property.getValue().equals(value)) {
                        documentsId.add(id);
                    }

                } else if (property.isIntegerValue()) {
                    int value = propertyNode.asInt();
                    if (property.getValue().equals(value)) {
                        documentsId.add(id);
                    }

                } else if (property.isStringValue()) {
                    String value = propertyNode.asText();
                    if (property.getValue().equals(value)) {
                        documentsId.add(id);
                    }
                }
            }
        }

        for (String id : documentsId) {
             DiskOperations.deleteFile(path.resolve(id+".json").toString());
             redisCachingService.deleteCachedValue(id);
        }
    }
    @Override // DONE + Tested
    public JsonNode readDocumentById(String database, String collection, JsonNode document) throws IOException {
        String id = document.get("id").asText();
        logger.info("Reading document with id '" + id + "' in " +
                "database '" + database + "' in collection '" + collection + "' !");

        Path path = getPath().resolve(database)
                .resolve("Collections")
                .resolve(collection)
                .resolve("Documents")
                .resolve(id + ".json");

        JsonNode result = jsonService.readJsonNode(path.toString());
        redisCachingService.cache(id, result, 60);
        return result;
    }
    @Override // Done + Tested
    public void deleteDocumentById(String database, String collection, JsonNode document) throws IOException {
        String id = document.get("id").asText();
        logger.info("Deleting document with id '" + id + "' in " +
                "database '" + database + "' in collection '" + collection + "' !");

        Path path = getPath().resolve(database)
                .resolve("Collections")
                .resolve(collection)
                .resolve("Documents")
                .resolve(id + ".json");

        if (redisCachingService.isCached(id)) {
            redisCachingService.deleteCachedValue(id);
        }
        if (redisCachingService.isCached(database+"/"+collection)) {
            redisCachingService.deleteCachedValue(database+"/"+collection);
        }
        DiskOperations.deleteFile(path.toString());
    }
    @Override // working
    public void updateDocument(DocumentUpdateRequest request) throws IOException {
        JsonNode updateRequest = request.getUpdateRequest();
        String collection = updateRequest.get("CollectionName").asText();
        String database = updateRequest.get("DatabaseName").asText();
        JsonNode documentInfo = updateRequest.get("info");
        String id = documentInfo.get("id").asText();
        JsonNode documentBeforeUpdate;
        if (redisCachingService.isCached(id)) {
            documentBeforeUpdate = (JsonNode) redisCachingService.getCachedValue(id);
        } else {
            documentBeforeUpdate = readDocumentById(database, collection, documentInfo);
        }
        int versionNumber = documentBeforeUpdate.get("version").asInt();
        int requestVersionNumber = documentInfo.get("version").asInt();
        if (versionNumber == requestVersionNumber) {
            // - check for each property if it's indexed, if it's indexed we need to fix our bPlusTree
            JsonNode documentData = updateRequest.get("data");
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
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            Path path = getPath()
                    .resolve(database)
                    .resolve("Collections")
                    .resolve(collection)
                    .resolve("Documents");

            if (redisCachingService.isCached(id)) {
                redisCachingService.deleteCachedValue(id);
                redisCachingService.cache(id, documentBeforeUpdate, 60);
            }
            if (redisCachingService.isCached(database +"/"+collection)) {
                redisCachingService.deleteCachedValue(database+"/"+collection);
            }
            String jsonString = jsonService.convertJsonToString(documentBeforeUpdate);
            DiskOperations.writeToFile(jsonString, path.toString(), id + ".json");
        } else {
            throw new OptimisticLockingFailureException("Concurrent update detected for document: '\n" + documentBeforeUpdate.toPrettyString());
        }
    }

}
