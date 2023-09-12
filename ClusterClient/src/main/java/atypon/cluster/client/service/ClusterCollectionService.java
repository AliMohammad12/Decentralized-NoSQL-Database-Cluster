package atypon.cluster.client.service;

import atypon.cluster.client.models.*;
import atypon.cluster.client.exception.ClusterOperationalIssueException;
import atypon.cluster.client.exception.CollectionReadException;
import atypon.cluster.client.request.WriteRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;


@Component
@DependsOn("clusterDatabaseService")
public class ClusterCollectionService {
    private static final Logger logger = LoggerFactory.getLogger(ClusterConnectionService.class);
    private final RestTemplate restTemplate;
    @Autowired
    public ClusterCollectionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    // todo: make choosing a name for the collection optional !!!
    @PostConstruct
    private void init() {
       // createCollection(Employee.class);
       // System.out.println(readCollection(NewC.class).toPrettyString());
    }
    public void createCollection(Class<?> collectionClass) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode collection = objectMapper.createObjectNode();
        ObjectNode database = objectMapper.createObjectNode();
        database.put("name", DatabaseInfo.getName());
        collection.put("name", collectionClass.getSimpleName());
        collection.put("database", database);

        Field[] fieldsArray = collectionClass.getDeclaredFields();
        ObjectNode fields = objectMapper.createObjectNode();
        for (Field field : fieldsArray) {
            String fieldName = field.getName();
            Class<?> fieldType = field.getType();
            Object dummyValue = createDummyValue(fieldType);
            fields.putPOJO(fieldName, dummyValue);
        }

        ObjectNode schema = objectMapper.createObjectNode();
        ObjectNode objectNode = objectMapper.createObjectNode();
        schema.put("fields", fields);
        schema.put("collection", collection);
        objectNode.put("collectionSchema", schema);


        String url = "http://localhost:9000/load-balance/write";
        User user = new User(UserInfo.getUsername(), UserInfo.getPassword());
        WriteRequest writeRequest = new WriteRequest(user,
                objectNode.toString(), "collection/create");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        HttpEntity<Object> requestEntity = new HttpEntity<>(writeRequest, headers);
        try {
            restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            logger.info("The collection '" + collectionClass.getSimpleName() + "' has been successfully created!");
        }catch (HttpClientErrorException e) {
            logger.warn("An existing collection with the name '" + collectionClass.getSimpleName() + "' has been found and will be utilized.");
        } catch (HttpServerErrorException e) {
            logger.error("There's an issue within the cluster, cannot create collection, please retry later!");
            throw new ClusterOperationalIssueException();
        }
    }
    public JsonNode readCollection(Class<?> collectionClass) {
        String collectionName = collectionClass.getSimpleName();
        String databaseName = DatabaseInfo.getName();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode request = objectMapper.createObjectNode();
        request.put("name", collectionName);
        request.put("database",
                objectMapper.createObjectNode().put("name", databaseName));

        String url = "http://localhost:"+Node.getPort()+"/collection/read";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        HttpEntity<Object> requestEntity = new HttpEntity<>(request, headers);

        ResponseEntity<JsonNode> responseEntity;
        try {
            responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, JsonNode.class);
            logger.info("Successfully read the '" + collectionClass.getSimpleName() + "' collection from the '" + databaseName + "' database.");
            return responseEntity.getBody();
        }catch (HttpClientErrorException e) {
            logger.error("Failed to read the collection: '" + collectionName + "'.");
            throw new CollectionReadException(collectionName);
        } catch (HttpServerErrorException e) {
            logger.error("There's an issue within the cluster, cannot create collection, please retry later!");
            throw new ClusterOperationalIssueException();
        }
    }
    private static Object createDummyValue(Class<?> fieldType) {
        if (fieldType == Integer.class || fieldType == int.class) {
            return 0;
        } else if (fieldType == Double.class || fieldType == double.class) {
            return 0.0;
        } else if (fieldType == String.class) {
            return "";
        } else {
            return true;
        }
    }
}
