package atypon.app.node.controller;

import atypon.app.node.model.Node;
import atypon.app.node.model.NodeInfo;
import atypon.app.node.model.User;
import atypon.app.node.service.services.IndexingService;
import atypon.app.node.utility.FileOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("/auth")
public class AuthenticationController { // connection controller
    private final IndexingService indexingService;
    @Autowired
    public AuthenticationController(IndexingService indexingService) {
        this.indexingService = indexingService;
    }
    @PostMapping("/connect")
    public ResponseEntity<?> connect(@RequestBody User user) throws IOException {
        indexingService.indexingInitializer();
        return ResponseEntity.ok(new NodeInfo(Node.getPort(), Node.getNodeId(), Node.getName()));
    }
    @PostMapping("/disconnect")
    public ResponseEntity<?> disconnect()  {
        indexingService.IndexingFinalizer();
        return ResponseEntity.ok("Successfully disconnected from the node!");
    }
}