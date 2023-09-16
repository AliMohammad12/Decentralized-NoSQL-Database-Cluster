package atypon.app.node.kafka.event.indexing;

import atypon.app.node.indexing.IndexObject;
import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.model.Node;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@RequiredArgsConstructor
public class CreateIndexingEvent extends WriteEvent {
    private IndexObject indexObject;
    public CreateIndexingEvent(IndexObject indexObject) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        this.indexObject = indexObject;
        this.username = user.getUsername();
        this.broadcastingNodeName = Node.getName();
    }
}
