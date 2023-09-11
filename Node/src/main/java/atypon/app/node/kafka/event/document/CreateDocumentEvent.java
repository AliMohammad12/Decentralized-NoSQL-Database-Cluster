package atypon.app.node.kafka.event.document;

import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.model.Node;
import atypon.app.node.request.document.DocumentRequest;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@RequiredArgsConstructor
public class CreateDocumentEvent extends WriteEvent {
    private DocumentRequest documentRequest;
    public CreateDocumentEvent(DocumentRequest documentRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        this.documentRequest = documentRequest;
        this.username = user.getUsername();
        this.broadcastingNodeName = Node.getName();
    }
}
