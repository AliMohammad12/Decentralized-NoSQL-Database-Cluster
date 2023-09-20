package bank.app;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


//@Service
public class RedisValueCache {
    private static ConcurrentHashMap<String, Lock> locksMap = new ConcurrentHashMap<>();
    public void writeToFile(String id, String filePath, String content) throws IOException {
        File file = new File(filePath);
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(content);
            writer.write("\n");
        }
    }
    public void write(String id, String message) throws InterruptedException, IOException {
        Lock lock;
        synchronized (id.intern()) {
            if (!locksMap.containsKey(id)) {
                lock = new ReentrantLock();
                locksMap.put(id, lock);
            } else {
                lock = locksMap.get(id);
            }
        }

        System.out.println(Thread.currentThread().getName() + id + " : " + lock);
        boolean acquired = lock.tryLock(3, TimeUnit.SECONDS);
        if (!acquired) {
            System.out.println(Thread.currentThread().getName() + id + " waited 3 seconds and it cancels the write operation");
            return;
        }
        System.out.println(Thread.currentThread().getName() + id + " locking!");
        if (Thread.currentThread().getName().equals("pool-3-thread-1")) {
            System.out.println(id + "Thread 1 is waiting 5 seconds!");
            Thread.sleep(5000); // Sleep for 5 seconds
        }
        writeToFile(id, "test", id + message);
        System.out.println(Thread.currentThread().getName() + id + " unlocking!");
        lock.unlock();
    }

//    @PostConstruct
    public void setup() throws IOException {
        String message = "HelloQ";
        final int numWriters = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numWriters);

        final AtomicInteger[] cnt = {new AtomicInteger(500)};
        int inc = 250;
        final int[] inc2 = {50};

        for (int i = 0; i < numWriters; i++) {
            executor.execute(() -> {
                try {
                    cnt[0].addAndGet(inc + inc2[0]);
                    inc2[0] += 25;
                    write("ABC", message);
                    write("123", message);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        executor.shutdown();

//        FileInputStream fileInputStream = new FileInputStream("yourfile.txt");
//        FileChannel fileChannel = fileInputStream.getChannel();
//
//        FileLock lock = fileChannel.lock();

//        final String filePath = "test.txt";
//        final int numWriters = 1;
//
//        ExecutorService executor = Executors.newFixedThreadPool(numWriters);
//        for (int i = 0; i < numWriters; i++) {
//            executor.execute(() -> {
//                String contentToWrite = "Writer Thread: " + Thread.currentThread().getId() + " - Data";
//                writeToFile(filePath, contentToWrite);
//                System.out.println("Writer Thread: " + Thread.currentThread().getId() + " - Wrote: " + contentToWrite);
//            });
//        }
//        executor.shutdown();
    }
}