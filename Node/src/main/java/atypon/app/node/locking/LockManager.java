package atypon.app.node.locking;

import atypon.app.node.model.Node;
import io.netty.util.Timeout;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.nio.channels.FileLock;
import java.util.concurrent.TimeUnit;

@Service
public class LockManager {
    private final ValueOperations<String, Object> valueOps;
    public LockManager(final RedisTemplate<String, Object> redisTemplate) {
        valueOps = redisTemplate.opsForValue();
    }
    public void storeLock(final String key, final FileLock fileLock, int time, TimeUnit timeUnit) {
        valueOps.set(key, fileLock, time, timeUnit);
    }
    public void createAndStoreLock(final String key, final FileLock fileLock, int time, TimeUnit timeUnit) {
        valueOps.setIfAbsent(key, fileLock, time, timeUnit);
    }
    public FileLock getAndDeleteLock(final String key) {
        return (FileLock) valueOps.getAndDelete(key);
    }
    public FileLock getLock(final String key) {
        return (FileLock) valueOps.get( key);
    }
    public void deleteLock(final String key) {
        valueOps.getOperations().delete(key);
    }
    public FileLock getLockAndExtendTTL(final String key, int time, TimeUnit timeUnit) {
        return (FileLock) valueOps.getAndExpire(key, time, timeUnit);
    }
}



//    public void cache(final String key, final Object data) {
//        valueOps.set(key, data);
//    }
//    public Object getCachedValue(final String key) {
//        return valueOps.get(key);
//    }
//    public void deleteCachedValue(final String key) {
//        valueOps.getOperations().delete(key);
//    }
//    @PostConstruct
//    public void setup() {
//        //cache("hey", "Data1");
////        cache("hey2", "Data2");
////        cache("hey3", "Data3");
////        cache("hey4", "Data4");
//        deleteCachedValue("hey");
//    }