package atypon.app.node.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseUpdateRequest extends ApiRequest {
    private String oldDatabaseName;
    private String newDatabaseName;
}
