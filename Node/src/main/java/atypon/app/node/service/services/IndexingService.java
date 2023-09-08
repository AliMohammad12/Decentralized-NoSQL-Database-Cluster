package atypon.app.node.service.services;

import atypon.app.node.indexing.IndexObject;
import atypon.app.node.indexing.bplustree.BPlusTree;

import java.io.IOException;
import java.util.List;

public interface IndexingService {
    void createIndexing(IndexObject indexObject) throws IOException;
    void setupIndexing(IndexObject indexObject, BPlusTree<?, List<String>> bPlusTree, String type);
    void indexingInitializer() throws IOException;
}
