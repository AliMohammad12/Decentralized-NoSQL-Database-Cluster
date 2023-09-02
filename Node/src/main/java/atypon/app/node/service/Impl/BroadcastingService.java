package atypon.app.node.service.Impl;

import atypon.app.node.model.Node;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class BroadcastingService {
    @Autowired
    private RestTemplate restTemplate;
    private final String node1 = "http://node1:8080/Node1";
    private final String node2 = "http://node2:8080/Node2";
    private final String node3 = "http://node3:8080/Node3";
    private final String node4 = "http://node4:8080/Node4";
    private List<String> urlList;
    @PostConstruct
    public void init() {
        urlList = new ArrayList<>();
        urlList.add(node1);
        urlList.add(node2);
        urlList.add(node3);
        urlList.add(node4);
    }
    public <T> void broadcast(T request, String endpoint) {

        HttpHeaders headers = new HttpHeaders();
        if (!endpoint.equals("/user/add")) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails user = (UserDetails) authentication.getPrincipal();
            headers.setBasicAuth(user.getUsername(), user.getPassword());
        }
        for (String url : urlList) {
            if (url.substring(18).equals(Node.getName())) continue;
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> requestEntity = new HttpEntity<>(request, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url + endpoint, HttpMethod.POST, requestEntity, String.class);
        }
    }
}
