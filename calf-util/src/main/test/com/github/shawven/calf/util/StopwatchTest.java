package com.github.shawven.calf.util;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.Uninterruptibles;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author xw
 * @date 2024/9/25
 */
public class StopwatchTest {

    @Test
    public void test() {
        Stopwatch stopwatch = Stopwatch.createStarted();

        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        System.out.println("cost" + stopwatch);

        stopwatch.reset().start();
        Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
        System.out.println("cost" + stopwatch);

        stopwatch.reset().start();
        Uninterruptibles.sleepUninterruptibly(3, TimeUnit.MILLISECONDS);
        System.out.println("cost" + stopwatch);
    }
}
