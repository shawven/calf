package com.github.shawven.calf.oplog.server.publisher;

import com.github.shawven.calf.oplog.base.EventBaseDTO;
import com.github.shawven.calf.oplog.register.domain.ClientInfo;

/**
 * @author zhenhui
 * @Ddate Created in 2018/18/01/2018/4:26 PM
 * @modified by
 */
public interface DataPublisher {

    String DATA = "BIN-LOG-DATA-";

    void publish(String clientId, String dataKey, EventBaseDTO data);

    boolean destroy(ClientInfo clientInfo);
}
