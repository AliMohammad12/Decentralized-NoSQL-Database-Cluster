package atypon.app.node.kafka.event.user;

import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.model.Node;
import atypon.app.node.request.user.UserRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CreateUserEvent extends WriteEvent {
    private UserRequest userRequest;
    public CreateUserEvent(UserRequest userRequest) {
        this.userRequest = userRequest;
    }
}
