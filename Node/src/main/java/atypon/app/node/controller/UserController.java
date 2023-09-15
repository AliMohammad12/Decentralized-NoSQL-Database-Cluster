package atypon.app.node.controller;


import atypon.app.node.kafka.KafkaService;
import atypon.app.node.kafka.TopicType;
import atypon.app.node.kafka.event.user.CreateUserEvent;
import atypon.app.node.locking.LockManager;
import atypon.app.node.model.User;
import atypon.app.node.request.document.DocumentRequest;
import atypon.app.node.request.user.UserRequest;
import atypon.app.node.response.ValidatorResponse;
import atypon.app.node.service.services.UserService;
import atypon.app.node.service.services.ValidatorService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.channels.FileLock;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final KafkaService kafkaService;
    private final ValidatorService validatorService;

    private final ValueOperations<String, Object> valueOps;

    @Autowired
    public UserController(UserService userService,
                          KafkaService kafkaService,
                          ValidatorService validatorService,
                          final RedisTemplate<String, Object> redisTemplate) {
        this.userService = userService;
        this.kafkaService = kafkaService;
        this.validatorService = validatorService;
        valueOps = redisTemplate.opsForValue();
    }
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserRequest request) throws IOException {
        User user = request.getUser();
        ValidatorResponse validatorResponse = validatorService.isUsernameExists(user.getUsername());
        if (validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validatorResponse.getMessage());
        }
        kafkaService.broadCast(TopicType.Create_User, new CreateUserEvent(request));
        return ResponseEntity.ok("The user " + user.getUsername() + " was registered successfully!");
    }


    @PostMapping("/in")
    public ResponseEntity<String> redisTestIn(@RequestBody DocumentRequest request) throws IOException {
        JsonNode jsonNode = request.getDocumentNode();
        valueOps.set(jsonNode.get("id").asText(), jsonNode.get("data"), 30, TimeUnit.SECONDS);
        return ResponseEntity.ok("Inserted");
    }
    @PostMapping("/out")
    public ResponseEntity<?> redisTestOut(@RequestBody String id)  {
        return ResponseEntity.ok(valueOps.get(id));
    }

}
