package com.Tas.TAS.queue;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class RequestQueue {

    @Value("${tas.queue.max-size:50}")
    private int maxQueueSize;

    @Value("${tas.queue.thread-pool-size:5}")
    private int threadPoolSize;

    private BlockingQueue<Runnable> queue;
    private ExecutorService executor;

    private final AtomicInteger processedCount = new AtomicInteger(0);
    private final AtomicInteger droppedCount = new AtomicInteger(0);

    @PostConstruct
    public void init() {
        queue = new LinkedBlockingQueue<>(maxQueueSize);

        // Named thread factory for readable stack traces
        ThreadFactory namedFactory = r -> {
            Thread t = new Thread(r, "tas-worker-" + processedCount.get());
            t.setDaemon(true);
            return t;
        };

        executor = Executors.newFixedThreadPool(threadPoolSize, namedFactory);
        startDispatcher();
        log.info("RequestQueue initialized — maxSize={}, threads={}", maxQueueSize, threadPoolSize);
    }

    private void startDispatcher() {
        Thread dispatcher = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Runnable task = queue.take(); // blocks until task available
                    executor.submit(() -> {
                        try {
                            task.run();
                            processedCount.incrementAndGet();
                        } catch (Exception e) {
                            log.error("Task execution failed", e);
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.info("Dispatcher thread interrupted, shutting down.");
                }
            }
        }, "tas-dispatcher");
        dispatcher.setDaemon(true);
        dispatcher.start();
    }

    public boolean enqueue(Runnable task) {
        if (!queue.offer(task)) {
            droppedCount.incrementAndGet();
            log.warn("Queue full ({}/{}), request dropped.", queue.size(), maxQueueSize);
            return false;
        }
        return true;
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down RequestQueue executor...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public int getQueueSize() { return queue.size(); }
    public int getProcessedCount() { return processedCount.get(); }
    public int getDroppedCount() { return droppedCount.get(); }
    public int getMaxQueueSize() { return maxQueueSize; }
}
