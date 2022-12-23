package com.example.zkpractice;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.*;
import org.apache.curator.framework.state.ConnectionState;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author xw
 * @date 2022/12/23
 */
public class LeaderLatchTest extends ZkPracticeApplicationTests {

    private Stopwatch electWatch = Stopwatch.createUnstarted();

    private String path = "/elect";

    @Test
    public void latch() throws InterruptedException {
        electWatch.start();

        Runnable runnable = this::startElect;

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        executorService.execute(runnable);
        executorService.execute(runnable);
        executorService.execute(runnable);

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() ->{
            LeaderLatch leaderLatch = new LeaderLatch(client, path);
            try {
                logger.info("current Leader:{}", leaderLatch.getLeader());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }, 0, 2, TimeUnit.SECONDS);

        executorService.awaitTermination(30, TimeUnit.SECONDS);
    }



    public void startElect() {
        String threadName = Thread.currentThread().getName();
        String name = threadName.substring(threadName.indexOf("thread"));

        Stopwatch runnerWatch = Stopwatch.createStarted();

        logger.info("{} start elect", name);
        try {
            LeaderLatch leaderLatch = new LeaderLatch(client, path, name);
            leaderLatch.addListener(new LeaderLatchListener() {
                @Override
                public void isLeader() {
                    logger.info("{} isLeader: cost:{}", name, runnerWatch);
                    logger.info("elect cost {}", electWatch);

                    runnerWatch.reset();
                    runnerWatch.start();

                    logger.info("{} doWork start", name);
                    Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
                    logger.info("{} doWork end cost:{}", name, runnerWatch);

                    electWatch.reset();
                    electWatch.start();

                    logger.info("{} hasLeadership:{} cost{} ", name,  leaderLatch.hasLeadership(), runnerWatch);
                }

                @Override
                public void notLeader() {
                    try {
                        Participant leader = leaderLatch.getLeader();
                        logger.info("{} notLeader, wait for elect, current leader:{}", name, leader);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            leaderLatch.start();
            leaderLatch.await();
            leaderLatch.close();

            logger.info("{} prepare next loop", name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
