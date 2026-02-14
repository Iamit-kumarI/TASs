package com.Tas.TAS.queue;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@Component
public class RequestQueue {

    private static final int maxQueueSize=50;

    private BlockingDeque<Runnable>queue=new LinkedBlockingDeque<>(maxQueueSize);
    public boolean enqueue(Runnable task){
        return queue.offer(task);
    }
    @PostConstruct
    public void startWorker(){
        Thread t1=new Thread(()->{
            while(true){
                try{
                    Runnable task=queue.take();
                    //using take not poll becouse if we do poll then it might give
                    // null but take wait untill it finds the item
                    task.run();
                    Thread.sleep(100);//making sense of doing something
                }catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                }
            }
        });
        t1.setDaemon(true);//jvm runns in background
        t1.start();
    }
}
