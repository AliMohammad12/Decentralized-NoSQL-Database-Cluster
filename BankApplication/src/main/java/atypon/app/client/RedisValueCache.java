package atypon.app.client;

import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;


@Service
public class RedisValueCache {
    private final ValueOperations<String, Object> valueOps;
    public RedisValueCache(final RedisTemplate<String, Object> redisTemplate) {
        valueOps = redisTemplate.opsForValue();
    }
    public void cache(final String key, final Object data) {
        valueOps.set(key, data);
    }
    public Object getCachedValue(final String key) {
        return valueOps.get(key);
    }
    public void deleteCachedValue(final String key) {
        valueOps.getOperations().delete(key);
    }
//    @PostConstruct
//    public void setup() {
//        //cache("hey", "Data1");
////        cache("hey2", "Data2");
////        cache("hey3", "Data3");
////        cache("hey4", "Data4");
//        deleteCachedValue("hey");
//    }
}