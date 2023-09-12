package atypon.cluster.client.shutdown;

import atypon.cluster.client.models.NodeInfo;
import atypon.cluster.client.models.User;
import atypon.cluster.client.models.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ShutdownListener implements ApplicationListener<ContextClosedEvent> {
    private final RestTemplate restTemplate;
    @Autowired
    public ShutdownListener(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        sendShutdownRequest();
    }
    private void sendShutdownRequest() {
        String url = "http://localhost:9000/api/disconnect";
        String username = UserInfo.getUsername();
        String password = UserInfo.getPassword();
        User user = new User(username, password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(username, password);
        HttpEntity<Object> requestEntity = new HttpEntity<>(user, headers);
        restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
    }
}