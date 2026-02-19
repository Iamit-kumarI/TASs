package com.Tas.TAS.limiter;

import com.Tas.TAS.monitor.TrafficMonitor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RateLimiter {
    public static final int maxReqPerSecond=10;

    @Getter
    @Value("${tas.rate-limit.max-rps:10}")
    private int maxRequestsPerSecond;

    private final TrafficMonitor trafficMonitor;

    public RateLimiter(TrafficMonitor trafficMonitor) {
        this.trafficMonitor = trafficMonitor;
    }

    /**
     * Returns true if the request is within the allowed rate, false if it should be throttled.
     */
    public boolean isAllowed() {
        int currentCount = trafficMonitor.recordRequest();
        return currentCount <= maxRequestsPerSecond;
    }

}
