package com.Tas.TAS.controller;

import com.Tas.TAS.monitor.TrafficMonitor;
import org.springframework.beans.factory.annotation.Autowired;

public class BuyController {

    @Autowired
    private TrafficMonitor trafficMonitor;

    public String Buy(){
        int currentCount=trafficMonitor.recordRequest();
        System.out.println("No Of Request Right Now: "+currentCount);
        return "Buy Request Processed By Controller";
    }
}
