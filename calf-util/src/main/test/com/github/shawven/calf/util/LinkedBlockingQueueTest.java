package com.github.shawven.calf.util;

import com.google.common.util.concurrent.Uninterruptibles;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * @author xw
 * @date 2024/11/13
 */
public class LinkedBlockingQueueTest {

    static LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(100);
    static ScheduledExecutorService scheduledExecutor1 = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "SqlRecord"));
    static ScheduledExecutorService scheduledExecutor2 = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) {
        scheduledExecutor1.scheduleWithFixedDelay(consume(), 0, 10, TimeUnit.SECONDS);

        scheduledExecutor2.scheduleWithFixedDelay(produce(), 0, 2, TimeUnit.SECONDS);


        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.MINUTES);
    }

    public static Runnable consume() {
        return () -> {
            System.out.println();
            System.out.println("Start consume, queue size: " + queue.size());

            AtomicInteger counter = new AtomicInteger();

            while (true) {
                try {
                    Runnable runnable = queue.poll(100, TimeUnit.MILLISECONDS);
                    if (runnable == null) {
                        System.out.println("Finish consume, size: " + counter.get());
                        break;
                    }
                    counter.incrementAndGet();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public static Runnable produce() {
        return () -> {
            System.out.println("Start produce");
            for (int i = 0; i < 10; i++) {
                int finalI = i;
                queue.offer(() -> {
                    System.out.println("taskId: " + finalI);
                });
                Uninterruptibles.sleepUninterruptibly(10, TimeUnit.MILLISECONDS);
            }
        };
    }
}
