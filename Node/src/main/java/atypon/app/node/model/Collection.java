package atypon.app.node.model;
import lombok.*;

import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Collection {
    private String name;
    private Database database;
}
