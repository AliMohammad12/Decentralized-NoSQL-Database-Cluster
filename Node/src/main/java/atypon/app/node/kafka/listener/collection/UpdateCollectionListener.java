package atypon.app.node.kafka.listener.collection;

import atypon.app.node.kafka.event.collection.UpdateCollectionEvent;
import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.kafka.listener.EventListener;
import atypon.app.node.locking.DistributedLocker;
import atypon.app.node.request.collection.CollectionUpdateRequest;
import atypon.app.node.service.services.CollectionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UpdateCollectionListener implements EventListener {
    private final CollectionService collectionService;
    private final DistributedLocker distributedLocker;
    private final String COLLECTION_PREFIX = "COLLECTION_LOCK:";
    @Autowired
    public UpdateCollectionListener(CollectionService collectionService,
                                    DistributedLocker distributedLocker) {
        this.collectionService = collectionService;
        this.distributedLocker = distributedLocker;
    }
    @Override
    @KafkaListener(topics = "updateCollectionTopic")
    public void onEvent(WriteEvent event) throws JsonProcessingException {
        UpdateCollectionEvent updateCollectionEvent = (UpdateCollectionEvent) event;
        setAuth(event.getUsername());

        CollectionUpdateRequest request = updateCollectionEvent.getCollectionUpdateRequest();
        String oldCollectionName = request.getOldCollectionName();
        String newCollectionName = request.getNewCollectionName();
        String databaseName = request.getDatabaseName();
        collectionService.updateCollectionName(databaseName, oldCollectionName, newCollectionName);

        // release
        distributedLocker.releaseWriteLock(COLLECTION_PREFIX + oldCollectionName);
    }
}
