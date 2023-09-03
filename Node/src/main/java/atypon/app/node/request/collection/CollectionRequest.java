package atypon.app.node.request.collection;

import atypon.app.node.model.Collection;
import atypon.app.node.request.ApiRequest;
import lombok.Data;
@Data
public class CollectionRequest extends ApiRequest {
    private Collection collection;
}
