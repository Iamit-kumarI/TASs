package com.Tas.TAS.controller;

import com.Tas.TAS.limiter.RateLimiter;
import com.Tas.TAS.model.ApiResponse;
import com.Tas.TAS.monitor.TrafficMonitor;
import com.Tas.TAS.queue.RequestQueue;
import com.Tas.TAS.service.BuyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
public class BuyController {

    private final RateLimiter rateLimiter;
    private final BuyService buyService;
    private final RequestQueue requestQueue;
    private final TrafficMonitor trafficMonitor;

    @PostMapping("/buy")
    public ResponseEntity<ApiResponse<String>>buy(){
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        log.info("[{}] Incoming buy request", requestId);

        if (!rateLimiter.isAllowed()) {
            trafficMonitor.recordRejection();
            log.warn("[{}] Rate limit exceeded", requestId);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.fail("Rate limit exceeded. Max " +
                            rateLimiter.getMaxRequestsPerSecond() + " requests/sec. Try again..."));
        }

        boolean queued = requestQueue.enqueue(() -> buyService.procesBuy(requestId));
        if (!queued) {
            trafficMonitor.recordRejection();
            log.warn("[{}] Queue full, request dropped", requestId);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.fail("Server is busy. Please try again later."));
        }

        log.info("[{}] Request queued successfully", requestId);
        return ResponseEntity.accepted()
                .body(ApiResponse.ok("Request queued successfully.", "requestId=" + requestId));
    }

    /**
     * GET /api/v1/queue/size
     * Quick check of current queue depth.
     */
    @GetMapping("/queue/size")
    public ResponseEntity<ApiResponse<Integer>> queueSize() {
        return ResponseEntity.ok(
                ApiResponse.ok("Current queue size", requestQueue.getQueueSize())
        );
    }
}