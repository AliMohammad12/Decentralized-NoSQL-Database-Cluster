package atypon.app.service;

import atypon.app.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class ConnectionService {
    private final RestTemplate connectionRestTemplate;
    @Autowired
    public ConnectionService(@Qualifier("connectionBean") RestTemplate connectionRestTemplate) {
        this.connectionRestTemplate = connectionRestTemplate;
    }
    public ResponseEntity<String> connect(User user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(user.getUsername(), user.getPassword());
        HttpEntity<Object> requestEntity = new HttpEntity<>(user, headers);

        try {
            return connectionRestTemplate.exchange(
                    "http://NODE/auth/connect", HttpMethod.POST, requestEntity, String.class);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }
}
