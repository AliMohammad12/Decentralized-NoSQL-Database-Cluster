package atypon.app.node.model;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Document {
    private String id;
    private String dbName;
    private String collectionName;
}
