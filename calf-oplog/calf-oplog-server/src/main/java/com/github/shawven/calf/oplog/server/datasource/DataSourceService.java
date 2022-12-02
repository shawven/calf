package com.github.shawven.calf.oplog.server.datasource;

import java.util.List;
import java.util.Map;

/**
 * @author xw
 * @date 2021/11/15
 */
public interface DataSourceService {

    List<ClientInfo> listBinLogConsumerClient(String queryType);

    void addBinLogConsumerClient(ClientInfo clientInfo);

    void removeBinLogConsumerClient(List<ClientInfo> clientInfo);

    List<Map<String, Object>> listBinLogStatus();

    List<ClientInfo> listBinLogConsumerClientByKey(String clientInfoKey);
}
