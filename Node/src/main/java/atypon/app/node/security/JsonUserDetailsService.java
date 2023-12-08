package atypon.app.node.security;

import atypon.app.node.model.Node;
import atypon.app.node.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.File;
import java.io.IOException;

public class JsonUserDetailsService implements UserDetailsService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            File jsonFile = new File("Storage/" + Node.getName() + "/Users.json");
            if (!jsonFile.exists()) {
                throw new UsernameNotFoundException("User data file not found");
            }

            User[] users = objectMapper.readValue(jsonFile, User[].class);

            for (User user : users) {
                if (user.getUsername().equals(username)) {
                    return new MyUserDetails(user);
                }
            }
            throw new UsernameNotFoundException("Could not find user");
        } catch (IOException e) {
            throw new RuntimeException("Error loading user data", e);
        }
    }
}
