package atypon.app.node.model;
import lombok.*;

import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Collection {
    private String name;
    private Database database;
}
