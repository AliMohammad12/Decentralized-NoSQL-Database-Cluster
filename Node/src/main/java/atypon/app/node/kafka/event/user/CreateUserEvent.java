package atypon.app.node.kafka.event.user;

import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.model.Node;
import atypon.app.node.request.UserRequest;
import atypon.app.node.request.document.DocumentRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@RequiredArgsConstructor
public class CreateUserEvent extends WriteEvent {
    private UserRequest userRequest;
    public CreateUserEvent(UserRequest userRequest) {
        this.userRequest = userRequest;
        this.broadcastingNodeName = Node.getName();
    }
}
