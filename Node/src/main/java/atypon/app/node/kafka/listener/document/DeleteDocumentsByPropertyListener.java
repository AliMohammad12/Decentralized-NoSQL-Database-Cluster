package atypon.app.node.kafka.listener.document;

import atypon.app.node.kafka.event.document.DeleteDocumentsByPropertyEvent;
import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.kafka.listener.EventListener;
import atypon.app.node.request.document.DocumentRequestByProperty;
import atypon.app.node.service.services.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DeleteDocumentsByPropertyListener implements EventListener {
    private final DocumentService documentService;
    @Autowired
    public DeleteDocumentsByPropertyListener(DocumentService documentService) {
        this.documentService = documentService;
    }
    @Override
    @KafkaListener(topics = "deleteDocumentsByPropertyTopic")
    public void onEvent(WriteEvent event) throws IOException {
        DeleteDocumentsByPropertyEvent deleteDocumentsByPropertyEvent = (DeleteDocumentsByPropertyEvent) event;
        setAuth(event.getUsername());

        DocumentRequestByProperty request = deleteDocumentsByPropertyEvent.getDocumentRequestByProperty();
        documentService.deleteDocumentByProperty(request);
    }
}
