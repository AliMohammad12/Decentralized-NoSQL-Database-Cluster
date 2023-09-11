package atypon.cluster.client.request;

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