package atypon.cluster.client.service;

import atypon.cluster.client.dbmodels.Database;
import atypon.cluster.client.dbmodels.DatabaseInfo;
import atypon.cluster.client.dbmodels.Node;
import atypon.cluster.client.dbmodels.UserInfo;
import atypon.cluster.client.request.DatabaseRequest;
import atypon.cluster.client.request.DocumentRequest;
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
        createDocument(NewC.class, new NewC("POTOTOTOTOTOT", 12, 500, true));
    }
    public <T> String createDocument(Class<?> collection, T document) throws JsonProcessingException {
        String collectionName = collection.getSimpleName();
        String url = "http://localhost:"+ Node.getPort()+"/document/create";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();

        objectNode.put("isBroadcast", false);
        ObjectNode documentNode = objectMapper.createObjectNode();
        documentNode.put("CollectionName", collectionName);
        documentNode.put("DatabaseName", databaseName);
        String documentString = objectMapper.writeValueAsString(document);
        documentNode.put("data", objectMapper.readTree(documentString));
        objectNode.put("documentNode", documentNode);
        HttpEntity<String> httpEntity = new HttpEntity<>(objectNode.toString(), headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
            logger.info("The following document has been successfully added to '" +
                    collectionName + "' collection within '" + databaseName + "' database: \n" + documentNode.get("data").toPrettyString());
        } catch (HttpClientErrorException e) {
            logger.error("Cannot add the following data to '" + collectionName + "' collection: \n" +
                    documentNode.get("data").toPrettyString() + "\nCause: " + e.getResponseBodyAsString());
        }
//        System.out.println(objectNode.toPrettyString());
//        System.out.println(response.getBody());
        return "";
    }
    public void deleteDocumentById(String id) {

    }
    public void deleteDocumentByProperty(Object property) {

    }
    public void readDocumentById(String id) {

    }
    public void readDocumentByProperty(Object property) {

    }
    public void updateDocument(String id, Object property, Object propertyNewValue) {

    }
}
