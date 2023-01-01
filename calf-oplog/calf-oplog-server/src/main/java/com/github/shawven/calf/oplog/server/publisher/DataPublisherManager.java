package com.github.shawven.calf.oplog.server.publisher;

import com.github.shawven.calf.oplog.base.EventBaseDTO;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhenhui
 * @Ddate Created in 2018/18/01/2018/4:26 PM
 * @modified by
 */
public class DataPublisherManager {

    private final Map<String, DataPublisher> dataPublisherMap;

    private final AtomicLong publishCount = new AtomicLong(0);

    private long lastPublishCount = 0;

    public DataPublisherManager(Map<String, DataPublisher> dataPublisherMap) {
        this.dataPublisherMap = dataPublisherMap;
    }

    public void publish(EventBaseDTO data) {
        String destQueue = data.getDestQueue();
        DataPublisher publisher = selectDataPublisher(destQueue);
        if (publisher == null) {
            throw new RuntimeException("The queue publisher was not found: " + destQueue);
        }
        publisher.publish(data);

        publishCount.incrementAndGet();
    }


    public long getPublishCount() {
        return publishCount.get();
    }

    public long publishCountSinceLastTime() {
        long total = publishCount.get();
        long res =  total - lastPublishCount;

        lastPublishCount = total;

        return res;
    }

    public DataPublisher selectDataPublisher(String queueType) {
        return dataPublisherMap.get(queueType.toLowerCase() + "DataPublisher");
    }
}
