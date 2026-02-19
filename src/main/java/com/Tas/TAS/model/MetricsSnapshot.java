package com.Tas.TAS.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MetricsSnapshot {
    private int currentRps;
    private int currentRpm;
    private int totalRequests;
    private int totalRejected;
    private double rejectionRatePercent;
    private int queueSize;
    private int maxQueueSize;
    private int queueProcessed;
    private int queueDropped;
    private int rateLimitMaxRps;
    private String status;
}