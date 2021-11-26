package com.github.shawven.calf.oplog.server.datasource.etcd;

import com.alibaba.fastjson.JSONObject;
import com.github.shawven.calf.oplog.base.ServiceStatus;
import com.github.shawven.calf.oplog.server.datasource.ClientInfo;
import com.github.shawven.calf.oplog.server.datasource.NodeConfig;
import com.github.shawven.calf.oplog.server.mode.Command;

import java.util.List;
import java.util.Map;

/**
 * @author wanglaomo
 * @since 2019/8/6
 **/
public interface EtcdService {

    boolean sendCommand(Command command);

    void updateBinLogStatus(String binlogFilename, long binlogPosition, NodeConfig nodeConfig, long timestamp);

    List<Map<String, Object>> listBinLogStatus();

    void addBinLogConsumerClient(ClientInfo clientInfo);

    List<ClientInfo> listBinLogConsumerClient(String queryType);

    void removeBinLogConsumerClient(ClientInfo clientInfo);

    List<ClientInfo> listBinLogConsumerClient(NodeConfig nodeConfig);

    JSONObject getBinaryLogStatus(NodeConfig nodeConfig);

    List<ClientInfo> listBinLogConsumerClientByKey(String clientInfoKey);

    void removeBinLogConsumerClient(List<ClientInfo> clientInfos);

    long getLease(String leaseName, long leaseTTL) throws Exception;

    void updateServiceStatus(String serviceKey, ServiceStatus status, long leaseId) throws Exception;

    List<ServiceStatus> getServiceStatus();
}
