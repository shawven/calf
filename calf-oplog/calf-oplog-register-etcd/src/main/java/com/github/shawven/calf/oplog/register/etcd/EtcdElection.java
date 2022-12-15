package com.github.shawven.calf.oplog.register.etcd;

import com.github.shawven.calf.oplog.register.election.AbstractElection;
import com.github.shawven.calf.oplog.register.election.Election;
import com.github.shawven.calf.oplog.register.election.TaskListener;
import io.etcd.jetcd.*;
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

class EtcdElection extends AbstractElection {

    private static final Logger logger = LoggerFactory.getLogger(Election.class);

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private Client client;

    private CloseableClient leaseCloser;

    private final String path;

    private final String uniqueId;

    private final long ttl;


    public EtcdElection(Client client, String path, String uniqueId,
                        Long ttl, boolean autoRequeue, TaskListener listener) {
        super(autoRequeue, listener);
        this.client = client;
        this.path = path;
        this.uniqueId = uniqueId != null ? uniqueId : UUID.randomUUID().toString();
        this.ttl = ttl;
    }


    @Override
    protected void startedCallback() throws ExecutionException, InterruptedException {
        ByteSequence pathKey = ByteSequence.from(path, UTF_8);
        ByteSequence uniqueIdKey = ByteSequence.from(uniqueId, UTF_8);

        logger.info("election uses [{}] as uniqueId", uniqueId);
        client.getKVClient().put(pathKey, uniqueIdKey).get();
    }

    @Override
    public void close() {
        super.close();
        leaseCloser.close();
    }


    @Override
    protected void lockForStart() throws ExecutionException, InterruptedException {
        // acquire distributed lock
        long leaseId = client.getLeaseClient().grant(ttl).get().getID();
        logger.debug("election get leaseId:[{}] and ttl:[{}]", leaseId, ttl);

        this.leaseCloser = client.getLeaseClient().keepAlive(leaseId, new StreamObserver<LeaseKeepAliveResponse>() {
            @Override
            public void onNext(LeaseKeepAliveResponse value) {
                logger.debug("election lease keeps alive for [{}]s:", value.getTTL());
            }

            @Override
            public void onError(Throwable t) {
                logger.debug("election lease renewal Exception!", t.fillInStackTrace());
                CompletableFuture.runAsync(() -> doEnd());
            }

            @Override
            public void onCompleted() {
                logger.info("election lease renewal completed! start canceling task.");
                CompletableFuture.runAsync(() -> doEnd());
            }
        });

        LockResponse lockResponse = client.getLockClient().lock(ByteSequence.from(path, UTF_8), leaseId).get();
        logger.debug("election successfully get Lock [{}]", lockResponse.getKey().toString(UTF_8));
    }
}
