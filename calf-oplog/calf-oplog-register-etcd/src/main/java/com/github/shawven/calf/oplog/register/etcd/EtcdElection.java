package com.github.shawven.calf.oplog.register.etcd;

import com.github.shawven.calf.oplog.register.election.Election;
import com.github.shawven.calf.oplog.register.election.ElectionListener;
import com.google.common.base.Stopwatch;
import io.etcd.jetcd.*;
import io.etcd.jetcd.election.CampaignResponse;
import io.etcd.jetcd.election.LeaderKey;
import io.etcd.jetcd.election.LeaderResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.lock.LockResponse;
import io.etcd.jetcd.support.CloseableClient;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

class EtcdElection implements Election {

    private static final Logger logger = LoggerFactory.getLogger(Election.class);

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private Client client;

    private final String path;

    private final String uniqueId;

    private final long ttl;

    private final boolean autoRequeue;

    private final ElectionListener listener;

    private CloseableClient closeableClient;

    private final Stopwatch electWatch = Stopwatch.createUnstarted();

    public EtcdElection(Client client, String path, String uniqueId,
                        Long ttl, boolean autoRequeue, ElectionListener listener) {
        this.client = client;
        this.path = path;
        this.uniqueId = uniqueId != null ? uniqueId : UUID.randomUUID().toString();
        this.ttl = ttl;
        this.autoRequeue = autoRequeue;
        this.listener = listener;
    }

    @Override
    public void start() {
        String name = this.uniqueId;
        ByteSequence elect = ByteSequence.from(path, UTF_8);
        ByteSequence proposal = ByteSequence.from(name, UTF_8);

        logger.info("{} start elect", name);
        electWatch.start();

        try {
            long id = getLeaseId();

            observer(name, elect, proposal);

            CampaignResponse campaignResponse = client.getElectionClient().campaign(elect, id, proposal).get();

            LeaderKey leader = campaignResponse.getLeader();

            logger.info("{} campaignResponse elect:{} LeaderKey:{}", name, leader.getName(), leader.getKey());

            LeaderResponse leaderResponse = client.getElectionClient().leader(ByteSequence.from("elect", UTF_8)).get();
            KeyValue kv = leaderResponse.getKv();
            logger.info("{} leaderResponse LeaderKey:{}, proposal:{}", name, kv.getKey().toString(UTF_8),
                    kv.getValue().toString(UTF_8));

            if (kv.getValue().equals(proposal)) {
                logger.info("{} isLeader, cost:{}", name, electWatch);
                listener.isLeader();
            }

            keepAlive(name, id);
        } catch (Exception e) {
            logger.error(name + " election error: " + e.getMessage(), e);
        } finally {
            if (autoRequeue) {
                logger.info("{} prepare enqueue", name);
                start();
            }
        }
    }

    private void observer(String name, ByteSequence elect, ByteSequence proposal) {
        client.getElectionClient().observe(elect, new io.etcd.jetcd.Election.Listener() {
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
                start();
            }

            @Override
            public void onCompleted() {
                logger.info("elect observe.onCompleted");
            }
        });
    }

    private long getLeaseId() throws InterruptedException, ExecutionException {
        return client.getLeaseClient().grant(ttl, ttl, TimeUnit.SECONDS).get().getID();
    }

    private void keepAlive(String name, long id) {
        closeableClient = client.getLeaseClient().keepAlive(id, new StreamObserver<LeaseKeepAliveResponse>() {
            @Override
            public void onNext(LeaseKeepAliveResponse leaseKeepAliveResponse) {
                logger.debug("{} lease keepAlive.onNext ts:{}, ", name, leaseKeepAliveResponse.getTTL());
            }

            @Override
            public void onError(Throwable throwable) {
                logger.info("{} lease keepAlive.onError: {}", name, throwable.getMessage());
                start();
            }

            @Override
            public void onCompleted() {
                logger.info("{} lease keepAlive.onCompleted", name);
            }
        });
    }

    @Override
    public void close() {
        closeableClient.close();
    }
}
