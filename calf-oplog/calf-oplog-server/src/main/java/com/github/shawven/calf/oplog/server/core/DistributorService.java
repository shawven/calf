package com.github.shawven.calf.oplog.server.core;



import com.github.shawven.calf.oplog.server.datasource.NodeConfig;
import com.github.shawven.calf.base.ServiceStatus;

import java.util.List;

/**
 * @author wanglaomo
 * @since 2019/6/10
 **/
public interface DistributorService {

    void startDistribute();

    boolean persistDatasourceConfig(NodeConfig config);

    boolean removeDatasourceConfig(String namespace);

    void submitBinLogDistributeTask(NodeConfig config);

    void stopBinLogDistributeTask(String namespace);

    List<NodeConfig> getAllConfigs();

    boolean startDatasource(String namespace, String delegatedIp);

    boolean stopDatasource(String namespace);

    List<ServiceStatus> getServiceStatus();
}
