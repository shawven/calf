package com.github.shawven.calf.oplog.server.datasource.etcd;

import com.github.shawven.calf.oplog.server.datasource.leaderselector.AbstractLeaderSelector;
import com.github.shawven.calf.oplog.server.datasource.leaderselector.LeaderSelector;
import com.github.shawven.calf.oplog.server.datasource.leaderselector.TaskListener;
import io.etcd.jetcd.*;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.lock.LockResponse;
import io.etcd.jetcd.support.CloseableClient;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

class EtcdLeaderSelector extends AbstractLeaderSelector {

    private static final Logger logger = LoggerFactory.getLogger(LeaderSelector.class);

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private Client client;

    private CloseableClient leaseCloser;

    private final String path;

    private final String uniqueId;

    private final long ttl;


    public EtcdLeaderSelector (Client client, String path, String uniqueId,
                               Long ttl, boolean autoRequeue, TaskListener listener) {
        super(autoRequeue, listener);
        this.client = client;
        this.path = path;
        this.uniqueId = uniqueId != null ? uniqueId : UUID.randomUUID().toString();
        this.ttl = ttl;
    }


    @Override
    protected void doStartCallback() throws ExecutionException, InterruptedException {
        ByteSequence pathKey = ByteSequence.from(path, UTF_8);
        ByteSequence uniqueIdKey = ByteSequence.from(uniqueId, UTF_8);

        logger.info("LeaderSelector uses [{}] as uniqueId", uniqueId);
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
        logger.debug("LeaderSelector get leaseId:[{}] and ttl:[{}]", leaseId, ttl);

        this.leaseCloser = client.getLeaseClient().keepAlive(leaseId, new StreamObserver<LeaseKeepAliveResponse>() {
            @Override
            public void onNext(LeaseKeepAliveResponse value) {
                logger.debug("LeaderSelector lease keeps alive for [{}]s:", value.getTTL());
            }

            @Override
            public void onError(Throwable t) {
                logger.debug("LeaderSelector lease renewal Exception!", t.fillInStackTrace());
                CompletableFuture.runAsync(() -> doEnd());
            }

            @Override
            public void onCompleted() {
                logger.info("LeaderSelector lease renewal completed! start canceling task.");
                CompletableFuture.runAsync(() -> doEnd());
            }
        });

        LockResponse lockResponse = client.getLockClient().lock(ByteSequence.from(path, UTF_8), leaseId).get();
        logger.debug("LeaderSelector successfully get Lock [{}]", lockResponse.getKey().toString(UTF_8));
    }
}
