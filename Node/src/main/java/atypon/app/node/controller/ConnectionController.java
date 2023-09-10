package atypon.app.node.controller;

import atypon.app.node.model.Node;
import atypon.app.node.model.NodeInfo;
import atypon.app.node.model.User;
import atypon.app.node.service.services.IndexingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
public class ConnectionController { // connection controller
    private final IndexingService indexingService;
    @Autowired
    public ConnectionController(IndexingService indexingService) {
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