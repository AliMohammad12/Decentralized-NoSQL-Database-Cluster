package atypon.app.node.kafka.listener.user;

import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.kafka.event.user.CreateUserEvent;
import atypon.app.node.kafka.listener.EventListener;
import atypon.app.node.model.User;
import atypon.app.node.request.user.UserRequest;
import atypon.app.node.service.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CreateUserListener implements EventListener {
    private final UserService userService;
    @Autowired
    public CreateUserListener(UserService userService) {
        this.userService = userService;
    }

    @Override
    @KafkaListener(topics = "createUserTopic")
    public void onEvent(WriteEvent event) throws IOException {
        CreateUserEvent createDocumentEvent = (CreateUserEvent) event;

        UserRequest userRequest = createDocumentEvent.getUserRequest();
        User user = userRequest.getUser();
        userService.addUser(user.getUsername(), user.getPassword());
    }
}
