package com.github.shawven.calf.oplog.client;

import java.util.Map;

/**
 * @author zhenhui
 * @Ddate Created in 2018/18/01/2018/4:26 PM
 * @modified by
 */
public interface DataSubscriber {

    void subscribe(String clientId, Map<String, DatabaseEventHandler> handlerMap);
}
