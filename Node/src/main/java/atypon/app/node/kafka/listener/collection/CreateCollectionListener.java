package atypon.app.node.kafka.listener.collection;

import atypon.app.node.kafka.event.collection.CreateCollectionEvent;
import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.kafka.listener.EventListener;
import atypon.app.node.request.collection.CreateCollectionRequest;
import atypon.app.node.schema.CollectionSchema;
import atypon.app.node.service.services.CollectionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CreateCollectionListener implements EventListener {
    private final CollectionService collectionService;
    @Autowired
    public CreateCollectionListener(CollectionService collectionService) {
        this.collectionService = collectionService;
    }
    @Override
    @KafkaListener(topics = "createCollectionTopic")
    public void onEvent(WriteEvent event) throws JsonProcessingException {
        CreateCollectionEvent createCollectionEvent = (CreateCollectionEvent) event;
        setAuth(event.getUsername());

        CreateCollectionRequest request = createCollectionEvent.getCreateCollectionRequest();
        CollectionSchema collectionSchema = request.getCollectionSchema();
        collectionService.createCollection(collectionSchema);
    }
}
