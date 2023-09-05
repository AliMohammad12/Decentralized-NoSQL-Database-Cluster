package atypon.cluster.controller;

import atypon.cluster.request.UserRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrationController {
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam("username") String username,
                                      @RequestParam("password") String password) {

        return ResponseEntity.ok("The cluster has started successfully !");
    }
    @GetMapping("/register")
    public String register() {
        return "register";
    }

}
