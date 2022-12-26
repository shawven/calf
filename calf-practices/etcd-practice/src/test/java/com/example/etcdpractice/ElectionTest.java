package com.example.etcdpractice;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.Uninterruptibles;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Election;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.election.CampaignResponse;
import io.etcd.jetcd.election.LeaderKey;
import io.etcd.jetcd.election.LeaderResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.support.CloseableClient;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author xw
 * @date 2022/12/23
 */
public class ElectionTest extends EtcdPracticeApplicationTests {

    private Stopwatch electWatch = Stopwatch.createUnstarted();

    @Test
    void election() throws Exception {
        electWatch.start();

        Runnable runnable = this::startElect;

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        executorService.execute(runnable);
        executorService.execute(runnable);
        executorService.execute(runnable);

        executorService.awaitTermination(30, TimeUnit.SECONDS);
    }


    private void startElect() {
        Election electionClient = client.getElectionClient();
        Lease leaseClient = client.getLeaseClient();

        Stopwatch runnerWatch = Stopwatch.createStarted();

        String threadName = Thread.currentThread().getName();
        String name = threadName.substring(threadName.indexOf("thread"));

        try {
            logger.info("{} start elect", name);
            long id = leaseClient.grant(10, 5, TimeUnit.SECONDS).get().getID();

            ByteSequence elect = ByteSequence.from("elect", UTF_8);
            ByteSequence proposal = ByteSequence.from(name, UTF_8);

            electionClient.observe(elect, new Election.Listener() {
                @Override
                public void onNext(LeaderResponse response) {
                    KeyValue kv = response.getKv();
                    if (!kv.getValue().equals(proposal)) {
                        logger.info("{} await for elect，current LeaderKey:{}, proposal:{}", name,
                                kv.getKey().toString(UTF_8), kv.getValue().toString(UTF_8));
                    }
                }

                @Override
                public void onError(Throwable e) {
                    logger.info("elect observe.onError:" + e.getMessage(), e);
                    startElect();
                }

                @Override
                public void onCompleted() {
                    logger.info("elect observe.onCompleted");
                }
            });


            CampaignResponse campaignResponse = electionClient.campaign(elect, id, proposal).get();

            LeaderKey leader = campaignResponse.getLeader();

            logger.info("{} campaignResponse elect:{} LeaderKey:{}", name, leader.getName(), leader.getKey());

            LeaderResponse leaderResponse = electionClient.leader(ByteSequence.from("elect", UTF_8)).get();
            KeyValue kv = leaderResponse.getKv();
            logger.info("{} leaderResponse LeaderKey:{}, proposal:{}", name, kv.getKey().toString(UTF_8),
                    kv.getValue().toString(UTF_8));

            if (kv.getValue().equals(proposal)) {
                logger.info("{} isLeader, cost:{}", name, runnerWatch);
                logger.info("elect cost:{}", electWatch);
            }

            CloseableClient closeableClient = leaseClient.keepAlive(id, new StreamObserver<>() {
                @Override
                public void onNext(LeaseKeepAliveResponse leaseKeepAliveResponse) {
                    logger.debug("{} lease keepAlive.onNext ts:{}, ttl: {}", name, getCurrentTs(), leaseKeepAliveResponse.getTTL());
                }

                @Override
                public void onError(Throwable throwable) {
                    logger.info("{} lease keepAlive.onError: {}", name, throwable.getMessage());
                }

                @Override
                public void onCompleted() {
                    logger.info("{} lease keepAlive.onCompleted", name);
                }
            });

            runnerWatch.reset();
            runnerWatch.start();

            logger.info("{} doWork start", name);
            Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
            logger.info("{} doWork end cost:{}", name,  runnerWatch);

            electWatch.reset();
            electWatch.start();
            electionClient.resign(leader);
            closeableClient.close();

            logger.info("{} prepare enqueue", name);
            startElect();
        } catch (Exception  e) {
            throw new RuntimeException(e);
        }
    }

    private static long getCurrentTs() {
        return System.currentTimeMillis();
    }
}
