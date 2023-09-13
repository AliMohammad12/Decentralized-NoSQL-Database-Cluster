package atypon.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionData {
    private String databaseName;
    private String collectionName;
    private List<FieldInfo> fieldInfoList;
}
