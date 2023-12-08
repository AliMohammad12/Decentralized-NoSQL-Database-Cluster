package atypon.app.node.kafka.event.database;

import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.model.Node;
import atypon.app.node.request.database.DatabaseUpdateRequest;
import atypon.app.node.request.document.DocumentRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@RequiredArgsConstructor
public class UpdateDatabaseEvent extends WriteEvent {
    private DatabaseUpdateRequest databaseUpdateRequest;
    public UpdateDatabaseEvent(DatabaseUpdateRequest databaseUpdateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        this.databaseUpdateRequest = databaseUpdateRequest;
        this.username = user.getUsername();
    }
}
