package atypon.app.node.request;

import atypon.app.node.model.Database;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class DeleteCollectionRequest extends ApiRequest{
    private String name;
    private Database database;
}
