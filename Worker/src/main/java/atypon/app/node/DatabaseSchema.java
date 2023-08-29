package atypon.app.node;

import java.util.ArrayList;
import java.util.List;

public class DatabaseSchema {
    private String name;
    private List<CollectionSchema> collections = new ArrayList<>();
    public DatabaseSchema(String name) {
        this.name = name;
    }
    public CollectionSchema getCollection(String collectionName) {
        for (CollectionSchema collection : collections) {
            if (collection.getName().equals(collectionName)) {
                return collection;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }
    public List<CollectionSchema> getCollections() {
        return collections;
    }
}