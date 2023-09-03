package atypon.app.node.request.collection;

import atypon.app.node.request.ApiRequest;
import atypon.app.node.schema.CollectionSchema;
import lombok.Data;

@Data
public class CreateCollectionRequest extends ApiRequest {
    private CollectionSchema collectionSchema;
}
