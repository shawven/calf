package com.example.etcdpractice;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.lock.LockResponse;
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
public class LockTest extends EtcdPracticeApplicationTests{


    @Test
    void lock() throws Exception {
        Runnable runnable = () -> {
            Thread thread = Thread.currentThread();
            long start = System.currentTimeMillis();
            try {
                long id = client.getLeaseClient().grant(10).get().getID();

                client.getLeaseClient().keepAlive(id, new StreamObserver<>() {
                    @Override
                    public void onNext(LeaseKeepAliveResponse value) {
                        logger.info("{}: LeaderSelector lease keeps alive for [{}]s:", thread.getName(), value.getTTL());
                    }

                    @Override
                    public void onError(Throwable t) {
                        logger.info("{}: LeaderSelector lease renewal Exception!", thread.getName(), t.fillInStackTrace());
                    }

                    @Override
                    public void onCompleted() {
                        logger.info("{}: LeaderSelector lease renewal completed! start canceling task.", thread.getName());
                    }
                });

                LockResponse lockResponse = client.getLockClient().lock(ByteSequence.from("/lock", UTF_8), id).get();

                logger.info("{}:{}", thread.getName(), lockResponse.getKey().toString(UTF_8));
            } catch (InterruptedException | ExecutionException e) {
                logger.info(thread.getName() + ":" + e.getMessage(), e);
                logger.info("{}: time:{}" , thread.getName(), (System.currentTimeMillis() - start) / 1000 );
            }

        };

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(runnable);
        executorService.submit(runnable);

        executorService.awaitTermination(30, TimeUnit.SECONDS);
    }
}
