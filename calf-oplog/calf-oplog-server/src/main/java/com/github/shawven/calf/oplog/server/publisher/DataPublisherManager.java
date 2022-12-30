package com.github.shawven.calf.oplog.server.publisher;

import com.github.shawven.calf.oplog.base.Const;
import com.github.shawven.calf.oplog.base.EventBaseDTO;
import com.github.shawven.calf.oplog.register.domain.ClientInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhenhui
 * @Ddate Created in 2018/18/01/2018/4:26 PM
 * @modified by
 */
public class DataPublisherManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataPublisherManager.class);

    private final Map<String, DataPublisher> dataPublisherMap;


    private final AtomicLong publishCount = new AtomicLong(0);

    private long lastPublishCount = 0;

    public DataPublisherManager(Map<String, DataPublisher> dataPublisherMap) {
        this.dataPublisherMap = dataPublisherMap;
    }

    public void publish(Collection<ClientInfo> clientInfos, EventBaseDTO data) {
        for (ClientInfo clientInfo : clientInfos) {
            String topicName = Const.TOPIC_EVENT_DATA.concat(clientInfo.getKey());

            doPublish(clientInfo, topicName, data);

            publishCount.incrementAndGet();
        }
    }

    private void doPublish(ClientInfo clientInfo, String dataKey, EventBaseDTO data) {
        selectDataPublisher(clientInfo).publish(clientInfo.getClientId(), dataKey, data);
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

    public DataPublisher selectDataPublisher(ClientInfo clientInfo) {
        return dataPublisherMap.get(clientInfo.getQueueType().toLowerCase() + "DataPublisher");
    }
}
