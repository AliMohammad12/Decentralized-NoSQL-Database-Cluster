package atypon.app.node.kafka.listener.collection;

import atypon.app.node.kafka.event.collection.DeleteCollectionEvent;
import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.kafka.listener.EventListener;
import atypon.app.node.locking.DistributedLocker;
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
    private final DistributedLocker distributedLocker;
    private final String COLLECTION_PREFIX = "COLLECTION_LOCK:";
    @Autowired
    public DeleteCollectionListener(CollectionService collectionService,
                                    DistributedLocker distributedLocker) {
        this.collectionService = collectionService;
        this.distributedLocker = distributedLocker;
    }
    @Override
    @KafkaListener(topics = "deleteCollectionTopic")
    public void onEvent(WriteEvent event) throws IOException {
        DeleteCollectionEvent deleteCollectionEvent = (DeleteCollectionEvent) event;
        setAuth(event.getUsername());

        CollectionRequest request = deleteCollectionEvent.getCollectionRequest();
        Collection collection = request.getCollection();
        collectionService.deleteCollection(collection);

        // release
        distributedLocker.releaseWriteLock(COLLECTION_PREFIX + collection.getName());
    }
}
