package com.github.shawven.calf.oplog.server.publisher;

import com.github.shawven.calf.base.EventBaseDTO;

/**
 * @author zhenhui
 * @Ddate Created in 2018/18/01/2018/4:26 PM
 * @modified by
 */
public interface DataPublisher {

    void publish(String clientId, String dataKey, EventBaseDTO data);
}
