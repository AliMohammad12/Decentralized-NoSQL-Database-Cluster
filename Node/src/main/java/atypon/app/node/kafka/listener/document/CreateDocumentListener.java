package atypon.app.node.kafka.listener.document;

import atypon.app.node.kafka.event.document.CreateDocumentEvent;
import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.kafka.listener.EventListener;
import atypon.app.node.request.document.DocumentRequest;
import atypon.app.node.service.services.DocumentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CreateDocumentListener implements EventListener {
    private final DocumentService documentService;
    @Autowired
    public CreateDocumentListener(DocumentService documentService) {
        this.documentService = documentService;
    }
    @Override
    @KafkaListener(topics = "createDocumentTopic")
    public void onEvent(WriteEvent event) throws JsonProcessingException {
        CreateDocumentEvent createDocumentEvent = (CreateDocumentEvent) event;
        setAuth(event.getUsername());

        DocumentRequest request = createDocumentEvent.getDocumentRequest();
        documentService.addDocument(request);
    }
}
