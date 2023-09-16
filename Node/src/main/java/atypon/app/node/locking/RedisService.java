package atypon.app.node.locking;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
public class RedisService {
    private final ValueOperations<String, Object> valueOps;
    public RedisService(final RedisTemplate<String, Object> redisTemplate) {
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