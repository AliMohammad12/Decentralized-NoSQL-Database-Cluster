package atypon.app.node.kafka.event.document;

import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.model.Node;
import atypon.app.node.request.document.DocumentRequestByProperty;
import atypon.app.node.request.document.DocumentUpdateRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@RequiredArgsConstructor
public class UpdateDocumentEvent extends WriteEvent {
    private DocumentUpdateRequest documentUpdateRequest;
    public UpdateDocumentEvent(DocumentUpdateRequest documentUpdateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        this.documentUpdateRequest = documentUpdateRequest;
        this.username = user.getUsername();
    }
}
