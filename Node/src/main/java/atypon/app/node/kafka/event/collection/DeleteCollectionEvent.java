package atypon.app.node.kafka.event.collection;

import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.model.Node;
import atypon.app.node.request.collection.CollectionRequest;
import atypon.app.node.request.collection.CollectionUpdateRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@RequiredArgsConstructor
public class DeleteCollectionEvent extends WriteEvent {
    private CollectionRequest collectionRequest;
    public DeleteCollectionEvent(CollectionRequest collectionRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        this.collectionRequest = collectionRequest;
        this.username = user.getUsername();
        this.broadcastingNodeName = Node.getName();
    }
}
