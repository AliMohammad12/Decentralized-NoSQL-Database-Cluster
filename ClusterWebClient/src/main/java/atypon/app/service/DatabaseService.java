package atypon.app.service;

import atypon.app.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class DatabaseService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    private final RestTemplate restTemplate;
    @Autowired
    public DatabaseService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    public void createDatabase(String databaseName) {
        User user = new User(UserInfo.getUsername(), UserInfo.getPassword());
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("database", databaseName);
        WriteRequest writeRequest = new WriteRequest(user, objectNode.toString(), "database/create");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        HttpEntity<Object> requestEntity = new HttpEntity<>(writeRequest, headers);
        String url = "http://localhost:9000/load-balance/write";


        // remember to show these to the user!!
        try {
            restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            logger.info("Database with the name '" + databaseName + "' has been successfully created and will be utilized.");
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 400) {
                logger.warn("An existing database with the name '" + databaseName + "' has been found and will be utilized.");
            } else if (e.getStatusCode().value() == 401) {
                logger.error("Invalid credentials! Please use correct credentials!");
            } else {
                logger.error("There's an issue within the cluster, please try connecting later!");
            }
        }
    }
    public List<String> readAllDatabases() {
        String url = "http://localhost:" + Node.getPort() + "/database/read/all";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());

        ResponseEntity<List<String>> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<String>>() {}
        );

        return responseEntity.getBody();
    }
    public void updateDatabase(String oldName, String newName) {
        User user = new User(UserInfo.getUsername(), UserInfo.getPassword());
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("oldDatabaseName", oldName);
        objectNode.put("newDatabaseName", newName);
        WriteRequest writeRequest = new WriteRequest(user, objectNode.toString(), "database/update");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        HttpEntity<Object> requestEntity = new HttpEntity<>(writeRequest, headers);
        String url = "http://localhost:9000/load-balance/write";

        restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
    }
    public void deleteDatabase(String databaseName) throws Exception {
        User user = new User(UserInfo.getUsername(), UserInfo.getPassword());
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("database", databaseName);
        WriteRequest writeRequest = new WriteRequest(user, objectNode.toString(), "database/delete");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        HttpEntity<Object> requestEntity = new HttpEntity<>(writeRequest, headers);
        String url = "http://localhost:9000/load-balance/write";

        try {
            restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
          //  logger.info("Database with the name '" + databaseName + "' has been successfully created and will be utilized.");
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 400) {
            //    logger.warn("An existing database with the name '" + databaseName + "' has been found and will be utilized.");
            } else if (e.getStatusCode().value() == 401) {
             //   logger.error("Invalid credentials! Please use correct credentials!");
                throw new Exception();
            } else {
              //  logger.error("There's an issue within the cluster, please try connecting later!");
                throw new Exception();
            }
        }

    }
}
