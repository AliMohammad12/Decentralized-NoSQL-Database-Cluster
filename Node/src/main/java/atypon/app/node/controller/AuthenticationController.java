package atypon.app.node.controller;

import atypon.app.node.service.Impl.AuthenticationServiceImpl;
import atypon.app.node.request.AuthenticationRequest;
import atypon.app.node.response.AuthenticationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class AuthenticationController {
    private AuthenticationServiceImpl authenticationServiceImpl;
    @Autowired
    public AuthenticationController(AuthenticationServiceImpl authenticationServiceImpl) {
        this.authenticationServiceImpl = authenticationServiceImpl;
    }
    @PostMapping(path = "/api/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) throws IOException {
        return ResponseEntity.ok(authenticationServiceImpl.authenticate(request));
    }
}
