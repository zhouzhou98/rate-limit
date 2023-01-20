package com.limit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class BucketLimit {
    class Node {
        Thread thread;
        public Node(Thread thread) {
            this.thread = thread;
        }
    }
    private LinkedBlockingQueue<Node> linkedBlockingQueue;
    private Integer capacity;
    private TimeUnit timeUnit;
    private Integer flowRate;
    private Long flowNanosRate;
    private AtomicInteger threadNum = new AtomicInteger(1);

    public LinkedBlockingQueue<Node> getLinkedBlockingQueue() {
        return linkedBlockingQueue;
    }

    public BucketLimit(Integer capacity, Integer flowRate, TimeUnit timeUnit) {
        this.linkedBlockingQueue = new LinkedBlockingQueue<>(capacity);
        this.flowRate = flowRate;
        this.timeUnit = timeUnit;
        this.capacity = capacity;
        this.flowNanosRate = this.timeUnit.toNanos(1) / this.flowRate;
        this.consumer();
    }

    private void consumer() {
        Thread thread = new Thread(() -> {
            while (true) {
                Node node = this.linkedBlockingQueue.poll();
                if (Objects.nonNull(node)) {
                    LockSupport.unpark(node.thread);
                }
                LockSupport.parkNanos(this.flowNanosRate);
            }
        });
        thread.setName("漏桶线程-" + threadNum.getAndIncrement());
        thread.start();
    }

    public Boolean acquire() {
        Thread thread = Thread.currentThread();
        Node node = new Node(thread);
        LinkedBlockingQueue<Node> queue = this.getLinkedBlockingQueue();
        boolean res = queue.offer(node);
        if (res) {
            LockSupport.park();
            return true;
        }
        return false;
    }


    public static void main(String[] args) {
        BucketLimit bucketLimit = new BucketLimit(10, 60, TimeUnit.MINUTES);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < 15; i++) {
            new Thread(() -> {
                boolean acquire = bucketLimit.acquire();
                System.out.println(" bucketLimit is : " + acquire+"  , "+sdf.format(new Date()));
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}