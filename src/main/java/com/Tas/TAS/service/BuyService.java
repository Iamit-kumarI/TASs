package com.Tas.TAS.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BuyService {
    public void procesBuy(String requestId){
        log.info("[{}] Processing buy on thread: {}", requestId, Thread.currentThread().getName());
        try {
            // Simulating actual purchase processing work
            Thread.sleep(100);
            log.info("[{}] Buy processed successfully", requestId);
        } catch (InterruptedException e) {
            log.error("[{}] Buy processing interrupted", requestId, e);
            Thread.currentThread().interrupt();
        }
    }
}
