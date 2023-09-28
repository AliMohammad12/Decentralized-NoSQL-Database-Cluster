package atypon.app.node.controller;

import atypon.app.node.model.Node;
import atypon.app.node.model.NodeInfo;
import atypon.app.node.model.User;
import atypon.app.node.service.services.CollectionService;
import atypon.app.node.service.services.IndexingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.SynchronousQueue;

@RestController
@RequestMapping("/auth")
public class ConnectionController {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionController.class);
    private final IndexingService indexingService;
    @Autowired
    public ConnectionController(IndexingService indexingService) {
        this.indexingService = indexingService;
    }
    @PostMapping("/connect")
    public ResponseEntity<?> connect(@RequestBody User user) throws IOException {
        logger.info("Connecting user '{}'", user.getUsername());
        indexingService.indexingInitializer();
        return ResponseEntity.ok(new NodeInfo(Node.getPort(), Node.getNodeId(), Node.getName()));
    }
    @PostMapping("/disconnect")
    public ResponseEntity<?> disconnect(@RequestBody User user) {
        logger.info("Disconnecting user '{}'", user.getUsername());
        indexingService.IndexingFinalizer();
        return ResponseEntity.ok("Successfully disconnected from the node!");
    }
}


