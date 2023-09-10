package atypon.app.node.kafka.listener;

import atypon.app.node.kafka.event.CreateDocumentEvent;
import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.model.Node;
import atypon.app.node.model.User;
import atypon.app.node.request.document.DocumentRequest;
import atypon.app.node.security.MyUserDetails;
import atypon.app.node.service.services.DocumentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
        if (createDocumentEvent.getBroadcastingNodeName().equals(Node.getName())) {
            System.out.println("Same node returning!");
            return;
        }
        User user = new User();
        user.setUsername(event.getUsername());
        UserDetails userDetails = new MyUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
          userDetails,
          null,
          null
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        DocumentRequest request = createDocumentEvent.getDocumentRequest();
        JsonNode document = request.getDocumentNode();
        String collectionName = document.get("CollectionName").asText();
        String databaseName = document.get("DatabaseName").asText();
        JsonNode documentData = document.get("data");
        documentService.addDocument(databaseName, collectionName, documentData);
    }
}
