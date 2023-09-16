package atypon.app.node.kafka.listener.locking;

import atypon.app.node.kafka.event.WriteEvent;
import atypon.app.node.kafka.event.locking.ShareLockEvent;
import atypon.app.node.kafka.listener.EventListener;
import atypon.app.node.locking.DistributedLocker;
import atypon.app.node.locking.RedisService;
import atypon.app.node.model.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class ShareLockListener implements EventListener {
    private static final Logger logger = LoggerFactory.getLogger(ShareLockListener.class);
    @Value("${redis_host}")
    private String redisHostName;
    private final ValueOperations<String, String> valueOps;
    public ShareLockListener(final RedisTemplate<String, String> redisTemplate) {
        valueOps = redisTemplate.opsForValue();
    }
    @Override
    @KafkaListener(topics = "shareLockingTopic")
    public void onEvent(WriteEvent event) throws IOException {
        ShareLockEvent shareLockEvent = (ShareLockEvent) event;
        if (Node.getName().equals(shareLockEvent.getNodeName())) return;
        final String key = shareLockEvent.getKey();
        int time = shareLockEvent.getTime();
        logger.info("Saving lock with key '" + key + "' in '" + redisHostName + "' for '" + time + "' seconds!");
        valueOps.setIfAbsent(key, key, time, TimeUnit.SECONDS);
    }
}
