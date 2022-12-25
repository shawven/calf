package com.github.shawven.calf.oplog.register.zookeeper;

import com.github.shawven.calf.oplog.register.election.AbstractElection;
import com.github.shawven.calf.oplog.register.election.Election;
import com.github.shawven.calf.oplog.register.election.ElectionListener;
import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.recipes.leader.Participant;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

class ZookeeperElection extends Election {

    private static final Logger logger = LoggerFactory.getLogger(Election.class);

    /**
     * keep racing for leadership when losing
     */
    private final boolean autoRequeue;

    /**
     * invoke when acquire leadership
     */
    private final ElectionListener electionListener;

    private final CuratorFramework client;

    private final String path;

    private final String uniqueId;

    private final long ttl;

        this.electionListener = electionListener;
        this.client = client;
        this.path = path;
        th

    public ZookeeperElection(CuratorFramework client, String path, String uniqueId,
                Long ttl, boolean autoRequeue, ElectionListener listener) {
            this.autoRequeue = autoRequeue;is.uniqueId = uniqueId != null ? uniqueId : UUID.randomUUID().toString();
        this.ttl = ttl;
    }

    @Override
    public void start() {
        LeaderLatch leaderLatch = new LeaderLatch(client, path, name);
        leaderLatch.addListener(new LeaderLatchListener() {
            @Override
            public void isLeader() {
                logger.info("{} isLeader: cost:{}", name, runnerWatch);
                logger.info("elect cost {}", electWatch);

                runnerWatch.reset();
                runnerWatch.start();

                logger.info("{} doWork start", name);
                listener
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
    }

    @Override
    public void close() {

    }
}
