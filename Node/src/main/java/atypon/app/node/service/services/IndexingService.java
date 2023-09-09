package atypon.app.node.service.services;

import atypon.app.node.indexing.IndexObject;
import atypon.app.node.indexing.Property;
import atypon.app.node.indexing.bplustree.BPlusTree;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.List;

public interface IndexingService {
    void createIndexing(IndexObject indexObject) throws IOException;
    void setupIndexing(IndexObject indexObject, BPlusTree<?, List<String>> bPlusTree, String type);
    void indexingInitializer() throws IOException;
    void deleteIndexing(IndexObject indexObject) throws IOException;
    void IndexingFinalizer();
    void addDocument(String database, String collection, ObjectNode document);
    void deleteDocument(String database, String collection, Property property) throws IOException;
    boolean isIndexed(IndexObject indexObject);
}
