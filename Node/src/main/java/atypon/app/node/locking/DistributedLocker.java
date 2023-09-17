package atypon.app.node.locking;


import atypon.app.node.kafka.KafkaService;
import atypon.app.node.kafka.TopicType;
import atypon.app.node.kafka.event.locking.RemoveLockEvent;
import atypon.app.node.kafka.event.locking.ShareLockEvent;
import atypon.app.node.model.Node;
import io.lettuce.core.ScriptOutputType;
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
    private static final long DEFAULT_RETRY_TIME = 1000L;
    private final String DB_PREFIX = "DATABASE_LOCK:";
    private final String COLLECTION_PREFIX = "COLLECTION_LOCK:";
    private final String DOCUMENT_PREFIX = "DOCUMENT_LOCK:";
    private final ValueOperations<String, String> valueOps;
    private final KafkaService kafkaService;
    @Autowired
    public DistributedLocker(final RedisTemplate<String, String> redisTemplate, KafkaService kafkaService) {
        this.valueOps = redisTemplate.opsForValue();
        this.kafkaService = kafkaService;
    }
    /* 
        Implement rate limiter for BruteForce attacks..
        Make one read lock!

        Whenever you need to lock anything in that tree you go down from the root and acquire a
        read-lock on everything except the target node itself. The target node gets a write lock.
     */


    //  Read locks: These locks allow concurrent read operations, but prevent write operations.
    public <T> LockExecutionResult<T> readLock(final String key,
                                           final int howLongShouldLockBeAcquiredSeconds,
                                           final int lockTimeoutSeconds,
                                           final Callable<T> task) throws Exception {
        return tryToGetLock(() -> {
            // try to get ReadLock
            if (!acquireReadLock(key, lockTimeoutSeconds)) {
                return null;
            }
            sleep(30000);
            try {
                T taskResult = task.call();
                kafkaService.broadCast(TopicType.Remove_lock, new RemoveLockEvent(key));
                sleep(150);
                return LockExecutionResult.buildLockAcquiredResult(taskResult);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                return LockExecutionResult.buildLockAcquiredWithException(e);
            }
        }, key, howLongShouldLockBeAcquiredSeconds);
    }

    //  Write locks: These locks prevent both read and write operations, so it has to acquire both.
    public <T> LockExecutionResult<T> writeLock(final String key,
                                           final int howLongShouldLockBeAcquiredSeconds,
                                           final int lockTimeoutSeconds,
                                           final Callable<T> task) throws Exception {
        return tryToGetLock(() -> {
            if (!acquireWriteLock(key, lockTimeoutSeconds)) {
                return null;
            }
            final long startTime = TimeUnit.SECONDS.toMillis(lockTimeoutSeconds);
            sleep(30000);
            try {
                T taskResult = task.call();
                ResponseEntity<?> response = (ResponseEntity<?>) taskResult;
                if (response.getStatusCode().is2xxSuccessful()) {
                    Lock lock = new ReentrantLock();
                    lock.lock();
                    while (valueOps.get(key) != null) {
                        LOG.info("Waiting for key '{}' to get unlocked! ", key);
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

    public <T> LockExecutionResult<T> databaseReadLock(final String dbName,
                                                final int howLongShouldLockBeAcquiredSeconds,
                                                final int lockTimeoutSeconds,
                                                final Callable<T> task) throws Exception {
        return readLock(DB_PREFIX + dbName, howLongShouldLockBeAcquiredSeconds, lockTimeoutSeconds, task);
    }
    public <T> LockExecutionResult<T> databaseWriteLock(final String dbName,
                                                       final int howLongShouldLockBeAcquiredSeconds,
                                                       final int lockTimeoutSeconds,
                                                       final Callable<T> task) throws Exception {
        return writeLock(DB_PREFIX + dbName, howLongShouldLockBeAcquiredSeconds, lockTimeoutSeconds, task);
    }

    public <T> LockExecutionResult<T> collectionReadLock(final String dbName, final String collectionName,
                                                       final int howLongShouldLockBeAcquiredSeconds,
                                                       final int lockTimeoutSeconds,
                                                       final Callable<T> task) throws Exception {
        if (!acquireReadLock(DB_PREFIX + dbName,  lockTimeoutSeconds)) {
            return null;
        }
        // acquired lock DB Lock!
        return readLock(COLLECTION_PREFIX + collectionName, howLongShouldLockBeAcquiredSeconds, lockTimeoutSeconds, task);
    }
    public <T> LockExecutionResult<T> collectionWriteLock(final String dbName, final String collectionName,
                                                        final int howLongShouldLockBeAcquiredSeconds,
                                                        final int lockTimeoutSeconds,
                                                        final Callable<T> task) throws Exception {

        if (!acquireReadLock(DB_PREFIX + dbName, lockTimeoutSeconds)) {
            return null;
        }
        // acquired lock DB Lock!
        return writeLock(COLLECTION_PREFIX + collectionName, howLongShouldLockBeAcquiredSeconds, lockTimeoutSeconds, task);
    }



    private boolean acquireReadLock(String lockKey, int lockTimeoutSeconds) {
        final Boolean lockAcquired = valueOps.setIfAbsent(lockKey, "ReadLock", lockTimeoutSeconds, TimeUnit.SECONDS);
        if (lockAcquired == Boolean.FALSE) {
            String value = (String)valueOps.get(lockKey);
            if (value.equals("WriteLock")) {
                LOG.error("Failed to acquire {} lock for key '{}'", "ReadLock", lockKey);
                return false;
            }
        }
        LOG.info("Successfully acquired {} lock for key '{}'", "ReadLock", lockKey);
        kafkaService.broadCast(TopicType.Share_locking, new ShareLockEvent(lockKey, "ReadLock", lockTimeoutSeconds));
        return true;
    }

    private boolean acquireWriteLock(String lockKey, int lockTimeoutSeconds) {
        final Boolean lockAcquired = valueOps.setIfAbsent(lockKey, "WriteLock", lockTimeoutSeconds, TimeUnit.SECONDS);
        if (lockAcquired == Boolean.FALSE) {
            LOG.error("Failed to acquire {} lock for key '{}'", "WriteLock", lockKey);
            return false;
        }
        LOG.info("Successfully acquired {} lock for key '{}'", "WriteLock", lockKey);
        kafkaService.broadCast(TopicType.Share_locking, new ShareLockEvent(lockKey, "WriteLock", lockTimeoutSeconds));
        return true;
    }

    public void releaseLock(final String key) {
        LOG.info("Releasing Read-Write lock for '{}'", key);
        valueOps.getOperations().delete(key);
    }
    private static <T> T tryToGetLock(final Supplier<T> task,
                                      final String lockKey,
                                      final int howLongShouldLockBeAcquiredSeconds) throws Exception {
        final long tryToGetLockTimeout = TimeUnit.SECONDS.toMillis(howLongShouldLockBeAcquiredSeconds);
        final long startTimestamp = System.currentTimeMillis();

        while (true) {
            final T response = task.get();
            if (response != null) {
                return response;
            }
            LOG.info("Failed to get the lock with key '{}', " +
                    "retrying for '" + howLongShouldLockBeAcquiredSeconds + "' seconds!", lockKey);
            sleep(DEFAULT_RETRY_TIME);
            if (System.currentTimeMillis() - startTimestamp > tryToGetLockTimeout) {
                throw new Exception("Failed to acquire lock in '" + howLongShouldLockBeAcquiredSeconds + "' seconds!");
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