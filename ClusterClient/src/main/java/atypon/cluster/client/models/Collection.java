package atypon.cluster.client.models;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Collection {
    private String name;
    private Database database;
}
