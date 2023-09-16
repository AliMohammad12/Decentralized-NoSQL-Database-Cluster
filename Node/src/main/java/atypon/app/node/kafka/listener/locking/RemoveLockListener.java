package atypon.app.node.kafka.listener.locking;

import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.kafka.event.locking.RemoveLockEvent;
import atypon.app.node.kafka.event.locking.ShareLockEvent;
import atypon.app.node.kafka.listener.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class RemoveLockListener implements EventListener {
    private static final Logger logger = LoggerFactory.getLogger(ShareLockListener.class);
    @Value("${redis_host}")
    private String redisHostName;
    private final ValueOperations<String, String> valueOps;
    public RemoveLockListener(final RedisTemplate<String, String> redisTemplate) {
        valueOps = redisTemplate.opsForValue();
    }
    @Override
    @KafkaListener(topics = "removeLockTopic")
    public void onEvent(WriteEvent event) throws IOException {
        RemoveLockEvent removeLockEvent = (RemoveLockEvent) event;
        String key = removeLockEvent.getKey();
        logger.info("Removing lock with key '" + key + "'");
        valueOps.getOperations().delete(key);
    }
}
