package atypon.app.node.kafka.listener;

import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.model.User;
import atypon.app.node.security.MyUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

public interface EventListener {
    void onEvent(WriteEvent event) throws IOException;
    default void setAuth(String username) {
        User user = new User();
        user.setUsername(username);
        UserDetails userDetails = new MyUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                null
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
