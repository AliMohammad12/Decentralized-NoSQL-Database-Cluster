package atypon.app.security;

import atypon.app.model.Node;
import atypon.app.model.NodeInfo;
import atypon.app.model.User;
import atypon.app.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class NodeAuthenticationProvider implements AuthenticationProvider {
    private final RestTemplate restTemplate;
    @Autowired
    public NodeAuthenticationProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(username, password);
        HttpEntity<Object> requestEntity = new HttpEntity<>(new User(username, password), headers);
        try {
            ResponseEntity<NodeInfo> responseEntity;
            responseEntity = restTemplate.exchange(
                    "http://load-balancer:9000/api/connect", HttpMethod.POST, requestEntity, NodeInfo.class);
            NodeInfo nodeInfo = responseEntity.getBody();
            Node.setPort(nodeInfo.getPort());
            Node.setNodeId(nodeInfo.getId());
            Node.setName(nodeInfo.getName());
            UserInfo.setUsername(username);
            UserInfo.setPassword(password);
            return new UsernamePasswordAuthenticationToken(username, password, null);
        } catch (HttpClientErrorException e) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}