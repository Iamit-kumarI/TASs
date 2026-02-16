package com.Tas.TAS.controller;

import com.Tas.TAS.limiter.RateLimiter;
import com.Tas.TAS.monitor.TrafficMonitor;
import com.Tas.TAS.queue.RequestQueue;
import com.Tas.TAS.service.BuyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
public class BuyController {

//    @Autowired
//    private TrafficMonitor trafficMonitor;

    @Autowired
    private RateLimiter rateLimiter;

    @Autowired
    private BuyService buyService;

    @Autowired
    private RequestQueue requestQueue;
    @GetMapping("/matrix")
    public String queueSize(){
        return "Queue Size "+requestQueue.getQueueSize();
    }

    @PostMapping("/buy")
    public String Buy(){
//        int currentCount=trafficMonitor.recordRequest();
//        System.out.println("No Of Request Right Now: "+currentCount);
//        return "Buy Request Processed By Controller";

        if(!rateLimiter.allowedRequest()){
            return "Buy Request Send to Service";
        }
        boolean added=requestQueue.enqueue(()-> buyService.procesBuy());
        if(!added)return "Server is Busy Try Again Later";
        return "Request Accepted && Queued Successfully";
    }


}
