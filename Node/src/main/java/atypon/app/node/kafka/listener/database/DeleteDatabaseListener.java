package atypon.app.node.kafka.listener.database;

import atypon.app.node.kafka.event.database.DeleteDatabaseEvent;
import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.kafka.listener.EventListener;
import atypon.app.node.model.Database;
import atypon.app.node.request.database.DatabaseRequest;
import atypon.app.node.service.services.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DeleteDatabaseListener implements EventListener {
    private final DatabaseService databaseService;
    @Autowired
    public DeleteDatabaseListener(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }
    @Override
    @KafkaListener(topics = "deleteDatabaseTopic")
    public void onEvent(WriteEvent event) throws IOException {
        DeleteDatabaseEvent deleteDatabaseEvent = (DeleteDatabaseEvent) event;
        setAuth(event.getUsername());

        DatabaseRequest request = deleteDatabaseEvent.getDatabaseRequest();
        Database database = request.getDatabase();
        databaseService.deleteDatabase(database);
    }
}
