package atypon.app.node.config;

import atypon.app.node.indexing.IndexObject;
import atypon.app.node.indexing.bplustree.BPlusTree;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class IndexingConfig {
    @Bean(name = "indexRegistry")
    public HashMap<IndexObject, BPlusTree> getIndexRegistry() {
        return new HashMap<>();
    }
}
