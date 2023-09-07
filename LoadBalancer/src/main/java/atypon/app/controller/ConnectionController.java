package atypon.app.controller;

import atypon.app.model.User;
import atypon.app.service.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ConnectionController {
    private final ConnectionService connectionService;
    @Autowired
    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }
    @PostMapping("/connect")
    public ResponseEntity<?> connect(@RequestBody User user) {
        return connectionService.connect(user);
    }
}
