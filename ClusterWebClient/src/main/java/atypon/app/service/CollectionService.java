package atypon.app.service;

import atypon.app.model.Node;
import atypon.app.model.User;
import atypon.app.model.UserInfo;
import atypon.app.model.WriteRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CollectionService {
    private static final Logger logger = LoggerFactory.getLogger(CollectionService.class);
    private final RestTemplate restTemplate;
    @Autowired
    public CollectionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    public String createCollection(String databaseName, String collectionName,
                                 List<String> fieldNames, List<String> fieldTypes) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode collection = objectMapper.createObjectNode();
        ObjectNode database = objectMapper.createObjectNode();
        database.put("name", databaseName);
        collection.put("name", collectionName);
        collection.put("database", database);

        ObjectNode fields = objectMapper.createObjectNode();
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldType = fieldTypes.get(i);
            Object dummyValue = createDummyValue(fieldType);
            fields.putPOJO(fieldName, dummyValue);
        }

        ObjectNode schema = objectMapper.createObjectNode();
        ObjectNode objectNode = objectMapper.createObjectNode();
        schema.put("fields", fields);
        schema.put("collection", collection);
        objectNode.put("collectionSchema", schema);


        String url = "http://load-balancer:9000/load-balance/write";
        User user = new User(UserInfo.getUsername(), UserInfo.getPassword());
        WriteRequest writeRequest = new WriteRequest(user,
                objectNode.toString(), "collection/create");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        HttpEntity<Object> requestEntity = new HttpEntity<>(writeRequest, headers);
        try {
            restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            return "The collection '" + collectionName + "' has been successfully created!";
        }catch (HttpClientErrorException e) {
            return e.getResponseBodyAsString();
        } catch (HttpServerErrorException e) {
            return "Cannot create collection, please retry again!";
        }
    }
    public List<String> readAllCollections(String databaseName) {
        String url = "http://"+Node.getName().toLowerCase()+":8080/database/read/database";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode database = objectMapper.createObjectNode();
        database.put("name", databaseName);

        HttpEntity<Object> requestEntity = new HttpEntity<>(database.toString(), headers);

        ResponseEntity<List<String>> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<List<String>>() {}
        );

        return responseEntity.getBody();
    }
    public JsonNode readFields(String dbName, String collectionName) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode request = objectMapper.createObjectNode();
        ObjectNode database = objectMapper.createObjectNode();
        database.put("name", dbName);
        request.put("name", collectionName);
        request.put("database", database);

        String url = "http://"+Node.getName().toLowerCase()+":8080/collection/read-fields";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        HttpEntity<Object> requestEntity = new HttpEntity<>(request.toString(), headers);

        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                JsonNode.class
        );
        return responseEntity.getBody();
    }
    public ArrayNode readCollection(String dbName, String collectionName) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode request = objectMapper.createObjectNode();
        ObjectNode database = objectMapper.createObjectNode();
        database.put("name", dbName);
        request.put("name", collectionName);
        request.put("database", database);

        String url = "http://"+Node.getName().toLowerCase()+":8080/collection/read";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        HttpEntity<Object> requestEntity = new HttpEntity<>(request.toString(), headers);

        ResponseEntity<ArrayNode> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                ArrayNode.class
        );
        return responseEntity.getBody();
    }
    public String updateCollection(String oldName, String newName, String dbName) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode request = objectMapper.createObjectNode();
        request.put("oldCollectionName", oldName);
        request.put("newCollectionName", newName);
        request.put("databaseName", dbName);

        String url = "http://load-balancer:9000/load-balance/write";
        User user = new User(UserInfo.getUsername(), UserInfo.getPassword());
        WriteRequest writeRequest = new WriteRequest(user,
                request.toString(), "collection/update");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        HttpEntity<Object> requestEntity = new HttpEntity<>(writeRequest, headers);
        try {
            restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            return "The the name of the collection '" + oldName + "' has been successfully updated to '" + newName + "' !";
        }catch (HttpClientErrorException e) {
            return e.getResponseBodyAsString();
        } catch (HttpServerErrorException e) {
            return "Cannot update collection, please retry later!";
        }
    }
    public String deleteCollection(String databaseName, String collectionName) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode collection = objectMapper.createObjectNode();
        ObjectNode database = objectMapper.createObjectNode();
        ObjectNode request = objectMapper.createObjectNode();
        database.put("name", databaseName);
        collection.put("name", collectionName);
        collection.put("database", database);
        request.put("collection", collection);

        String url = "http://load-balancer:9000/load-balance/write";
        User user = new User(UserInfo.getUsername(), UserInfo.getPassword());
        WriteRequest writeRequest = new WriteRequest(user,
                request.toString(), "collection/delete");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        HttpEntity<Object> requestEntity = new HttpEntity<>(writeRequest, headers);
        try {
            restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            return "The collection '" + collectionName + "' has been successfully deleted!";
        }catch (HttpClientErrorException e) {
            return e.getResponseBodyAsString();
        } catch (HttpServerErrorException e) {
            return "Cannot delete collection, please try again!";
        }
    }

    private static Object createDummyValue(String fieldType) {
        if (fieldType.equals("integer")) {
            return 0;
        } else if (fieldType.equals("double")) {
            return 0.0;
        } else if (fieldType.equals("string")) {
            return "";
        } else {
            return true;
        }
    }
}
