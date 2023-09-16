package atypon.app.node.locking;


import atypon.app.node.kafka.KafkaService;
import atypon.app.node.kafka.TopicType;
import atypon.app.node.kafka.event.locking.RemoveLockEvent;
import atypon.app.node.kafka.event.locking.ShareLockEvent;
import atypon.app.node.model.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Service
public class DistributedLocker {
    private static final Logger LOG = LoggerFactory.getLogger(DistributedLocker.class);
    private static final long DEFAULT_RETRY_TIME = 100L;
    private final ValueOperations<String, String> valueOps;
    private final KafkaService kafkaService;
    @Autowired
    public DistributedLocker(final RedisTemplate<String, String> redisTemplate, KafkaService kafkaService) {
        this.valueOps = redisTemplate.opsForValue();
        this.kafkaService = kafkaService;
    }
    public <T> LockExecutionResult<T> lock(final String key,
                                           final int howLongShouldLockBeAcquiredSeconds,
                                           final int lockTimeoutSeconds,
                                           final Callable<T> task) throws Exception {
        return tryToGetLock(() -> {
            final Boolean lockAcquired = valueOps.setIfAbsent(key, key, lockTimeoutSeconds, TimeUnit.SECONDS);
            if (lockAcquired == Boolean.FALSE) {
             // LOG.error("Failed to acquire lock for key '{}'", key);
                return null;
            }
            LOG.info("Successfully acquired lock for key '{}'", key);
            final long startTime = TimeUnit.SECONDS.toMillis(lockTimeoutSeconds);
            kafkaService.broadCast(TopicType.Share_locking, new ShareLockEvent(key, lockTimeoutSeconds));
            sleep(150);
            try {
                T taskResult = task.call();
                ResponseEntity<?> response = (ResponseEntity<?>) taskResult;
                if (response.getStatusCode().is2xxSuccessful()) {
                    Lock lock = new ReentrantLock();
                    lock.lock();
                    while (valueOps.get(key) != null) {
                        LOG.info("Waiting for key '{}' to get unlocked! " + valueOps.get(key), key);
                        sleep(50);
                    }
                    if (System.currentTimeMillis() - startTime > lockTimeoutSeconds + 500) {
                        LOG.info("Potential Dead-Lock detected! key '" + key + "' has held the lock " +
                                "for over '"+ lockTimeoutSeconds +"' ! Removing the lock from Redis!");
                    }
                    lock.unlock();
                } else {
                    kafkaService.broadCast(TopicType.Remove_lock, new RemoveLockEvent(key));
                    sleep(150);
                }
                return LockExecutionResult.buildLockAcquiredResult(taskResult);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                return LockExecutionResult.buildLockAcquiredWithException(e);
            }
        }, key, howLongShouldLockBeAcquiredSeconds);
    }
    public void releaseLock(final String key) {
        LOG.info("Releasing lock for '{}'", key);
        valueOps.getOperations().delete(key);
    }
    private static <T> T tryToGetLock(final Supplier<T> task,
                                      final String lockKey,
                                      final int howLongShouldLockBeAcquiredSeconds) throws Exception {
        final long tryToGetLockTimeout = TimeUnit.SECONDS.toMillis(howLongShouldLockBeAcquiredSeconds);
        final long startTimestamp = System.currentTimeMillis();
        LOG.info("Trying to get the lock with key '{}'", lockKey);
        while (true) {
            final T response = task.get();
            if (response != null) {
                return response;
            }
            sleep(DEFAULT_RETRY_TIME);
            if (System.currentTimeMillis() - startTimestamp > tryToGetLockTimeout) {
                throw new Exception("Failed to acquire lock in " + tryToGetLockTimeout + " milliseconds");
            }
        }
    }
    private static void sleep(final long sleepTimeMillis) {
        try {
            Thread.sleep(sleepTimeMillis);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error(e.getMessage(), e);
        }
    }
}