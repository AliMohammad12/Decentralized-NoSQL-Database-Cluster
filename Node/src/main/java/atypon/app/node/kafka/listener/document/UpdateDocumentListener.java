package atypon.app.node.kafka.listener.document;

import atypon.app.node.kafka.event.document.UpdateDocumentEvent;
import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.kafka.listener.EventListener;
import atypon.app.node.locking.DistributedLocker;
import atypon.app.node.request.document.DocumentUpdateRequest;
import atypon.app.node.service.services.DocumentService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UpdateDocumentListener implements EventListener {
    private final DocumentService documentService;
    private final DistributedLocker distributedLocker;
    private final String DOCUMENT_PREFIX = "DOCUMENT_LOCK:";
    @Autowired
    public UpdateDocumentListener(DocumentService documentService,
                                  DistributedLocker distributedLocker) {
        this.documentService = documentService;
        this.distributedLocker = distributedLocker;
    }
    @Override
    @KafkaListener(topics = "updateDocumentTopic")
    public void onEvent(WriteEvent event) throws IOException {
        UpdateDocumentEvent updateDocumentEvent = (UpdateDocumentEvent) event;
        setAuth(event.getUsername());

        DocumentUpdateRequest request = updateDocumentEvent.getDocumentUpdateRequest();
        documentService.updateDocument(request);

        JsonNode updateRequest = request.getUpdateRequest();
        String id = updateRequest.get("info").get("id").asText();
        System.out.println("Document update topic: " + id);
        distributedLocker.releaseWriteLock(DOCUMENT_PREFIX + id);

    }
}
