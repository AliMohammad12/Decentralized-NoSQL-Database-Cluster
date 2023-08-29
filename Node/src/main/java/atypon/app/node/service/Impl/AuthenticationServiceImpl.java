package atypon.app.node.service.Impl;

import atypon.app.node.model.Node;
import atypon.app.node.model.User;
import atypon.app.node.request.AuthenticationRequest;
import atypon.app.node.response.AuthenticationResponse;
import atypon.app.node.service.services.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    public AuthenticationResponse authenticate(AuthenticationRequest request) throws IOException {
        String username = request.getUsername();
        String password = request.getPassword();
        File jsonFile = new File("Storage/"+ Node.getNodeName()+"Users/Users.json");
        ObjectMapper objectMapper = new ObjectMapper();

        User[] users = objectMapper.readValue(jsonFile, User[].class);
        boolean isAuthenticated = Arrays.stream(users)
                .anyMatch(user -> user.getUsername().equals(username) && user.getPassword().equals(password));

        return AuthenticationResponse.builder().authenticated(isAuthenticated).build();
    }
}
