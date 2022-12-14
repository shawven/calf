package com.github.shawven.calf.oplog.server.datasource;

import com.github.shawven.calf.oplog.base.ServiceStatus;
import com.github.shawven.calf.oplog.server.mode.Command;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author xw
 * @date 2021/11/15
 */
public interface ClientDataSource {

    List<ClientInfo> listConsumerClient(String queryType);

    List<ClientInfo> listConsumerClient(NodeConfig nodeConfig);

    List<ClientInfo> listConsumerClientsByKey(String clientInfoKey);

    void addConsumerClient(ClientInfo clientInfo);

    void removeConsumerClient(List<ClientInfo> clientInfos);

    boolean sendCommand(Command command);

    void updateNodeStatus(String filename, long position, NodeConfig nodeConfig);

    List<Map<String, Object>> listStatus();

    Map<String, Object> getNodeStatus(NodeConfig nodeConfig);

    void updateServiceStatus(String serviceKey, ServiceStatus status) throws Exception;

    List<ServiceStatus> getServiceStatus();

    void watcherClientInfo(NodeConfig nodeConfig, Consumer<List<ClientInfo>> consumer);
}
