package atypon.app.node.request.document;

import atypon.app.node.model.Document;
import atypon.app.node.request.ApiRequest;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class AddDocumentRequest extends ApiRequest {
    private JsonNode documentNode;
}
