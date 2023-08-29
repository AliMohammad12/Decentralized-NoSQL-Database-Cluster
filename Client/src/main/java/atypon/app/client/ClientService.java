package atypon.app.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Service
public class ClientService {
    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity<APIResponse> callCreateCollectionEndpoint() throws IllegalAccessException {
        Employee employee = new Employee();

        Map<String, Object> schemaFields = new HashMap<>();
        Field[] fields = Employee.class.getDeclaredFields();
        for (Field field : fields) {
            System.out.println(field.getName() + " " + field.getType().getSimpleName());
            schemaFields.put(field.getName(), field.getType().getSimpleName());
        }

        CollectionSchema collectionSchema = new CollectionSchema(schemaFields, Employee.class.getSimpleName(), "DatabaseX");

        String baseUrl = "http://localhost:8081";
        String endpoint = "/create-collection";
        String url = baseUrl + endpoint;
        System.out.println(url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CollectionSchema> requestEntity = new HttpEntity<>(collectionSchema, headers);

        ResponseEntity<APIResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                APIResponse.class
        );
        return response;
    }

    public ResponseEntity<APIResponse> callCreateDocumentEndpoint() {
        Employee employee = new Employee();
        employee.setFirstName("Ahmad");
        employee.setLastName("Ali");

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode requestPayload = objectMapper.createObjectNode();

        JsonNode employeeJson = objectMapper.convertValue(employee, ObjectNode.class);
        String className = Employee.class.getSimpleName();

        requestPayload.put("CollectionName", className);
        requestPayload.set("data", employeeJson);

        String url = "http://localhost:8081/create-document";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JsonNode> requestEntity = new HttpEntity<>(requestPayload, headers);
        ResponseEntity<APIResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                APIResponse.class
        );
        return response;
    }

    public ResponseEntity<APIResponse> callCreateDatabaseEndpoint() {
        String databaseName = "DatabaseV";
        String url = "http://localhost:8081/create-database";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(databaseName, headers);
        ResponseEntity<APIResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                APIResponse.class
        );
        return response;
    }
}