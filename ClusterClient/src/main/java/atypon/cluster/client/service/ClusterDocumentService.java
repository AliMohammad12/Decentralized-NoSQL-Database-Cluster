package atypon.cluster.client.service;

import atypon.cluster.client.models.*;
import atypon.cluster.client.exception.*;

import atypon.cluster.client.request.Property;
import atypon.cluster.client.request.WriteRequest;
import atypon.cluster.client.testmodels.NewC;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@DependsOn("clusterDatabaseService")
public class ClusterDocumentService {
    private static final Logger logger = LoggerFactory.getLogger(ClusterConnectionService.class);
    private final RestTemplate restTemplate;
    @Value("${cluster.database}")
    private String databaseName;
    @Autowired
    public ClusterDocumentService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void init() throws JsonProcessingException, DocumentReadingException {
        // createDocument(NewC.class, new NewC("OhMama", 23, 1200, true));
//        createDocument(NewC.class, new NewC("Ahmad", 20, 1200, false));
//        createDocument(NewC.class, new NewC("Mumen", 21, 1500, true));
//        createDocument(NewC.class, new NewC("ABCCC", 21, 1200, false));

        // deleteDocumentById(NewC.class, "dd051cb5-e096-4dde-a93f-c2e1a34faea7");
        // deleteDocumentByProperty(NewC.class, new Property("age", 23));
        // readDocumentById(NewC.class, "3c0f2ad1-f163-4a64-b6ce-4017d6123ccc4ac");
//        updateDocument(NewC.class, "3c0f2ad1-f163-4a64-b6ce-4017d6ccc4ac",
//                new Property("name", "Ahmad"),
//                new Property("age", 25),
//                new Property("salary", 1500.20));

        // readDocumentByProperty(NewC.class, new Property("age", 20));
    }
    public <T> String createDocument(Class<?> collection, T document) throws JsonProcessingException {
        String collectionName = collection.getSimpleName();

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();

        System.out.println(Node.getName());
        ObjectNode documentNode = objectMapper.createObjectNode();
        documentNode.put("CollectionName", collectionName);
        documentNode.put("DatabaseName", databaseName);
        documentNode.put("NodeName", Node.getName());
        String documentString = objectMapper.writeValueAsString(document);
        documentNode.put("data", objectMapper.readTree(documentString));
        objectNode.put("documentNode", documentNode);


        System.out.println(objectNode.toPrettyString());


        User user = new User(UserInfo.getUsername(), UserInfo.getPassword());
        WriteRequest writeRequest = new WriteRequest(user, objectNode.toString(), "document/create");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        HttpEntity<Object> httpEntity = new HttpEntity<>(writeRequest, headers);
        String url = "http://localhost:9000/load-balance/write";

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
            logger.info("The following document has been successfully added to '" +
                    collectionName + "' collection within '" + databaseName + "' database: \n" + documentNode.get("data").toPrettyString());
            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Cannot add the following data to '" + collectionName + "' collection: \n" +
                    documentNode.get("data").toPrettyString() + "\nCause: " + e.getResponseBodyAsString());
            throw new DocumentCreationException(documentNode.get("data").toPrettyString());
        } catch(HttpServerErrorException e) {
            logger.error("There's an issue within the cluster, please try connecting later!");
            throw new ClusterOperationalIssueException();
        }
    }
    public void deleteDocumentById(Class<?> collection, String id) {
        String collectionName = collection.getSimpleName();

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        ObjectNode documentNode = objectMapper.createObjectNode();
        ObjectNode idNode = objectMapper.createObjectNode();
        idNode.put("id", id);
        documentNode.put("CollectionName", collectionName);
        documentNode.put("DatabaseName", databaseName);
        documentNode.put("data", idNode);
        objectNode.put("documentNode", documentNode);

        User user = new User(UserInfo.getUsername(), UserInfo.getPassword());
        WriteRequest writeRequest = new WriteRequest(user, objectNode.toString(), "document/delete-id");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        HttpEntity<Object> httpEntity = new HttpEntity<>(writeRequest, headers);
        String url = "http://localhost:9000/load-balance/write";

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
            logger.info("The document with id '" + id + "' has been successfully deleted");
        } catch (HttpClientErrorException e) {
            logger.error(e.getResponseBodyAsString());
            throw new DocumentDeletionException(id);
        } catch(HttpServerErrorException e) {
            logger.error("There's an issue within the cluster, please try connecting later!");
            throw new ClusterOperationalIssueException();
        }
    }
    public void deleteDocumentByProperty(Class<?> collection, Property property) {
        String collectionName = collection.getSimpleName();

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode documentNode = objectMapper.createObjectNode();
        ObjectNode propertyNode = objectMapper.createObjectNode();
        propertyNode.put("name", property.getName());
        if (property.isStringValue()) {
            propertyNode.put("value", (String) property.getValue());
        } else if (property.isDoubleValue()) {
            propertyNode.put("value", (double) property.getValue());
        } else if (property.isBooleanValue()) {
            propertyNode.put("value", (boolean) property.getValue());
        } else {
            propertyNode.put("value", (int) property.getValue());
        }
        documentNode.put("database", databaseName);
        documentNode.put("collection", collectionName);
        documentNode.put("property", propertyNode);
        documentNode.put("nodeNameIndexingUpdate", Node.getName());

        System.out.println(documentNode.toPrettyString());

        User user = new User(UserInfo.getUsername(), UserInfo.getPassword());
        WriteRequest writeRequest = new WriteRequest(user, documentNode.toString(), "document/delete-property");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        HttpEntity<Object> httpEntity = new HttpEntity<>(writeRequest, headers);
        String url = "http://localhost:9000/load-balance/write";

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
            logger.info("The documents with the property '{" + property.getName() + ", " + property.getValue() +"}' " +
                    "have been successfully deleted");
        } catch (HttpClientErrorException e) {
            logger.error(e.getResponseBodyAsString());
            throw new DocumentDeletionException(e.getResponseBodyAsString());
        } catch(HttpServerErrorException e) {
            logger.error("There's an issue within the cluster, please try connecting later!");
            throw new ClusterOperationalIssueException();
        }
    }
    public void updateDocument(Class<?> collection, String id, Property... properties) throws DocumentReadingException {
        String collectionName = collection.getSimpleName();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode request = objectMapper.createObjectNode();
        ObjectNode updateRequest = objectMapper.createObjectNode();
        ObjectNode info = objectMapper.createObjectNode();
        ObjectNode data = objectMapper.createObjectNode();
        for (Property property : properties) {
            if (property.isStringValue()) {
                data.put(property.getName(), (String) property.getValue());
            } else if (property.isDoubleValue()) {
                data.put(property.getName(), (double) property.getValue());
            } else if (property.isBooleanValue()) {
                data.put(property.getName(), (boolean) property.getValue());
            } else {
                data.put(property.getName(), (int) property.getValue());
            }
        }

        JsonNode jsonNode = readDocumentById(collection, id);
        int version = jsonNode.get("version").asInt();
        info.put("version", version);
        info.put("id", id);
        info.put("NodeName", Node.getName());
        updateRequest.put("CollectionName", collectionName);
        updateRequest.put("DatabaseName", databaseName);
        updateRequest.put("info", info);
        updateRequest.put("data", data);
        request.put("updateRequest", updateRequest);

        System.out.println(request.toPrettyString());

        User user = new User(UserInfo.getUsername(), UserInfo.getPassword());
        WriteRequest writeRequest = new WriteRequest(user, request.toString(), "document/update");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        HttpEntity<Object> httpEntity = new HttpEntity<>(writeRequest, headers);
        String url = "http://localhost:9000/load-balance/write";

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
            logger.info("The document with id '" + id + "' has been updated successfully!");

        } catch (HttpClientErrorException e) {
            logger.error(e.getResponseBodyAsString());
            throw new DocumentUpdateException(e.getResponseBodyAsString());
        } catch(HttpServerErrorException e) {
            logger.error("There's an issue within the cluster, please try connecting later!");
            throw new ClusterOperationalIssueException();
        }
    }
    public JsonNode readDocumentById(Class<?> collection, String id) throws DocumentReadingException {
        String collectionName = collection.getSimpleName();

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        ObjectNode documentNode = objectMapper.createObjectNode();
        ObjectNode idNode = objectMapper.createObjectNode();

        idNode.put("id", id);
        documentNode.put("CollectionName", collectionName);
        documentNode.put("DatabaseName", databaseName);
        documentNode.put("data", idNode);
        objectNode.put("documentNode", documentNode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        HttpEntity<Object> httpEntity = new HttpEntity<>(objectNode.toPrettyString(), headers);
        String url = "http://localhost:"+Node.getPort()+"/document/read-id";

        ResponseEntity<JsonNode> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, JsonNode.class);
            logger.info("Successfully read the document with id '" + id + "' !\n" + response.getBody().toPrettyString());
            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Could not read the document with id: '" + id + "'!");
            throw new DocumentReadingException(e.getResponseBodyAsString());
        } catch(HttpServerErrorException e) {
            logger.error("There's an issue within the cluster, please try connecting later!");
            throw new ClusterOperationalIssueException();
        }
    }
    public JsonNode readDocumentByProperty(Class<?> collection, Property property) {
        String collectionName = collection.getSimpleName();

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode documentNode = objectMapper.createObjectNode();
        ObjectNode propertyNode = objectMapper.createObjectNode();

        propertyNode.put("name", property.getName());
        if (property.isStringValue()) {
            propertyNode.put("value", (String) property.getValue());
        } else if (property.isDoubleValue()) {
            propertyNode.put("value", (double) property.getValue());
        } else if (property.isBooleanValue()) {
            propertyNode.put("value", (boolean) property.getValue());
        } else {
            propertyNode.put("value", (int) property.getValue());
        }
        documentNode.put("database", databaseName);
        documentNode.put("collection", collectionName);
        documentNode.put("property", propertyNode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        HttpEntity<Object> httpEntity = new HttpEntity<>(documentNode.toString(), headers);
        String url = "http://localhost:"+Node.getPort()+"/document/read-property";

        ResponseEntity<JsonNode> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, JsonNode.class);
            logger.info("Successfully read the documents: \n" + response.getBody().toPrettyString());
            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.error(e.getResponseBodyAsString());
            throw new CollectionReadException(e.getResponseBodyAsString());
        } catch(HttpServerErrorException e) {
            logger.error("There's an issue within the cluster, please try connecting later!");
            throw new ClusterOperationalIssueException();
        }
    }
}
