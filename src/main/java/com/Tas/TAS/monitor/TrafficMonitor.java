package com.Tas.TAS.monitor;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class TrafficMonitor {

    private final AtomicInteger requestCountPerSecond = new AtomicInteger(0);
    private final AtomicLong currentSecond = new AtomicLong(System.currentTimeMillis() / 1000);

    // Per-minute rolling total
    private final AtomicInteger requestCountPerMinute = new AtomicInteger(0);
    private final AtomicLong currentMinute = new AtomicLong(System.currentTimeMillis() / 60000);

    // All-time totals
    private final AtomicInteger totalRequests = new AtomicInteger(0);
    private final AtomicInteger totalRejected = new AtomicInteger(0);

    /**
     * Records an incoming request and returns the count in the current second window.
     * Thread-safe: uses CAS (Compare-And-Swap) to avoid race conditions on window reset.
     */
    public int recordRequest() {
        long nowSecond = System.currentTimeMillis() / 1000;
        long lastSecond = currentSecond.get();

        // CAS-based window reset — only one thread will win and reset the counter
        if (nowSecond != lastSecond && currentSecond.compareAndSet(lastSecond, nowSecond)) {
            requestCountPerSecond.set(0);
        }

        long nowMinute = System.currentTimeMillis() / 60000;
        long lastMinute = currentMinute.get();
        if (nowMinute != lastMinute && currentMinute.compareAndSet(lastMinute, nowMinute)) {
            requestCountPerMinute.set(0);
        }

        totalRequests.incrementAndGet();
        requestCountPerMinute.incrementAndGet();
        return requestCountPerSecond.incrementAndGet();
    }

    public void recordRejection() {
        totalRejected.incrementAndGet();
    }

    public int getCurrentRps() {
        return requestCountPerSecond.get();
    }

    public int getCurrentRpm() {
        return requestCountPerMinute.get();
    }

    public int getTotalRequests() {
        return totalRequests.get();
    }

    public int getTotalRejected() {
        return totalRejected.get();
    }

    public double getRejectionRate() {
        int total = totalRequests.get();
        if (total == 0) return 0.0;
        return (double) totalRejected.get() / total * 100;
    }
}
