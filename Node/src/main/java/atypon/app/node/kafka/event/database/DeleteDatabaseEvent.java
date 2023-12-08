package atypon.app.node.kafka.event.database;

import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.model.Node;
import atypon.app.node.request.database.DatabaseRequest;
import atypon.app.node.request.database.DatabaseUpdateRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@RequiredArgsConstructor
public class DeleteDatabaseEvent extends WriteEvent {
    private DatabaseRequest databaseRequest;
    public DeleteDatabaseEvent(DatabaseRequest databaseRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        this.databaseRequest = databaseRequest;
        this.username = user.getUsername();
    }
}
