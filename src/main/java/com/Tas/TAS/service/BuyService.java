package com.Tas.TAS.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BuyService {
    public void procesBuy(){
        System.out.println("Processing with BUY, Thread "+Thread.currentThread().getPriority());
        try{
            Thread.sleep(100);//just simulating work
        }catch (InterruptedException e){
            log.error("Error in buying service",e);
            Thread.currentThread().interrupt();
        }
    }
}
