package atypon.cluster.client.service;

import atypon.cluster.client.exception.RequestTimeoutException;
import atypon.cluster.client.models.*;
import atypon.cluster.client.exception.ClusterOperationalIssueException;
import atypon.cluster.client.exception.InvalidUserCredentialsException;
import atypon.cluster.client.request.WriteRequest;
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Component
@DependsOn("clusterConnectionService")
public class ClusterDatabaseService {
    private static final Logger logger = LoggerFactory.getLogger(ClusterDatabaseService.class);
    @Value("${cluster.database}")
    private String databaseName;
    private final RestTemplate restTemplate;
    @Autowired
    public ClusterDatabaseService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    @PostConstruct
    private void init() throws InterruptedException {
//        for (int j = 0; j < 10; j++) {
//            ExecutorService executor = Executors.newFixedThreadPool(1);
//            for (int i = 0; i < 1; i++) {
//                executor.execute(() -> {
//                    createDatabase();
//                });
//            }
//
//            ExecutorService executor2 = Executors.newFixedThreadPool(1);
//            for (int i = 0; i < 1; i++) {
//                executor2.execute(() -> {
//                    createDatabase();
//                });
//            }
//        }
    }
    private void createDatabase() {
        DatabaseInfo.setName(databaseName);
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
        try {
            restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            logger.info("Database with the name '" + databaseName + "' has been successfully created and will be utilized.");
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 400) {
                logger.warn("An existing database with the name '" + databaseName + "' has been found and will be utilized.");
            } else if (e.getStatusCode().value() == 401) {
                logger.error("Invalid credentials! Please use correct credentials!");
                throw new InvalidUserCredentialsException();
            } else if (e.getStatusCode().value() == 408) {
                logger.error(e.getResponseBodyAsString());
                throw new RequestTimeoutException(e.getResponseBodyAsString());
            } else {
                logger.error(e.getResponseBodyAsString());
                throw new ClusterOperationalIssueException();
            }
        }
    }
}
