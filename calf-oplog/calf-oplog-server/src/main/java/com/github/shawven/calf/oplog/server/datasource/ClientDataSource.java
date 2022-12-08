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

    boolean sendBinLogCommand(Command command);

    void updateBinLogStatus(String binlogFilename, long binlogPosition, NodeConfig nodeConfig, long timestamp);

    List<Map<String, Object>> listBinLogStatus();

    void addBinLogConsumerClient(ClientInfo clientInfo);

    List<ClientInfo> listBinLogConsumerClient(String queryType);

    void removeBinLogConsumerClient(ClientInfo clientInfo);

    List<ClientInfo> listBinLogConsumerClient(NodeConfig nodeConfig);

    Map<String, Object> getBinaryLogStatus(NodeConfig nodeConfig);

    List<ClientInfo> listBinLogConsumerClientByKey(String clientInfoKey);

    void removeBinLogConsumerClient(List<ClientInfo> clientInfos);

    long getLease(String leaseName, long leaseTTL) throws Exception;

    void updateServiceStatus(String serviceKey, ServiceStatus status, long leaseId) throws Exception;

    List<ServiceStatus> getServiceStatus();

    void watcherClientInfo(NodeConfig nodeConfig, Consumer<List<ClientInfo>> consumer);
}
