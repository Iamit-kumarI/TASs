package com.Tas.TAS.controller;

import com.Tas.TAS.limiter.RateLimiter;
import com.Tas.TAS.model.ApiResponse;
import com.Tas.TAS.model.MetricsSnapshot;
import com.Tas.TAS.monitor.TrafficMonitor;
import com.Tas.TAS.queue.RequestQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MetricsController {

    private final TrafficMonitor trafficMonitor;
    private final RequestQueue requestQueue;
    private final RateLimiter rateLimiter;

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<MetricsSnapshot>> getMetrics() {
        int rps = trafficMonitor.getCurrentRps();
        int maxRps = rateLimiter.getMaxRequestsPerSecond();

        String status;
        if (rps >= maxRps) {
            status = "OVERLOADED";
        } else if (rps >= maxRps * 0.75) {
            status = "DEGRADED";
        } else {
            status = "HEALTHY";
        }

        MetricsSnapshot snapshot = MetricsSnapshot.builder()
                .currentRps(rps)
                .currentRpm(trafficMonitor.getCurrentRpm())
                .totalRequests(trafficMonitor.getTotalRequests())
                .totalRejected(trafficMonitor.getTotalRejected())
                .rejectionRatePercent(trafficMonitor.getRejectionRate())
                .queueSize(requestQueue.getQueueSize())
                .maxQueueSize(requestQueue.getMaxQueueSize())
                .queueProcessed(requestQueue.getProcessedCount())
                .queueDropped(requestQueue.getDroppedCount())
                .rateLimitMaxRps(maxRps)
                .status(status)
                .build();

        return ResponseEntity.ok(ApiResponse.ok("Metrics retrieved", snapshot));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        int rps = trafficMonitor.getCurrentRps();
        int maxRps = rateLimiter.getMaxRequestsPerSecond();
        boolean healthy = rps < maxRps && requestQueue.getQueueSize() < requestQueue.getMaxQueueSize();
        return ResponseEntity.ok(ApiResponse.ok("Health check", healthy ? "UP" : "DEGRADED"));
    }
}
