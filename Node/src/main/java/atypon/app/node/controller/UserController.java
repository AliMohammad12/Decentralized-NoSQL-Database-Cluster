package atypon.app.node.controller;


import atypon.app.node.kafka.KafkaService;
import atypon.app.node.kafka.TopicType;
import atypon.app.node.kafka.event.user.CreateUserEvent;
import atypon.app.node.model.User;
import atypon.app.node.request.user.UserRequest;
import atypon.app.node.response.ValidatorResponse;
import atypon.app.node.service.services.UserService;
import atypon.app.node.service.services.ValidatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final KafkaService kafkaService;
    private final ValidatorService validatorService;
    @Autowired
    public UserController(UserService userService,
                          KafkaService kafkaService,
                          ValidatorService validatorService) {
        this.userService = userService;
        this.kafkaService = kafkaService;
        this.validatorService = validatorService;
    }
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserRequest request) throws IOException {
        User user = request.getUser();
        ValidatorResponse validatorResponse = validatorService.isUsernameExists(user.getUsername());
        if (validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validatorResponse.getMessage());
        }
        kafkaService.broadCast(TopicType.Create_User, new CreateUserEvent(request));
        return ResponseEntity.ok("The user " + user.getUsername() + " was registered successfully!");
    }

}
