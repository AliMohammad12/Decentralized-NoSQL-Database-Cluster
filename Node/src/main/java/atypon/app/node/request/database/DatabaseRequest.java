package atypon.app.node.request.database;

import atypon.app.node.model.Database;
import atypon.app.node.request.ApiRequest;
import lombok.Data;

@Data
public class DatabaseRequest extends ApiRequest {
    private Database database;
}
