package atypon.app.node.request.document;

import atypon.app.node.indexing.Property;
import atypon.app.node.request.ApiRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentRequestByProperty extends ApiRequest {
    private String database;
    private String collection;
    private Property property;
}
