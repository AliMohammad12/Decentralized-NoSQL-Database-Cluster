package atypon.app.service;

import atypon.app.model.Database;
import atypon.app.model.Node;
import atypon.app.model.UserInfo;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.xml.crypto.Data;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DatabaseService {
    private final RestTemplate restTemplate;
    @Autowired
    public DatabaseService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    public void createDatabase() {

    }
    public List<Database> readAllDatabases() {
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

        List<String> databaseNames = responseEntity.getBody();

        List<Database> databases = databaseNames.stream()
                .map(Database::new)
                .collect(Collectors.toList());

        System.out.println(databaseNames);
        return databases;
    }
    public void readDatabase() {

    }
    public void updateDatabase() {

    }
    public void deleteDatabase() {

    }
}
