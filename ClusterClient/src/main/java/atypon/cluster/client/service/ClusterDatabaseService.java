package atypon.cluster.client.service;

import atypon.cluster.client.dbmodels.DatabaseInfo;
import atypon.cluster.client.exception.ClusterOperationalIssueException;
import atypon.cluster.client.exception.InvalidUserCredentialsException;
import atypon.cluster.client.dbmodels.Database;
import atypon.cluster.client.dbmodels.Node;
import atypon.cluster.client.dbmodels.UserInfo;
import atypon.cluster.client.request.DatabaseRequest;
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
    public void init() { // create new method called CreateDatabase
        String url = "http://localhost:"+Node.getPort()+"/database/create";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        DatabaseRequest databaseRequest = new DatabaseRequest();
        databaseRequest.setDatabase(new Database(databaseName));
        headers.setBasicAuth(UserInfo.getUsername(), UserInfo.getPassword());
        HttpEntity<Object> requestEntity = new HttpEntity<>(databaseRequest, headers);
        DatabaseInfo.setName(databaseName);
        try {
            restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            logger.info("Database with the name '" + databaseName + "' has been successfully created and will be utilized.");
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 400) {
                logger.warn("An existing database with the name '" + databaseName + "' has been found and will be utilized.");
            } else if (e.getStatusCode().value() == 401) {
                logger.error("Invalid credentials! Please use correct credentials!");
                throw new InvalidUserCredentialsException();
            } else {
                logger.error("There's an issue within the cluster, please try connecting later!");
                throw new ClusterOperationalIssueException();
            }
        }
    }
}
