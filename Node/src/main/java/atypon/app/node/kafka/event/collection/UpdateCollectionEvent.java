package atypon.app.node.kafka.event.collection;

import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.model.Node;
import atypon.app.node.request.collection.CollectionUpdateRequest;
import atypon.app.node.request.collection.CreateCollectionRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@RequiredArgsConstructor
public class UpdateCollectionEvent extends WriteEvent {
    private CollectionUpdateRequest collectionUpdateRequest;
    public UpdateCollectionEvent(CollectionUpdateRequest collectionUpdateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        this.collectionUpdateRequest = collectionUpdateRequest;
        this.username = user.getUsername();
    }
}
