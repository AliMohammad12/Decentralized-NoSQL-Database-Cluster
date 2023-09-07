package atypon.app.client.schema;

import java.util.Map;

public class CollectionSchema {
    private Map<String, Object> fields;
    private String collectionName;
    private String databaseName;

    public CollectionSchema(Map<String, Object> fields, String collectionName, String databaseName) {
        this.fields = fields;
        this.collectionName = collectionName;
        this.databaseName = databaseName;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
}
