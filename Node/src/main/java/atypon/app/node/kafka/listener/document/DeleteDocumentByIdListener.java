package atypon.app.node.kafka.listener.document;

import atypon.app.node.kafka.event.document.DeleteDocumentByIdEvent;
import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.kafka.listener.EventListener;
import atypon.app.node.request.document.DocumentRequest;
import atypon.app.node.service.services.DocumentService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DeleteDocumentByIdListener implements EventListener {
    private final DocumentService documentService;
    @Autowired
    public DeleteDocumentByIdListener(DocumentService documentService) {
        this.documentService = documentService;
    }
    @Override
    @KafkaListener(topics = "deleteDocumentsByIdTopic")
    public void onEvent(WriteEvent event) throws IOException {
        DeleteDocumentByIdEvent deleteDocumentByIdEvent = (DeleteDocumentByIdEvent) event;
        setAuth(event.getUsername());

        DocumentRequest request = deleteDocumentByIdEvent.getDocumentRequest();
        JsonNode document = request.getDocumentNode();
        String collection = document.get("CollectionName").asText();
        String database = document.get("DatabaseName").asText();
        JsonNode documentData = document.get("data");
        documentService.deleteDocumentById(database, collection, documentData);
    }
}
