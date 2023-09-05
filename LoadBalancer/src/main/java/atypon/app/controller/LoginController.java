package atypon.app.controller;

import atypon.app.request.UserRequest;
import atypon.app.service.LoginService;
import atypon.app.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login")
public class LoginController {
    private final LoginService loginService;
    @Autowired
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }
    @PostMapping
    public ResponseEntity<?> login(@RequestBody UserRequest userRequest) {
        return loginService.login(userRequest);
    }
}
