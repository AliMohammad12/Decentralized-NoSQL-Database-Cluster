package atypon.cluster.client.request;
import atypon.cluster.client.dbmodels.Database;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseRequest {
    private Database database;
}
