package atypon.app.node.kafka.event.document;

import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.model.Node;
import atypon.app.node.request.document.DocumentRequestByProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@RequiredArgsConstructor
public class DeleteDocumentsByPropertyEvent extends WriteEvent {
    private DocumentRequestByProperty documentRequestByProperty;
    public DeleteDocumentsByPropertyEvent(DocumentRequestByProperty documentRequestByProperty) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        this.documentRequestByProperty = documentRequestByProperty;
        this.username = user.getUsername();
        this.broadcastingNodeName = Node.getName();
    }
}
