package atypon.cluster.client.service;

import atypon.cluster.client.exception.ClusterOperationalIssueException;
import atypon.cluster.client.exception.InvalidUserCredentialsException;
import atypon.cluster.client.models.Node;
import atypon.cluster.client.models.NodeInfo;
import atypon.cluster.client.models.User;
import atypon.cluster.client.models.UserInfo;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
@Component
public class ClusterConnectionService {
    private static final Logger logger = LoggerFactory.getLogger(ClusterConnectionService.class);
    private final RestTemplate restTemplate;
    private final String username;
    private final String password;
    @Autowired
    public ClusterConnectionService (
            RestTemplate restTemplate,
            @Value("${cluster.username}") String username,
            @Value("${cluster.password}") String password) {
        this.restTemplate = restTemplate;
        this.username = username;
        this.password = password;
    }
    @PostConstruct
    private void init() {
        connect();
    }
    private void connect() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(username, password);
        HttpEntity<Object> requestEntity = new HttpEntity<>(new User(username, password), headers);
        ResponseEntity<NodeInfo> responseEntity;
        try {
            responseEntity = restTemplate.exchange(
                    "http://localhost:9000/api/connect", HttpMethod.POST, requestEntity, NodeInfo.class);
            NodeInfo nodeInfo = responseEntity.getBody();
            Node.setPort(nodeInfo.getPort());
            Node.setNodeId(nodeInfo.getId());
            Node.setName(nodeInfo.getName());
            UserInfo.setPassword(password);
            UserInfo.setUsername(username);
            logger.info("User '" + username + "' has been successfully connected to the cluster!");
        } catch (HttpClientErrorException e) {
            logger.error("Invalid credentials! Please use correct credentials!");
            throw new InvalidUserCredentialsException();
        } catch (HttpServerErrorException e) {
            logger.error("There's an issue within the cluster, please try connecting later!");
            throw new ClusterOperationalIssueException();
        }
    }
}