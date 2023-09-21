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
import org.springframework.web.client.HttpServerErrorException;
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
    public String createDatabase(String databaseName) {
        User user = new User(UserInfo.getUsername(), UserInfo.getPassword());
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("database", databaseName);
        WriteRequest writeRequest = new WriteRequest(user, objectNode.toString(), "database/create");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        HttpEntity<Object> requestEntity = new HttpEntity<>(writeRequest, headers);
        String url = "http://load-balancer:9000/load-balance/write";

        try {
            restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            return "Database with the name '" + databaseName + "' has been successfully created!";
        } catch (HttpClientErrorException e) {
            return e.getResponseBodyAsString();
        }
    }
    public List<String> readAllDatabases() {
        String url = "http://"+Node.getName().toLowerCase()+":8080/database/read/all";
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
    public String updateDatabase(String oldName, String newName) {
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
        String url = "http://load-balancer:9000/load-balance/write";

        try {
            restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            return "Database with the name '" + oldName + "' has been successfully updated to '" + newName + "' !";
        } catch (HttpClientErrorException e) {
            return e.getResponseBodyAsString();
        } catch (HttpServerErrorException e){
            return "There's an issue within the cluster, please try again!";
        }
    }
    public String deleteDatabase(String databaseName) throws Exception {
        User user = new User(UserInfo.getUsername(), UserInfo.getPassword());
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("database", databaseName);
        WriteRequest writeRequest = new WriteRequest(user, objectNode.toString(), "database/delete");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        HttpEntity<Object> requestEntity = new HttpEntity<>(writeRequest, headers);
        String url = "http://load-balancer:9000/load-balance/write";

        try {
            restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            return "Database with the name '" + databaseName + "' has been successfully deleted!";
        } catch (HttpClientErrorException e) {
            return e.getResponseBodyAsString();
        }

    }
}
