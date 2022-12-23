package com.example.etcdpractice;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author xw
 * @date 2022/12/23
 */
public class WatchTest extends EtcdPracticeApplicationTests {


    @Test
    void watch() throws Exception {
        Watch watchClient = client.getWatchClient();
        Watch.Watcher watcher = watchClient.watch(
                ByteSequence.from("/test/command", UTF_8),
                WatchOption.newBuilder().withPrevKV(true).withNoDelete(true).build(),
                new Watch.Listener() {
                    @Override
                    public void onNext(WatchResponse response) {
                        List<WatchEvent> eventList = response.getEvents();
                        for (WatchEvent watchEvent : eventList) {
                            KeyValue keyValue = watchEvent.getKeyValue();
                            if (keyValue.getValue().toString(UTF_8).equals("hello world")) {
                                client.getKVClient()
                                        .get(ByteSequence.from("/test/command", UTF_8))
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
                ByteSequence.from("/test/command", UTF_8),
                ByteSequence.from("hello world", UTF_8)
        ).get();
        logger.info("put");

        CompletableFuture<GetResponse> future = client.getKVClient()
                .get(ByteSequence.from("/test/command", UTF_8));

        future.get();
        logger.info("end");

    }
}
