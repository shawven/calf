package com.github.shawven.calf.util;

import com.google.common.base.Stopwatch;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.commons.lang3.RandomUtils;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xw
 * @date 2023/6/21
 */
public class GuavaCacheTests {

    static final Cache<String, Integer> cache = CacheBuilder.newBuilder()
            .initialCapacity(10)
            .concurrencyLevel(5)
            .expireAfterAccess(10, TimeUnit.DAYS)
            .maximumSize(500)
            .build();

    static Map<String, Integer> map = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        int num = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(1000);
        CountDownLatch latch1 = new CountDownLatch(1);

        for (int i = 0; i < num; i++) {
            executor.execute(() -> {
                get();
//                latch1.countDown();
            });
        }
        System.out.println(executor.toString());
        Uninterruptibles.sleepUninterruptibly(Duration.ofMillis(15));
        set(2);
        System.out.println(executor.toString());

        System.out.format("getCnt %s \n", getCnt.get());

        executor.shutdown();
        Uninterruptibles.awaitTerminationUninterruptibly(executor);


        int redisVal = redis.get();
        int cacheVal = get();
        System.out.format("getCnt %s \n", getCnt.get());
        System.out.format("final redisVal %s \n", redisVal);
        System.out.format("final cacheVal %s \n", cacheVal);
        System.out.format("final loadCnt %s \n", loadCnt.get());

        if (redisVal != cacheVal) {
//            int cnt = 0;
//            do {
//                cacheVal = get();
//                if (redisVal == cacheVal) {
//                    break;
//                }
//                cnt ++;
//            } while (true);
//
            System.err.printf("redis: %d, cache: %d%n", redisVal, cacheVal);
//            stopwatch.stop();
//            System.err.format("time: %s, cnt: %s", stopwatch.elapsed(TimeUnit.MICROSECONDS), cnt);
            CompletableFuture.runAsync(() -> {

            }).join();
        }
    }

    static AtomicInteger loadCnt = new AtomicInteger();
    static AtomicInteger getCnt = new AtomicInteger();

    public static int get()  {
        try {
            int num = getCnt.incrementAndGet() % 2;
            Uninterruptibles.sleepUninterruptibly(Duration.ofMillis(num == 0 ? 10 : 20));

            return cache.get("k", () -> {
                int i = redis.get();
                loadCnt.incrementAndGet();
                return i;
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


    public static void set(int i) {
        System.out.printf("set %s to redis \n",i);
        redis.set(i);
        cache.invalidate("k");
        System.out.format("get: %s, loadCnt: %s", get(), loadCnt.get());
        System.out.println();
    }


    private static Redis redis = new Redis();

    public static class Redis {

        AtomicInteger atomic = new AtomicInteger();

        public int get() {
            return atomic.get();
        }

        public void set(int i) {
            atomic.set(i);
        }
    }
}
