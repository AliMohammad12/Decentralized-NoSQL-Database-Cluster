package atypon.app.node.kafka.listener.document;

import atypon.app.node.kafka.event.document.UpdateDocumentEvent;
import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.kafka.listener.EventListener;
import atypon.app.node.request.document.DocumentUpdateRequest;
import atypon.app.node.service.services.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UpdateDocumentListener implements EventListener {
    private final DocumentService documentService;
    @Autowired
    public UpdateDocumentListener(DocumentService documentService) {
        this.documentService = documentService;
    }
    @Override
    @KafkaListener(topics = "updateDocumentTopic")
    public void onEvent(WriteEvent event) throws IOException {
        UpdateDocumentEvent updateDocumentEvent = (UpdateDocumentEvent) event;
        setAuth(event.getUsername());

        DocumentUpdateRequest request = updateDocumentEvent.getDocumentUpdateRequest();
        documentService.updateDocument(request);
    }
}
