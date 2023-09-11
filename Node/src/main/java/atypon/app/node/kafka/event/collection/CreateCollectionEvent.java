package atypon.app.node.kafka.event.collection;

import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.model.Node;
import atypon.app.node.request.collection.CreateCollectionRequest;
import atypon.app.node.request.database.DatabaseRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@RequiredArgsConstructor
public class CreateCollectionEvent extends WriteEvent {
    private CreateCollectionRequest createCollectionRequest;
    public CreateCollectionEvent(CreateCollectionRequest createCollectionRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        this.createCollectionRequest = createCollectionRequest;
        this.username = user.getUsername();
        this.broadcastingNodeName = Node.getName();
    }
}
