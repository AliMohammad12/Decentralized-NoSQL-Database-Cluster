package atypon.app.node.controller;


import atypon.app.node.model.Node;
import atypon.app.node.model.User;
import atypon.app.node.request.UserRequest;
import atypon.app.node.service.Impl.BroadcastServiceImpl;
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
    private final BroadcastServiceImpl broadcastServiceImpl;
    @Autowired
    public UserController(UserService userService, BroadcastServiceImpl broadcastServiceImpl) {
        this.userService = userService;
        this.broadcastServiceImpl = broadcastServiceImpl;
    }
    @PostMapping("/add")
    public String addUser(@RequestBody UserRequest request) throws IOException {
        if (!request.isBroadcast()) {
            System.out.println(Node.getName() + " is executing now!");
        }
        // check if user already exists..
        User user = request.getUser();
        userService.addUser(user.getUsername(), user.getPassword());
        broadcastServiceImpl.UnprotectedBroadcast(request, "/user/add");
        return "User registered: " + user.getUsername();
    }
}
