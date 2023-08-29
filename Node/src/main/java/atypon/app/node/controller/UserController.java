package atypon.app.node.controller;


import atypon.app.node.model.User;
import atypon.app.node.service.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class UserController {
    private final UserService userService;
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @RequestMapping("/add-user")
    public String addUser(@RequestBody User user) throws IOException {
        // check if user already exists..
        userService.addUser(user.getUsername(), user.getPassword());
        return "User registered: " + user.getUsername();
    }
}
