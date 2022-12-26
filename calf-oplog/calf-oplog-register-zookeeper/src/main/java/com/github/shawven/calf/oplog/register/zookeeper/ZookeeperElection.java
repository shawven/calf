package com.github.shawven.calf.oplog.register.zookeeper;

import com.github.shawven.calf.oplog.register.election.Election;
import com.github.shawven.calf.oplog.register.election.ElectionListener;
import com.google.common.base.Stopwatch;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.recipes.leader.Participant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

class ZookeeperElection implements Election {

    private static final Logger logger = LoggerFactory.getLogger(Election.class);

    /**
     * keep racing for leadership when losing
     */
    private final boolean autoRequeue;

    private final String uniqueId;

    private final LeaderLatch leaderLatch;

    private final Stopwatch electWatch = Stopwatch.createUnstarted();

    public ZookeeperElection(CuratorFramework client, String path, String uniqueId,
                Long ttl, boolean autoRequeue, ElectionListener listener) {
        this.uniqueId = uniqueId != null ? uniqueId : UUID.randomUUID().toString();
        this.autoRequeue = autoRequeue;
        this.leaderLatch = new LeaderLatch(client, path, this.uniqueId);
        leaderLatch.addListener(new LeaderLatchListener() {
            @Override
            public void isLeader() {
                logger.info("election {} isLeader: cost:{}", uniqueId, electWatch);

                listener.isLeader();
                electWatch.reset();
                electWatch.start();
            }

            @Override
            public void notLeader() {
                try {
                    listener.notLeader();

                    Participant leader = leaderLatch.getLeader();
                    logger.info("election {} notLeader, wait for elect, current leader:{}", uniqueId, leader);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void start() {
        try {
            leaderLatch.start();
            leaderLatch.await();
        } catch (Exception e) {
            logger.info(String.format("%s error:%s", uniqueId, e.getMessage()));
        } finally {
            if (autoRequeue) {
                logger.info("election {} prepare autoRequeue", uniqueId);
                start();
            }
        }
    }

    @Override
    public void close() {
        try {
            leaderLatch.close();
            logger.info("election {} closed", uniqueId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
