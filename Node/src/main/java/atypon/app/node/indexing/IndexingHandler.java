package atypon.app.node.indexing;

import atypon.app.node.indexing.bplustree.BPlusTree;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class IndexingHandler {
    @Bean(name = "indexRegistry")
    public HashMap<IndexObject, BPlusTree> getIndexRegistry() {
        return new HashMap<>();
    }
}
