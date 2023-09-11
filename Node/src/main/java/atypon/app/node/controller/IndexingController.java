package atypon.app.node.controller;

import atypon.app.node.indexing.IndexObject;
import atypon.app.node.kafka.KafkaService;
import atypon.app.node.kafka.TopicType;
import atypon.app.node.kafka.event.indexing.CreateIndexingEvent;
import atypon.app.node.kafka.event.indexing.DeleteIndexingEvent;
import atypon.app.node.response.ValidatorResponse;
import atypon.app.node.service.services.BroadcastService;
import atypon.app.node.service.services.IndexingService;
import atypon.app.node.service.services.ValidatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/indexing")
public class IndexingController {
    private final ValidatorService validatorService;
    private final IndexingService indexingService;
    private final KafkaService kafkaService;

    @Autowired
    public IndexingController(ValidatorService validatorService,
                              IndexingService indexingService,
                              KafkaService kafkaService) {
        this.validatorService = validatorService;
        this.indexingService = indexingService;
        this.kafkaService = kafkaService;
    }
    @PostMapping("/create")
    public ResponseEntity<?> createIndexing(@RequestBody IndexObject indexObject) {
        ValidatorResponse validatorResponse = validatorService.isIndexCreationAllowed(indexObject);
        if (!validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validatorResponse.getMessage());
        }
        kafkaService.broadCast(TopicType.Create_Indexing, new CreateIndexingEvent(indexObject));
        return ResponseEntity.ok(validatorResponse.getMessage() +
                " Index for property '" + indexObject.getProperty() + "' has been created successfully!");
    }
    @PostMapping("/delete")
    public ResponseEntity<?> deleteIndexing(@RequestBody IndexObject indexObject) {
        ValidatorResponse validatorResponse = validatorService.IsIndexDeletionAllowed(indexObject);
        if (!validatorResponse.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validatorResponse.getMessage());
        }
        kafkaService.broadCast(TopicType.Delete_Indexing, new DeleteIndexingEvent(indexObject));
        return ResponseEntity.ok(validatorResponse.getMessage() +
                " Index for property '" + indexObject.getProperty() + "' has been deleted successfully!");
    }
}
