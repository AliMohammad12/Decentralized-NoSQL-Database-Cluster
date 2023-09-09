package atypon.app.node.indexing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Property<T> {
    private String name;
    private T value;
    public boolean isIntegerValue() {
        return value instanceof Integer;
    }
    public boolean isDoubleValue() {
        return value instanceof Double;
    }
    public boolean isStringValue() {
        return value instanceof String;
    }
    public boolean isBooleanValue() {
        return value instanceof Boolean;
    }
}

/*
    {
    "isBroadcast": false,
    "database": "Sad",
    "collection": "NewC",
        "property": {
            "name": "age"
            "value": 15
        }
    }

    - delete from collection NewC, all documents where age = 15
    - read from collection NewC, all documents where age = 15
 */