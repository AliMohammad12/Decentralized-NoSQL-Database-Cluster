package atypon.app.node.controller;


import atypon.app.node.model.Node;
import atypon.app.node.model.User;
import atypon.app.node.request.UserRequest;
import atypon.app.node.response.ValidatorResponse;
import atypon.app.node.service.Impl.BroadcastServiceImpl;
import atypon.app.node.service.services.BroadcastService;
import atypon.app.node.service.services.UserService;
import atypon.app.node.service.services.ValidatorService;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final BroadcastService broadcastService;
    private final ValidatorService validatorService;
    @Autowired
    public UserController(UserService userService,
                          BroadcastService broadcastService,
                          ValidatorService validatorService) {
        this.userService = userService;
        this.broadcastService = broadcastService;
        this.validatorService = validatorService;
    }
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserRequest request) throws IOException {
        User user = request.getUser();
        ValidatorResponse validatorResponse = validatorService.isUsernameExists(user.getUsername());
        if (validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validatorResponse.getMessage());
        }
        userService.addUser(user.getUsername(), user.getPassword());
        broadcastService.UnprotectedBroadcast(request, "/user/register");
        return ResponseEntity.ok("The user " + user.getUsername() + " was registered successfully!");
    }

}
