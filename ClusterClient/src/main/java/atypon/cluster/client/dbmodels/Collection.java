package atypon.cluster.client.dbmodels;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Collection {
    private String name;
    private Database database;
}
