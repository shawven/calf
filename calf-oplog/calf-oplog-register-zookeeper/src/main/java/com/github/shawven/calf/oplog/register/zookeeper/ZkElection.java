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
import java.util.concurrent.atomic.AtomicBoolean;

class ZkElection implements Election {

    private static final Logger logger = LoggerFactory.getLogger(Election.class);

    private final String name;
    /**
     * keep racing for leadership when losing
     */
    private final boolean requeue;

    private final ElectionListener listener;

    private final LeaderLatch leaderLatch;

    private final Stopwatch electWatch = Stopwatch.createUnstarted();

    private final AtomicBoolean running = new AtomicBoolean();

    public ZkElection(CuratorFramework client, String path, String name,
                      Long ttl, boolean requeue, ElectionListener listener) {
        this.name = name != null ? name : UUID.randomUUID().toString();
        this.requeue = requeue;
        this.listener = listener;
        this.leaderLatch = new LeaderLatch(client, path, this.name);
    }

    @Override
    public void start() {
        try {
            leaderLatch.addListener(new LeaderLatchListener() {
                @Override
                public void isLeader() {
                    running.set(true);
                    logger.info("election {} isLeader: cost:{}", name, electWatch);

                    try {
                        listener.isLeader();
                        logger.info(name + " election has successfully exec isLeader");
                    } catch (Exception e) {
                        logger.error(name + " election exec isLeader error: " + e.getMessage(), e);
                    }

                    electWatch.reset();
                    electWatch.start();
                }

                @Override
                public void notLeader() {
                    if(running.compareAndSet(true, false)) {
                        try {
                            listener.notLeader();
                            logger.info(name + " election has successfully exec notLeader");
                        } catch (Exception e) {
                            logger.error(name + " election exec notLeader error: " + e.getMessage(), e);
                        }
                    }

                    try {
                        Participant leader = leaderLatch.getLeader();
                        logger.info("get current leader:{}", leader);
                    } catch (Exception e) {
                        logger.info("get current leader error: " + e.getMessage(), e);
                    }
                }
            });
            leaderLatch.start();
        } catch (Exception e) {
            logger.info(String.format("%s election error:%s", name, e.getMessage()));
            requeue();
        }
    }

    private void requeue() {
        if (running.compareAndSet(true, false)) {
            try {
                listener.notLeader();
                logger.debug(name + " election has successfully exec notLeader");
            } catch (Exception e) {
                logger.error(name + " election exec notLeader error: " + e.getMessage(), e);
            }
        }

        if (requeue) {
            logger.info("election {} prepare autoRequeue", name);
            start();
        }
    }

    @Override
    public void close() {
        logger.info("{} election closing", name);

        if (running.compareAndSet(true, false)) {
            try {
                listener.notLeader();
                logger.info(name + " election has successfully exec notLeader");
            } catch (Exception e) {
                logger.error(name + " election exec notLeader error: " + e.getMessage(), e);
            }
        }

        try {
            leaderLatch.close();
        } catch (IOException e) {
            logger.info(name +"election closed error" + e.getMessage(), e);
        }

        logger.info("{} election closed", name);
    }
}
