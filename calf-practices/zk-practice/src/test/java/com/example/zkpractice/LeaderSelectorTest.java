package com.example.zkpractice;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author xw
 * @date 2022/12/23
 */
public class LeaderSelectorTest extends ZkPracticeApplicationTests {

    private Stopwatch electWatch = Stopwatch.createUnstarted();

    private String path = "/elect";

    @Test
    public void select() throws InterruptedException {
        electWatch.start();

        Runnable runnable = this::startElect;

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        executorService.execute(runnable);
        executorService.execute(runnable);
        executorService.execute(runnable);

        executorService.awaitTermination(30, TimeUnit.SECONDS);
    }

    public void startElect() {
        String threadName = Thread.currentThread().getName();
        String name = threadName.substring(threadName.indexOf("thread"));

        Stopwatch runnerWatch = Stopwatch.createStarted();

        logger.info("{} start elect", name);
        LeaderSelector leaderSelector = new LeaderSelector(client, path, new LeaderSelectorListener() {

            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                logger.debug("{} connection stateChanged: cost:{}", name, runnerWatch);
                if (newState.isConnected()) {
                    logger.info("{} await for elect: cost:{}", name, runnerWatch);
                }
            }

            @Override
            public void takeLeadership(CuratorFramework client) throws Exception {
                logger.info("{} isLeader: cost:{}", name, runnerWatch);
                logger.info("elect cost {}", electWatch);

                runnerWatch.reset();
                runnerWatch.start();

                logger.info("{} doWork start", name);
                Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
                logger.info("{} doWork end cost:{}", name, runnerWatch);

                electWatch.reset();
                electWatch.start();
            }
        });

        leaderSelector.autoRequeue();
        leaderSelector.start();
    }
}
