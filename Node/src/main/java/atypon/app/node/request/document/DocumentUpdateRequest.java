package atypon.app.node.request.document;

import atypon.app.node.request.ApiRequest;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentUpdateRequest {
    private JsonNode updateRequest;
}
