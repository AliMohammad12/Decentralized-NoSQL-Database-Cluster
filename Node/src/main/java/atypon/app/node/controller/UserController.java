package atypon.app.node.controller;


import atypon.app.node.model.User;
import atypon.app.node.request.NewUserRequest;
import atypon.app.node.service.Impl.BroadcastingService;
import atypon.app.node.service.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final BroadcastingService broadcastingService;
    @Autowired
    public UserController(UserService userService, BroadcastingService broadcastingService) {
        this.userService = userService;
        this.broadcastingService = broadcastingService;
    }
    @PostMapping("/add")
    public String addUser(@RequestBody NewUserRequest request) throws IOException {
        // check if user already exists..
        User user = request.getUser();
        userService.addUser(user.getUsername(), user.getPassword());
        if (!request.isBroadcast()) {
            request.setBroadcast(true);
            broadcastingService.broadcast(request, "/user/add");
        }
        return "User registered: " + user.getUsername();
    }
}
