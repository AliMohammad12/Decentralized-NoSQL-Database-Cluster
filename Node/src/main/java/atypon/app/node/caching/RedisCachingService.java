package atypon.app.node.caching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisCachingService {
    private static final Logger logger = LoggerFactory.getLogger(RedisCachingService.class);
    private final ValueOperations<String, Object> valueOps;
    public RedisCachingService(final RedisTemplate<String, Object> redisTemplate) {
        valueOps = redisTemplate.opsForValue();
    }
    public void cache(final String key, final Object data) {
        valueOps.set(key, data);
    }
    public void cache(final String key, final Object data, int timeSeconds) {
        logger.info("Storing '{}' in cache for '{}' seconds!", key, timeSeconds);
        valueOps.set(key, data, timeSeconds, TimeUnit.SECONDS);
    }
    public boolean isCached(final String key) {
        return valueOps.get(key) != null;
    }
    public Object getCachedValue(final String key) {
        logger.info("Retrieving '{}' from cache!", key);
        return valueOps.get(key);
    }
    public void deleteCachedValue(final String key) {
        logger.info("Removing '{}' from cache!", key);
        valueOps.getOperations().delete(key);
    }
}