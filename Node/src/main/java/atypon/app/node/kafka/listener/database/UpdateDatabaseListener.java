package atypon.app.node.kafka.listener.database;

import atypon.app.node.kafka.event.database.UpdateDatabaseEvent;
import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.kafka.listener.EventListener;
import atypon.app.node.locking.DistributedLocker;
import atypon.app.node.request.database.DatabaseUpdateRequest;
import atypon.app.node.service.services.DatabaseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
public class UpdateDatabaseListener implements EventListener {
    private final DatabaseService databaseService;
    private final DistributedLocker distributedLocker;
    private final String DB_PREFIX = "DATABASE_LOCK:";
    @Autowired
    public UpdateDatabaseListener(DatabaseService databaseService,
                                  DistributedLocker distributedLocker) {
        this.databaseService = databaseService;
        this.distributedLocker = distributedLocker;
    }
    @Override
    @KafkaListener(topics = "updateDatabaseTopic")
    public void onEvent(WriteEvent event) throws JsonProcessingException {
        UpdateDatabaseEvent updateDatabaseEvent = (UpdateDatabaseEvent) event;
        setAuth(event.getUsername());

        DatabaseUpdateRequest request = updateDatabaseEvent.getDatabaseUpdateRequest();
        String oldDbName = request.getOldDatabaseName();
        String newDbName = request.getNewDatabaseName();
        databaseService.updateDatabaseName(oldDbName, newDbName);

        // release
        distributedLocker.releaseWriteLock(DB_PREFIX + oldDbName);
    }
}
