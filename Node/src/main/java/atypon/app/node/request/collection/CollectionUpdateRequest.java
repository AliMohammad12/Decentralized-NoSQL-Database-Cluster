package atypon.app.node.request.collection;

import atypon.app.node.request.ApiRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CollectionUpdateRequest extends ApiRequest {
    private String oldCollectionName;
    private String newCollectionName;
    private String databaseName;
}
