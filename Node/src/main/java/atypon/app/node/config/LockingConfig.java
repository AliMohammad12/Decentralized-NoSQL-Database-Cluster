package atypon.app.node.config;

import atypon.app.node.indexing.IndexObject;
import atypon.app.node.indexing.bplustree.BPlusTree;
import atypon.app.node.locking.LockObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

@Configuration
public class LockingConfig {
    @Bean(name = "lockingRegistry")
    public ConcurrentHashMap<String, LockObject> getLockingRegistry() {
        return new ConcurrentHashMap<>();
    }
}
