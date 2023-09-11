package atypon.app.node.request.database;

import atypon.app.node.request.ApiRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseUpdateRequest  {
    private String oldDatabaseName;
    private String newDatabaseName;
}
