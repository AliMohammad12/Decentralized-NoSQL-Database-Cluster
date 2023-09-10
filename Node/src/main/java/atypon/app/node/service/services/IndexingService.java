package atypon.app.node.service.services;

import atypon.app.node.indexing.IndexObject;
import atypon.app.node.indexing.Property;
import atypon.app.node.indexing.bplustree.BPlusTree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.List;

public interface IndexingService {
    void createIndexing(IndexObject indexObject) throws IOException;
    void setupIndexing(IndexObject indexObject, BPlusTree<?, List<String>> bPlusTree, String type);
    void indexingInitializer() throws IOException;
    void deleteIndexing(IndexObject indexObject) throws IOException;
    void IndexingFinalizer();
    void indexDocumentPropertiesIfExists(String database, String collection, ObjectNode document);
    void deleteDocumentByProperty(String database, String collection, Property property) throws IOException;
    ArrayNode readDocumentsByProperty(String database, String collection, Property property) throws IOException;
    boolean isIndexed(IndexObject indexObject);
    void updateIndexing(String id, JsonNode newValue, JsonNode oldValue, IndexObject indexObject);
}
