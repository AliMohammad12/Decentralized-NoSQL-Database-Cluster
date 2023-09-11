package atypon.app.node.kafka.listener.database;

import atypon.app.node.kafka.event.database.CreateDatabaseEvent;
import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.kafka.listener.EventListener;
import atypon.app.node.model.Database;
import atypon.app.node.request.database.DatabaseRequest;
import atypon.app.node.service.services.DatabaseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CreateDatabaseListener implements EventListener {
    private final DatabaseService databaseService;
    @Autowired
    public CreateDatabaseListener(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }
    @Override
    @KafkaListener(topics = "createDatabaseTopic")
    public void onEvent(WriteEvent event) throws JsonProcessingException {
        CreateDatabaseEvent createDatabaseEvent = (CreateDatabaseEvent) event;
        setAuth(event.getUsername());

        DatabaseRequest request = createDatabaseEvent.getDatabaseRequest();
        Database database = request.getDatabase();
        databaseService.createDatabase(database);
    }
}
