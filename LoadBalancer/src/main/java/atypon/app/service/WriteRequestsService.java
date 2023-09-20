package atypon.app.service;

import atypon.app.model.User;
import atypon.app.model.UserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WriteRequestsService {
    private final RestTemplate restTemplate;
    @Autowired
    public WriteRequestsService(@Qualifier("writeRequestsBean") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    public ResponseEntity<?> sendWriteRequest(Object request, String endpoint, User user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(user.getUsername(), user.getPassword());
        HttpEntity<Object> requestEntity = new HttpEntity<>(request, headers);
        try {
            ResponseEntity<?> response = restTemplate.exchange(
                    "http://NODE/"+endpoint, HttpMethod.POST, requestEntity, String.class);
            return response;
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }
}
