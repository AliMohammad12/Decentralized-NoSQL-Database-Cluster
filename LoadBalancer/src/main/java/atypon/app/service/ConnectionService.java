package atypon.app.service;

import atypon.app.model.NodeInfo;
import atypon.app.model.User;
import atypon.app.model.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Service
public class ConnectionService {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionService.class);
    private final RestTemplate connectionRestTemplate;
    private final RestTemplate restTemplate;
    private HashMap<User, UserStatus> statusHashMap;
    @Autowired
    public ConnectionService(@Qualifier("connectionBean") RestTemplate connectionRestTemplate,
                             @Qualifier("userStatusBean") HashMap<User, UserStatus> statusHashMap,
                             @Qualifier("nonBalancedRestTemplateBean") RestTemplate restTemplate) {
        this.connectionRestTemplate = connectionRestTemplate;
        this.statusHashMap = statusHashMap;
        this.restTemplate = restTemplate;
    }
    public ResponseEntity<?> connect(User user) {
        if (!statusHashMap.containsKey(user)) {
            logger.info("Sending connection request for user '{}'!", user);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(user.getUsername(), user.getPassword());
            HttpEntity<Object> requestEntity = new HttpEntity<>(user, headers);
            try {
                ResponseEntity<NodeInfo> responseEntity = connectionRestTemplate.exchange(
                        "http://NODE/auth/connect", HttpMethod.POST, requestEntity, NodeInfo.class);
                statusHashMap.put(user, new UserStatus(responseEntity.getBody(),1));
                return responseEntity;
            } catch (HttpClientErrorException e) {
                return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
            }
        } else {
            UserStatus userStatus = statusHashMap.get(user);
            NodeInfo nodeInfo = userStatus.getNodeInfo();
            int connectedDevices = userStatus.getConnectedDevices();
            connectedDevices++;
            logger.info("User '{}' already connected, number of devices '{}' !", user, connectedDevices);

            userStatus.setConnectedDevices(connectedDevices);
            return ResponseEntity.ok(nodeInfo);
        }
    }
    public ResponseEntity<?> disconnect(User user) {
        logger.info("Disconnecting user '{}'!", user);
        UserStatus userStatus = statusHashMap.get(user);
        NodeInfo nodeInfo = userStatus.getNodeInfo();
        int connectedDevices = userStatus.getConnectedDevices();
        connectedDevices -= 1;
        userStatus.setConnectedDevices(connectedDevices);
        if (connectedDevices == 0) {
            logger.info("Sending disconnect request for user '{}'!", user);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(user.getUsername(), user.getPassword());
            HttpEntity<Object> requestEntity = new HttpEntity<>(user, headers);
            statusHashMap.remove(user);
            try {
                return restTemplate.exchange(
                        "http://"+ nodeInfo.getName().toLowerCase() + ":8080/auth/disconnect", HttpMethod.POST, requestEntity, String.class);
            } catch (HttpClientErrorException e) {
                return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
            }
        }
        return ResponseEntity.ok("User '" + user.getUsername() + "' device's number '"  + connectedDevices + "' has been disconnected!");
    }
}
