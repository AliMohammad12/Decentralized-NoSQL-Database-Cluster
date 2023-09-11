package atypon.cluster.client.service;

import atypon.cluster.client.dbmodels.*;
import atypon.cluster.client.exception.ClusterOperationalIssueException;
import atypon.cluster.client.exception.DocumentCreationException;
import atypon.cluster.client.exception.DocumentDeletionException;
import atypon.cluster.client.request.DatabaseRequest;
import atypon.cluster.client.request.DocumentRequest;
import atypon.cluster.client.request.Property;
import atypon.cluster.client.request.WriteRequest;
import atypon.cluster.client.testmodels.Employee;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Component
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
    public void init() throws JsonProcessingException {
        // createDocument(NewC.class, new NewC("Baba", 20, 1200, false));
        // deleteDocumentById(NewC.class, "dd051cb5-e096-4dde-a93f-c2e1a34faea7");
        // deleteDocumentByProperty(NewC.class, new Property("age", 20));

    }
    public <T> String createDocument(Class<?> collection, T document) throws JsonProcessingException {
        String collectionName = collection.getSimpleName();

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();

        ObjectNode documentNode = objectMapper.createObjectNode();
        documentNode.put("CollectionName", collectionName);
        documentNode.put("DatabaseName", databaseName);
        String documentString = objectMapper.writeValueAsString(document);
        documentNode.put("data", objectMapper.readTree(documentString));
        objectNode.put("documentNode", documentNode);

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
    public void updateDocument(Class<?> collection, String id, Property... properties) {
        // Your method logic here
        // Iterate over the 'properties' array and process each property
        for (Object property : properties) {
            // Process 'property' here
        }
    }
    public void readDocumentById(String id) {

    }
    public void readDocumentByProperty(Object property) {

    }
}
