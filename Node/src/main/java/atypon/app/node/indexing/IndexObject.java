package atypon.app.node.indexing;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndexObject {
    private String username;
    private String database;
    private String collection;
    private String property;
}
