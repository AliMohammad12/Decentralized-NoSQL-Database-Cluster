package atypon.app.node.controller;

import atypon.app.node.model.Node;
import atypon.app.node.model.NodeInfo;
import atypon.app.node.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    @PostMapping("/connect")
    public ResponseEntity<?> authenticate(@RequestBody User user) {
        System.out.println("Inside node " + Node.getName() + " Received = " + user.getUsername() + " " + user.getPassword());
        System.out.println(Node.getNodeId() + " " + Node.getName() + " " + Node.getPort());
        return ResponseEntity.ok(new NodeInfo(Node.getPort(), Node.getNodeId(), Node.getName()));
    }
}