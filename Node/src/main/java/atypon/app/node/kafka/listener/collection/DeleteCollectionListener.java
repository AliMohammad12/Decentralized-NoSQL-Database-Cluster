package atypon.app.node.kafka.listener.collection;

import atypon.app.node.kafka.event.collection.DeleteCollectionEvent;
import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.kafka.listener.EventListener;
import atypon.app.node.model.Collection;
import atypon.app.node.request.collection.CollectionRequest;
import atypon.app.node.service.services.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DeleteCollectionListener implements EventListener {
    private final CollectionService collectionService;
    @Autowired
    public DeleteCollectionListener(CollectionService collectionService) {
        this.collectionService = collectionService;
    }
    @Override
    @KafkaListener(topics = "deleteCollectionTopic")
    public void onEvent(WriteEvent event) throws IOException {
        DeleteCollectionEvent deleteCollectionEvent = (DeleteCollectionEvent) event;
        setAuth(event.getUsername());

        CollectionRequest request = deleteCollectionEvent.getCollectionRequest();
        Collection collection = request.getCollection();
        collectionService.deleteCollection(collection);
    }
}
