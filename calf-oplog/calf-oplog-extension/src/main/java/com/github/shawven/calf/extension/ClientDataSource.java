package com.github.shawven.calf.extension;

import com.github.shawven.calf.base.BinLogCommand;
import com.github.shawven.calf.base.ClientInfo;
import com.github.shawven.calf.base.ServiceStatus;

import java.util.List;
import java.util.Map;

/**
 * @author xw
 * @date 2021/11/15
 */
public interface ClientDataSource {

    boolean sendBinLogCommand(BinLogCommand binLogCommand);

    void updateBinLogStatus(String binlogFilename, long binlogPosition, BinaryLogConfig binaryLogConfig, long timestamp);

    List<Map<String, Object>> listBinLogStatus();

    void addBinLogConsumerClient(ClientInfo clientInfo);

    List<ClientInfo> listBinLogConsumerClient(String queryType);

    void removeBinLogConsumerClient(ClientInfo clientInfo);

    List<ClientInfo> listBinLogConsumerClient(BinaryLogConfig binaryLogConfig);

    Map<String, String> getBinaryLogStatus(BinaryLogConfig binaryLogConfig);

    List<ClientInfo> listBinLogConsumerClientByKey(String clientInfoKey);

    void removeBinLogConsumerClient(List<ClientInfo> clientInfos);

    long getLease(String leaseName, long leaseTTL) throws Exception;

    void updateServiceStatus(String serviceKey, ServiceStatus status, long leaseId) throws Exception;

    List<ServiceStatus> getServiceStatus();
}
