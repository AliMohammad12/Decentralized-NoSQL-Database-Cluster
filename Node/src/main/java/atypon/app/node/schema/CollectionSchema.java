package atypon.app.node.schema;

import atypon.app.node.model.Collection;
import lombok.*;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CollectionSchema {
    private Map<String, Object> fields;
    private Collection collection;
}
