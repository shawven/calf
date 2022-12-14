package com.github.shawven.calf.oplog.server.datasource.etcd;

import com.github.shawven.calf.oplog.server.datasource.DataSource;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import io.reactivex.rxjava3.core.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class EtcdDataSource implements DataSource {

    private final Logger logger = LoggerFactory.getLogger(EtcdDataSource.class);

    private final Client client;



    public EtcdDataSource(Client client) {
        this.client = client;
    }

    @Override
    public List<String> list(String key) {
        GetResponse response;
        try {
            response = client.getKVClient().get(
                    ByteSequence.from(key, StandardCharsets.UTF_8),
                    GetOption.newBuilder().isPrefix(true).build()
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
        return response.getKvs().stream()
                .map(kv -> kv.getValue().toString(StandardCharsets.UTF_8))
                .collect(Collectors.toList());
    }

    @Override
    public String get(String key) {
        GetResponse response;
        try {
            response = client.getKVClient().get(ByteSequence.from(key, StandardCharsets.UTF_8)).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
        return response.getKvs().get(0).getValue().toString(StandardCharsets.UTF_8);
    }

    @Override
    public void set(String key, String val) {
        client.getKVClient().put(
                ByteSequence.from(key, StandardCharsets.UTF_8),
                ByteSequence.from(val, StandardCharsets.UTF_8));
    }

    @Override
    public void set(String key, String val, long ttl) {
        client.getLeaseClient().grant(ttl).thenAccept(response -> {
            long leaseId = response.getID();
            client.getKVClient().put(
                    ByteSequence.from(key, StandardCharsets.UTF_8),
                    ByteSequence.from(val, StandardCharsets.UTF_8),
                    PutOption.newBuilder().withLeaseId(leaseId).build()
            );
        });
    }

    @Override
    public void watch(String key, Emitter<String> emitter) {
        client.getWatchClient().watch(
                ByteSequence.from(key, StandardCharsets.UTF_8),
                WatchOption.newBuilder().isPrefix(true).build(),
                new Watch.Listener() {
                    @Override
                    public void onNext(WatchResponse response) {
                        List<WatchEvent> events = response.getEvents();
                        for (WatchEvent event : events) {
                            if (WatchEvent.EventType.PUT.equals(event.getEventType())) {
                                String string = event.getKeyValue().getValue().toString(StandardCharsets.UTF_8);
                                emitter.onNext(string);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        emitter.onError(throwable);
                    }

                    @Override
                    public void onCompleted() {
                        emitter.onComplete();
                    }
                }
        );
    }
}
