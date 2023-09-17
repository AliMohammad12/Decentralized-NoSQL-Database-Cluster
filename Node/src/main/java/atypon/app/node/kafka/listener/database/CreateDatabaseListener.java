package atypon.app.node.kafka.listener.database;

import atypon.app.node.kafka.event.database.CreateDatabaseEvent;
import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.kafka.listener.EventListener;
import atypon.app.node.kafka.listener.locking.ShareLockListener;
import atypon.app.node.locking.DistributedLocker;
import atypon.app.node.model.Database;
import atypon.app.node.request.database.DatabaseRequest;
import atypon.app.node.service.services.DatabaseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CreateDatabaseListener implements EventListener {
    private final DatabaseService databaseService;
    private final DistributedLocker distributedLocker;
    @Autowired
    public CreateDatabaseListener(DatabaseService databaseService,
                                  DistributedLocker distributedLocker) {
        this.databaseService = databaseService;
        this.distributedLocker = distributedLocker;
    }

    @Override
    @KafkaListener(topics = "createDatabaseTopic")
    public void onEvent(WriteEvent event) throws JsonProcessingException {
        CreateDatabaseEvent createDatabaseEvent = (CreateDatabaseEvent) event;
        setAuth(event.getUsername());

        DatabaseRequest request = createDatabaseEvent.getDatabaseRequest();
        Database database = request.getDatabase();
        databaseService.createDatabase(database);

        // release
        distributedLocker.releaseLock("Database:"+database.getName());
    }
}
