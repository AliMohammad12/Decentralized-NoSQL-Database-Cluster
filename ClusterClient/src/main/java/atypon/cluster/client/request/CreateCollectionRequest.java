package atypon.cluster.client.request;

import atypon.cluster.client.schema.CollectionSchema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCollectionRequest extends ApiRequest {
    private CollectionSchema collectionSchema;
}
