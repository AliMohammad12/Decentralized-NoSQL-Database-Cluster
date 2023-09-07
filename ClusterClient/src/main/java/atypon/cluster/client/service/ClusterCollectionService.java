package atypon.cluster.client.service;

import atypon.cluster.client.dbmodels.*;
import atypon.cluster.client.request.CreateCollectionRequest;
import atypon.cluster.client.schema.CollectionSchema;
import atypon.cluster.client.testmodels.Employee;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;


@Component
@DependsOn("clusterDatabaseService")
public class ClusterCollectionService {
    private final RestTemplate restTemplate;
    @Autowired
    public ClusterCollectionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void init() {
        createCollection(Employee.class);
    }

    public void createCollection(Class<?> userClass) {
        CollectionSchema collectionSchema = new CollectionSchema();
        String databaseName = DatabaseInfo.getName();
        Field[] fields = userClass.getDeclaredFields();
        Collection collection = new Collection();
        collection.setName(userClass.getSimpleName());
        collection.setDatabase(new Database(databaseName));
        collectionSchema.setCollection(collection);
        Map<String, Object> schemaFields = new HashMap<>();
        for (Field field : fields) {
            String fieldName = field.getName();
            String fieldType = field.getType().getSimpleName();

            schemaFields.put(fieldName, fieldType);
            System.out.println("Attribute Name: " + fieldName);
            System.out.println("Attribute Type: " + fieldType);
            System.out.println();
        }
        collectionSchema.setFields(schemaFields);
        String url = "http://localhost:"+ Node.getPort()+"/collection/create";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        HttpEntity<CreateCollectionRequest> requestEntity = new HttpEntity<>(new CreateCollectionRequest(collectionSchema), headers);
        ResponseEntity<?> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
    }
}
