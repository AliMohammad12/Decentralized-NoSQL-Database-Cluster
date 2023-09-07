package atypon.cluster.client.request;
import atypon.cluster.client.dbmodels.Database;
import lombok.Data;
@Data
public class DatabaseRequest extends ApiRequest {
    private Database database;
}
