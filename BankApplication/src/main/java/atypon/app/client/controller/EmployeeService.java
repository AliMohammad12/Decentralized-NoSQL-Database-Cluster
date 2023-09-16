package atypon.app.client.controller;

import atypon.app.client.Locking.DistributedLocker;
import atypon.app.client.Locking.LockExecutionResult;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EmployeeService {
    private static final Logger LOG = LoggerFactory.getLogger(EmployeeService.class);

    private final DistributedLocker locker;
    @Autowired
    public EmployeeService(DistributedLocker locker) {
        this.locker = locker;
    }
    @PostConstruct
    private void setup() {
        CompletableFuture.runAsync(() -> runTask("1", 3000));
        CompletableFuture.runAsync(() -> runTask("2", 1000));
        CompletableFuture.runAsync(() -> runTask("3", 100));
    }

    private void runTask(final String taskNumber, final long sleep) {
        LOG.info("Running task : '{}'", taskNumber);

        LockExecutionResult<String> result = locker.lock("some-key", 5, 6, () -> {
            LOG.info(taskNumber + ": Sleeping for '{}' ms", sleep);
            Thread.sleep(sleep);
            LOG.info(taskNumber + ": Executing task '{}'", taskNumber);
            return taskNumber;
        });

        LOG.info(taskNumber + ": Task result : '{}' -> exception : '{}'", result.getResultIfLockAcquired(), result.hasException());
    }
}
