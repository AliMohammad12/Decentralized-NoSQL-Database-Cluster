package atypon.app.service;

import atypon.app.model.UserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class RegistrationService {
    private static final Logger logger = LoggerFactory.getLogger(WriteRequestsService.class);
    private final RestTemplate registrationRestTemplate;
    @Autowired
    public RegistrationService(@Qualifier("registrationBean") RestTemplate registrationRestTemplate) {
        this.registrationRestTemplate = registrationRestTemplate;
    }
    public ResponseEntity<String> register(UserRequest userRequest) {
        logger.info("Received registration request for the user '{}!'", userRequest.getUser());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> requestEntity = new HttpEntity<>(userRequest, headers);
        try {
            return registrationRestTemplate.exchange("http://NODE/user/register",
                    HttpMethod.POST, requestEntity, String.class);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }
}
