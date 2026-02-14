package com.Tas.TAS.limiter;

import com.Tas.TAS.monitor.TrafficMonitor;
import org.springframework.beans.factory.annotation.Autowired;

public class RateLimiter {
    public static final int maxReqPerSecond=10;

    @Autowired
    private TrafficMonitor trafficMonitor;

    public boolean allowedRequest(){
        int currentCount=trafficMonitor.recordRequest();
        if(currentCount<maxReqPerSecond){
            return true;
        }else return false;
    }
}
