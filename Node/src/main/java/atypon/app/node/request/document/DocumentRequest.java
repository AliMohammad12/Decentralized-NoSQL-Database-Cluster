package atypon.app.node.request.document;

import atypon.app.node.request.ApiRequest;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentRequest  {
    private JsonNode documentNode;
}
