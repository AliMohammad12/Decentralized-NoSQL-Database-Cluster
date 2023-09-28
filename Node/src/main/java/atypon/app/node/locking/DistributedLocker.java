package atypon.app.node.locking;


import atypon.app.node.kafka.KafkaService;
import atypon.app.node.kafka.TopicType;
import atypon.app.node.kafka.event.locking.RemoveLockEvent;
import atypon.app.node.kafka.event.locking.ShareLockEvent;
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
        Implementing rate limiter for DoS attacks, can help here too

        Whenever you need to lock anything in that tree you go down from the root and acquire a
        read-lock on everything except the target node itself. The target node gets write lock.
     */

    //  Read locks: These locks allow concurrent read operations, but prevent write operations.
    private <T> LockExecutionResult<T> readLock(final String key,
                                           final int maximumWaitingTimeToAcquireLock,
                                           final int lockTimeoutSeconds,
                                           final Callable<T> task) throws Exception {
        return tryToGetLock(() -> {
            // try to get ReadLock
            if (!acquireReadLock(key, lockTimeoutSeconds)) {
                return null;
            }
            try {
                T taskResult = task.call();
                releaseReadLock(key);
                return LockExecutionResult.buildLockAcquiredResult(taskResult);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                return LockExecutionResult.buildLockAcquiredWithException(e);
            }
        }, key, maximumWaitingTimeToAcquireLock);
    }

    //  Write locks: These locks prevent both read and write operations, so it has to acquire both.
    private <T> LockExecutionResult<T> writeLock(final String key,
                                           final int maximumWaitingTimeToAcquireLock,
                                           final int lockTimeoutSeconds,
                                           final Callable<T> task) throws Exception {
        return tryToGetLock(() -> {
            if (!acquireWriteLock(key, lockTimeoutSeconds)) {
                return null;
            }
            final long startTime = TimeUnit.SECONDS.toMillis(lockTimeoutSeconds);
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
        }, key, maximumWaitingTimeToAcquireLock);
    }


    public <T> LockExecutionResult<T> databaseReadLock(final String dbName,
                                                final int maximumWaitingTimeToAcquireLock,
                                                final int lockTimeoutSeconds,
                                                final Callable<T> task) throws Exception {
        return readLock( DB_PREFIX + dbName, maximumWaitingTimeToAcquireLock, lockTimeoutSeconds, task);
    }
    public <T> LockExecutionResult<T> databaseWriteLock(final String dbName,
                                                       final int maximumWaitingTimeToAcquireLock,
                                                       final int lockTimeoutSeconds,
                                                       final Callable<T> task) throws Exception {
        return writeLock(  DB_PREFIX + dbName, maximumWaitingTimeToAcquireLock, lockTimeoutSeconds, task);
    }


    public <T> LockExecutionResult<T> collectionReadLock(final String dbName, final String collectionName,
                                                       final int maximumWaitingTimeToAcquireLock,
                                                       final int lockTimeoutSeconds,
                                                       final Callable<T> task) throws Exception {
        return tryToGetLock(() -> {
            if (!acquireReadLock(DB_PREFIX + dbName,  lockTimeoutSeconds + 5)) {
                return null;
            }
            try {
                LockExecutionResult<T> result = readLock(COLLECTION_PREFIX + collectionName,
                        maximumWaitingTimeToAcquireLock, lockTimeoutSeconds, task);
                releaseReadLock(DB_PREFIX + dbName);
                return result;
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                return LockExecutionResult.buildLockAcquiredWithException(e);
            }
        }, DB_PREFIX + dbName, maximumWaitingTimeToAcquireLock);
    }
    public <T> LockExecutionResult<T> collectionWriteLock(final String dbName, final String collectionName,
                                                        final int maximumWaitingTimeToAcquireLock,
                                                        final int lockTimeoutSeconds,
                                                        final Callable<T> task) throws Exception {
        return tryToGetLock(() -> {
            if (!acquireReadLock(DB_PREFIX + dbName,  lockTimeoutSeconds + 5)) { // hold for extra time
                return null;
            }
            try {
                LockExecutionResult<T> result = writeLock(COLLECTION_PREFIX + collectionName,
                        maximumWaitingTimeToAcquireLock, lockTimeoutSeconds, task);
                releaseReadLock(DB_PREFIX + dbName);
                return result;
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                return LockExecutionResult.buildLockAcquiredWithException(e);
            }
        }, DB_PREFIX + dbName, maximumWaitingTimeToAcquireLock);
    }



    public <T> LockExecutionResult<T> documentReadLock(final String dbName, final String collectionName, final String id,
                                                         final int maximumWaitingTimeToAcquireLock,
                                                         final int lockTimeoutSeconds,
                                                         final Callable<T> task) throws Exception {
        return tryToGetLock(() -> {
            if (!acquireReadLock(DB_PREFIX + dbName,  lockTimeoutSeconds + 10)) { // hold for extra time
                return null;
            }
            // DB lock acquired
            try {
                return tryToGetLock(() -> {
                    if (!acquireReadLock(COLLECTION_PREFIX + collectionName,  lockTimeoutSeconds + 5)) {
                        return null;
                    }
                    // Collection lock acquired
                    try {
                        // Document lock
                        LockExecutionResult<T> result = readLock(DOCUMENT_PREFIX + id,
                                maximumWaitingTimeToAcquireLock, lockTimeoutSeconds, task);

                        releaseReadLock(DB_PREFIX + dbName);
                        releaseReadLock(COLLECTION_PREFIX + collectionName);
                        return result;
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                        return LockExecutionResult.buildLockAcquiredWithException(e);
                    }
                }, COLLECTION_PREFIX + collectionName, maximumWaitingTimeToAcquireLock);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                return LockExecutionResult.buildLockAcquiredWithException(e);
            }
        }, DB_PREFIX + dbName, maximumWaitingTimeToAcquireLock);
    }
    public <T> LockExecutionResult<T> documentWriteLock(final String dbName, final String collectionName, final String id,
                                                          final int maximumWaitingTimeToAcquireLock,
                                                          final int lockTimeoutSeconds,
                                                          final Callable<T> task) throws Exception {
        return tryToGetLock(() -> {
            if (!acquireReadLock(DB_PREFIX + dbName,  lockTimeoutSeconds + 10)) { // hold for extra time
                return null;
            }
            // DB lock acquired
            try {
                return tryToGetLock(() -> {
                    if (!acquireReadLock(COLLECTION_PREFIX + collectionName,  lockTimeoutSeconds + 5)) { // hold for extra time
                        return null;
                    }
                    // Collection lock acquired
                    try {
                        // Document lock
                        LockExecutionResult<T> result = writeLock(DOCUMENT_PREFIX + id,
                                maximumWaitingTimeToAcquireLock, lockTimeoutSeconds, task);

                        releaseReadLock(DB_PREFIX + dbName);
                        releaseReadLock(COLLECTION_PREFIX + collectionName);
                        return result;
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                        return LockExecutionResult.buildLockAcquiredWithException(e);
                    }
                }, COLLECTION_PREFIX + collectionName, maximumWaitingTimeToAcquireLock);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                return LockExecutionResult.buildLockAcquiredWithException(e);
            }
        }, DB_PREFIX + dbName, maximumWaitingTimeToAcquireLock);
    }


    public void releaseReadLock(final String key) {
        LOG.info("Releasing Read lock for '{}'", key);
        long activeReadOperations = valueOps.decrement(key + ":counter", 1);
        LOG.info("Number of active read operations acquiring '" + key + "' lock = " + activeReadOperations);
        if (activeReadOperations == 0) {
            kafkaService.broadCast(TopicType.Remove_lock, new RemoveLockEvent(key));
            valueOps.getOperations().delete(key + ":counter");
            sleep(200);
        }
    }
    private boolean acquireReadLock(String lockKey, int lockTimeoutSeconds) {
        final Boolean lockAcquired = valueOps.setIfAbsent(lockKey, "ReadLock", lockTimeoutSeconds, TimeUnit.SECONDS);
        if (lockAcquired == Boolean.FALSE) {
            String value = valueOps.get(lockKey);
            if (value.equals("WriteLock")) {
                LOG.error("Failed to acquire {} lock for key '{}'", "ReadLock", lockKey);
                return false;
            }
        }
        LOG.info("Successfully acquired '{}' lock for key '{}'", "ReadLock", lockKey);
        long activeReadOps = valueOps.increment(lockKey + ":counter", 1);
        LOG.info("Active read operations: '{}' for '{}' lock", (activeReadOps), (lockKey));
        kafkaService.broadCast(TopicType.Share_locking, new ShareLockEvent(lockKey, "ReadLock", lockTimeoutSeconds));
        sleep(100);
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
        sleep(100);
        return true;
    }

    public void releaseWriteLock(final String key) {
        LOG.info("Releasing Write lock for '{}'", key);
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