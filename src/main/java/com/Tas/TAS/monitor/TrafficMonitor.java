package com.Tas.TAS.monitor;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TrafficMonitor {

    private AtomicInteger requsetCount=new AtomicInteger(0);
    private long currentSecond=System.currentTimeMillis()/1000;

    public int recordRequest(){
        long nowTime=System.currentTimeMillis()/1000;
        if(nowTime!=currentSecond){
            currentSecond=nowTime;
            requsetCount.set(0);
        }
        return requsetCount.incrementAndGet();
    }
    public int getCurrentRequestCount(){
        return requsetCount.get();
    }
}
