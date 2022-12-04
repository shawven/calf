package com.example.etcdpratice;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@SpringBootTest
class EtcdPraticeApplicationTests {

    private final Logger logger = LoggerFactory.getLogger(EtcdPraticeApplicationTests.class);

    @Autowired
    private Client client;

    @Test
    void contextLoads() {
        CompletableFuture completableFuture = new CompletableFuture();
        completableFuture.whenComplete((o, o2) -> {
            logger.info(o.toString());
        });
        completableFuture.complete("hh");

    }

    @Test
    void watch() throws ExecutionException, InterruptedException {
        Watch watchClient = client.getWatchClient();
        Watch.Watcher watcher = watchClient.watch(
                ByteSequence.from("/test/command", StandardCharsets.UTF_8),
                WatchOption.newBuilder().withPrevKV(true).withNoDelete(true).build(),
                new Watch.Listener() {
                    @Override
                    public void onNext(WatchResponse response) {
                        List<WatchEvent> eventList = response.getEvents();
                        for (WatchEvent watchEvent : eventList) {
                            KeyValue keyValue = watchEvent.getKeyValue();
                            if (keyValue.getValue().toString(StandardCharsets.UTF_8).equals("hello world")) {
                                client.getKVClient()
                                        .get(ByteSequence.from("/test/command", StandardCharsets.UTF_8))
                                                .thenAccept(getResponse -> {
                                                    logger.info("watch:" + getResponse.getKvs().toString());
                                                });

                            }
                        }


                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });


        client.getKVClient().put(
                ByteSequence.from("/test/command", StandardCharsets.UTF_8),
                ByteSequence.from("hello world", StandardCharsets.UTF_8)
        ).get();
        logger.info("put");

        CompletableFuture<GetResponse> future = client.getKVClient()
                .get(ByteSequence.from("/test/command", StandardCharsets.UTF_8));

        future.get();
        logger.info("end");

    }

}
