package com.github.shawven.calf.oplog.server.dao;

import com.github.shawven.calf.oplog.register.domain.ClientInfo;
import com.github.shawven.calf.oplog.register.domain.DataSourceCfg;
import com.github.shawven.calf.oplog.server.domain.Command;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author xw
 * @date 2021/11/15
 */
public interface ClientDAO {

    List<ClientInfo> listConsumerClient(String queryType);

    List<ClientInfo> listConsumerClient(DataSourceCfg dataSourceCfg);

    List<ClientInfo> listConsumerClientsByKey(String clientInfoKey);

    void addConsumerClient(ClientInfo clientInfo);

    void removeConsumerClient(List<ClientInfo> clientInfos);

    boolean sendCommand(Command command);

    void watcherClientInfo(DataSourceCfg dataSourceCfg, Consumer<List<ClientInfo>> consumer);
}
