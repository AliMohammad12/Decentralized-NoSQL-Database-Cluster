package atypon.app.service;

import atypon.app.model.User;
import atypon.app.request.UserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RegistrationService {
    private final RestTemplate registrationRestTemplate;
    @Autowired
    public RegistrationService(@Qualifier("registrationBean") RestTemplate registrationRestTemplate) {
        this.registrationRestTemplate = registrationRestTemplate;
    }
    public ResponseEntity<String> register(UserRequest userRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> requestEntity = new HttpEntity<>(userRequest, headers);
        ResponseEntity<String> responseEntity =  registrationRestTemplate.exchange(
                "http://NODE/user/register", HttpMethod.POST, requestEntity, String.class);

        System.out.println("HERRRRRRRRRRREEEEEEE = " + requestEntity.getBody());
        return responseEntity;
//        return registrationRestTemplate.exchange(
//            "http://NODE/user/register", HttpMethod.POST, requestEntity, String.class);
    }
}
