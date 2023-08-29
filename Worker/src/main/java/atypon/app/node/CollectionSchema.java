package atypon.app.node;

public class CollectionSchema {
    private String name;
    private String jsonSchema;

    public CollectionSchema(String name, String jsonSchema) {
        this.name = name;
        this.jsonSchema = jsonSchema;
    }
    public String getName() {
        return name;
    }
    public String getJsonSchema() {
        return jsonSchema;
    }
}