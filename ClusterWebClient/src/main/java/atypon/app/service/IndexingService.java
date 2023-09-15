package atypon.app.service;

import atypon.app.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class IndexingService {
    private static final Logger logger = LoggerFactory.getLogger(IndexingService.class);
    private final RestTemplate restTemplate;
    @Autowired
    public IndexingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    public void createIndexing(IndexObject indexObject) {
        String url = "http://localhost:9000/load-balance/write";
        User user = new User(UserInfo.getUsername(), UserInfo.getPassword());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        WriteRequest writeRequest = new WriteRequest(user, indexObject, "indexing/create");
        HttpEntity<Object> requestEntity = new HttpEntity<>(writeRequest, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
        );
    }
    public void deleteIndexing(IndexObject indexObject) {
        String url = "http://localhost:9000/load-balance/write";
        User user = new User(UserInfo.getUsername(), UserInfo.getPassword());


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        WriteRequest writeRequest = new WriteRequest(user, indexObject, "indexing/delete");
        HttpEntity<Object> requestEntity = new HttpEntity<>(writeRequest, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
        );
    }
    public boolean isIndexed(IndexObject indexObject) {
        String url = "http://localhost:" + Node.getPort() + "/indexing/status";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        HttpEntity<Object> requestEntity = new HttpEntity<>(indexObject, headers);

        ResponseEntity<Boolean> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                Boolean.class
        );
        return responseEntity.getBody();
    }
}
