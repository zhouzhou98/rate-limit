package com.limit;

import java.util.concurrent.atomic.AtomicInteger;

public class CounterLimit {
    private Integer maxCount;
    private Integer timeInterval;
    private Long startTime;
    private AtomicInteger atomicInteger = new AtomicInteger(0);
    public CounterLimit(Integer maxCount, Integer timeInterval) {
        this.maxCount = maxCount;
        this.timeInterval = timeInterval;
        this.startTime = System.currentTimeMillis();
    }
    public Boolean limit() {
        this.atomicInteger.addAndGet(1);
        if (System.currentTimeMillis() - startTime > timeInterval * 1000) {
            this.atomicInteger = new AtomicInteger(1);
            this.startTime = System.currentTimeMillis();
            return true;
        }
        if (this.atomicInteger.get() == this.maxCount) {
            this.atomicInteger = new AtomicInteger(0);
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws InterruptedException {
        CounterLimit limiter=new CounterLimit(10, 1000);
        boolean flag;
        for(int i=0;i<60;i++){
            flag= limiter.limit();
            if(!flag){
                System.out.println("超过流量限制，限流了");
                Thread.sleep(2000);
            } else {
                System.out.println("没超过流量限制");
            }
        }
    }
}
