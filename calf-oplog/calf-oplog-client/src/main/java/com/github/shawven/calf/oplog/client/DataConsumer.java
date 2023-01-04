package com.github.shawven.calf.oplog.client;

import java.util.Map;

/**
 * @author zhenhui
 * @Ddate Created in 2018/18/01/2018/4:26 PM
 * @modified by
 */
public interface DataConsumer {

    void startConsumers(String clientId, Map<String, DataSubscribeHandler> handlerMap);

    void stopConsumers(String clientId);
}
