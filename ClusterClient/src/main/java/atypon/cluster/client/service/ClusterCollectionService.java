package atypon.cluster.client.service;

import atypon.cluster.client.dbmodels.*;
import atypon.cluster.client.exception.ClusterOperationalIssueException;
import atypon.cluster.client.exception.CollectionCreationException;
import atypon.cluster.client.exception.CollectionReadException;
import atypon.cluster.client.exception.InvalidUserCredentialsException;
import atypon.cluster.client.request.CreateCollectionRequest;
import atypon.cluster.client.schema.CollectionSchema;
import atypon.cluster.client.testmodels.qwer;
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

import javax.xml.crypto.Data;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
@DependsOn("clusterDatabaseService")
public class ClusterCollectionService {
    private static final Logger logger = LoggerFactory.getLogger(ClusterConnectionService.class);
    private final RestTemplate restTemplate;
    @Autowired
    public ClusterCollectionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void init() {
        readCollection(qwer.class);
    }
    public void createCollection(Class<?> collectionClass) { // make it load-balanced don't forget
        CollectionSchema collectionSchema = new CollectionSchema();
        String databaseName = DatabaseInfo.getName();
        Field[] fields = collectionClass.getDeclaredFields();
        Collection collection = new Collection();
        collection.setName(collectionClass.getSimpleName());
        collection.setDatabase(new Database(databaseName));
        collectionSchema.setCollection(collection);
        Map<String, Object> schemaFields = new HashMap<>();
        for (Field field : fields) {
            String fieldName = field.getName();
            Class<?> fieldType = field.getType();
            schemaFields.put(fieldName, createDummyValue(fieldType));
        }
        collectionSchema.setFields(schemaFields);
        String url = "http://localhost:"+Node.getPort()+"/collection/create";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        HttpEntity<CreateCollectionRequest> requestEntity = new HttpEntity<>(new CreateCollectionRequest(collectionSchema), headers);
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
    public void readCollection(Class<?> collectionClass) {
        String collectionName = collectionClass.getSimpleName();
        String databaseName = DatabaseInfo.getName();
        Collection collection = new Collection(collectionName, new Database(databaseName));

        String url = "http://localhost:"+Node.getPort()+"/collection/read";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        HttpEntity<Collection> requestEntity = new HttpEntity<>(collection, headers);

        ResponseEntity<?> responseEntity;
        try {
            responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            logger.info("Successfully read the '" + collectionClass.getSimpleName() + "' collection from the '" + databaseName + "' database.");
        }catch (HttpClientErrorException e) {
            logger.error("Failed to read the collection: '" + collectionName + "'.");
            throw new CollectionReadException(collectionName);
        } catch (HttpServerErrorException e) {
            logger.error("There's an issue within the cluster, cannot create collection, please retry later!");
            throw new ClusterOperationalIssueException();
        }
        System.out.println(responseEntity.getBody());
    }

    public static Object createDummyValue(Class<?> fieldType) {
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
