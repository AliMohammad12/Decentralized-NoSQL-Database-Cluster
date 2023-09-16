package atypon.app.client.Locking;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
public class RedisValueCache2 {
    private final ValueOperations<String, Object> valueOps;
    public RedisValueCache2(final RedisTemplate<String, Object> redisTemplate) {
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
}