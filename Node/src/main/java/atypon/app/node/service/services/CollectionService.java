package atypon.app.node.service.services;

import atypon.app.node.model.Collection;
import atypon.app.node.schema.CollectionSchema;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.List;

public interface CollectionService {
    void createCollection(CollectionSchema collectionSchema) throws JsonProcessingException;
    void updateCollectionName(String databaseName, String oldCollectionName, String newCollectionName);
    void deleteCollection(Collection collection) throws IOException;
    JsonNode readCollection(Collection collection);
}

