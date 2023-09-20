package bank.app;


import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FileLockExample {
//    private final Lock writeLock = new ReentrantLock();
//    public void writeToFile(String filePath, String content) {
//        try {
//            // Acquire write lock
//            System.out.println("Write THREAD " + Thread.currentThread().getId() + " is acquiring the write lock.");
//            writeLock.lock();
//            System.out.println("Write THREAD " + Thread.currentThread().getId() + " got the LOCK.");
//
////            File file = new File(filePath);
//
//            System.out.println("Write THREAD  " + Thread.currentThread().getId() + " is writing to the file.");
//            Files.delete(Path.of(filePath));
//            Thread.sleep(1000); // Sleep for 20 seconds to simulate a slow write
////            try (FileWriter writer = new FileWriter(file, true)) {
////                writer.write(content);
////                writer.write("\n");
////            }
//
//            System.out.println("Write THREAD  " + Thread.currentThread().getId() + " released the exclusive lock.");
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        } finally {
//            System.out.println("Write THREAD  " + Thread.currentThread().getId() + " released the write lock.");
//            writeLock.unlock();
//        }
//    }
//
//    public String readFromFile(String filePath) {
//        try {
//            Thread.sleep(1000);
//            return new String(Files.readAllBytes(Paths.get(filePath)));
//        } catch (IOException | InterruptedException e) {
//            // Handle exceptions
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    public static void main(String[] args) {
//        final String filePath = "test.txt";
//        final int numReaders = 1500;
//        final int numWriters = 1;
//
//        FileLockExample fileService = new FileLockExample();
//
//        ExecutorService executor = Executors.newFixedThreadPool(numReaders + numWriters);
//
//        for (int i = 0; i < numWriters; i++) {
//            executor.execute(() -> {
//                String contentToWrite = "Writer Thread: " + Thread.currentThread().getId() + " - Data";
//                fileService.writeToFile(filePath, contentToWrite);
//                System.out.println("Writer Thread: " + Thread.currentThread().getId() + " - Wrote: " + contentToWrite);
//            });
//        }
//
//        // Create reader threads
//        for (int i = 0; i < numReaders; i++) {
//            executor.execute(() -> {
//                String content = fileService.readFromFile(filePath);
//                System.out.println("Reader Thread: " + Thread.currentThread().getId() + " - Content: " + content);
//            });
//        }
//
//        executor.shutdown();
//    }
}
