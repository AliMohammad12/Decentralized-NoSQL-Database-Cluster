package atypon.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldInfo {
    private String fieldName;
    private String fieldType;
    private boolean isIndexed;
}