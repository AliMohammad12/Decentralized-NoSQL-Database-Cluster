package atypon.app.config;

import atypon.app.model.User;
import atypon.app.model.UserStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class UserStatusConfig {
    @Bean(name = "userStatusBean")
    public HashMap<User, UserStatus> getUsersStatus() {
        return new HashMap<>();
    } // make it concurrent hashmap
}
