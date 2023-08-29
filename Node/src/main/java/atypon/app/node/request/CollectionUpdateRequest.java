package atypon.app.node.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CollectionUpdateRequest {
    private String oldCollectionName;
    private String newCollectionName;
    private String databaseName;
}
