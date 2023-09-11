package atypon.app.node.kafka.listener.indexing;

import atypon.app.node.indexing.IndexObject;
import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.kafka.event.indexing.CreateIndexingEvent;
import atypon.app.node.kafka.listener.EventListener;
import atypon.app.node.service.services.IndexingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CreateIndexingListener implements EventListener {
    private final IndexingService indexingService;
    @Autowired
    public CreateIndexingListener(IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    @Override
    @KafkaListener(topics = "createIndexingTopic")
    public void onEvent(WriteEvent event) throws IOException {
        CreateIndexingEvent createIndexingEvent = (CreateIndexingEvent) event;
        setAuth(event.getUsername());

        IndexObject indexObject = createIndexingEvent.getIndexObject();
        indexingService.createIndexing(indexObject);
    }
}
