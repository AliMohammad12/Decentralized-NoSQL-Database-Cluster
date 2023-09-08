package atypon.app.node.model;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Document {
    private String id;
    private String dbName;
    private String collectionName;
}
