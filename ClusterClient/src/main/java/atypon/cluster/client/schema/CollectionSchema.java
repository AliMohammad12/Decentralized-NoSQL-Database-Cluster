package atypon.cluster.client.schema;

import atypon.cluster.client.dbmodels.Collection;
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
