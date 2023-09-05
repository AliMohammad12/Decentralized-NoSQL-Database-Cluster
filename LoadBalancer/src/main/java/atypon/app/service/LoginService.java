package atypon.app.service;

import atypon.app.request.UserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LoginService {
    private final RestTemplate loginRestTemplate;
    @Autowired
    public LoginService(@Qualifier("loginBean") RestTemplate loginRestTemplate) {
        this.loginRestTemplate = loginRestTemplate;
    }
    public ResponseEntity<String> login(UserRequest userRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> requestEntity = new HttpEntity<>(userRequest, headers);

        return loginRestTemplate.exchange(
                "http://NODE/login", HttpMethod.POST, requestEntity, String.class);
    }
}
