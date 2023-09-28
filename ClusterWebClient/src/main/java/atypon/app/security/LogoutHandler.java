package atypon.app.security;

import atypon.app.model.User;
import atypon.app.model.UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Component
public class LogoutHandler implements LogoutSuccessHandler {
    @Autowired
    private RestTemplate restTemplate;
    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException {
        String username = authentication.getName();
        String password = UserInfo.getPassword();

        String url = "http://load-balancer:9000/api/disconnect";
        User user = new User(username, password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(username, password);
        HttpEntity<Object> requestEntity = new HttpEntity<>(user, headers);
        restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        response.sendRedirect("/login");
    }
}
