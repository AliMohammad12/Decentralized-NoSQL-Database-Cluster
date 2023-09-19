package atypon.app.node.service.services;

import atypon.app.node.indexing.IndexObject;
import atypon.app.node.indexing.Property;
import atypon.app.node.indexing.bplustree.BPlusTree;
import atypon.app.node.model.Collection;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.List;

public interface IndexingService {
    void createIndexing(IndexObject indexObject) throws IOException;
    void setupIndexing(IndexObject indexObject, BPlusTree<?, List<String>> bPlusTree, String type) throws IOException;
    void indexingInitializer() throws IOException;
    void deleteIndexing(IndexObject indexObject) throws IOException;
    ArrayNode readCollection(Collection collection) throws IOException;
    void IndexingFinalizer();
    void indexDocumentPropertiesIfExists(String database, String collection, JsonNode document);
    List<String> retrieveAndRemoveByProperty(String database, String collection, Property property) throws IOException;
    List<String> retrieveByProperty(String database, String collection, Property property) throws IOException;
    boolean isIndexed(IndexObject indexObject);
    void updateIndexing(String id, JsonNode newValue, JsonNode oldValue, IndexObject indexObject);
}
