package com.github.shawven.calf.oplog.server.core;



import com.github.shawven.calf.oplog.register.domain.DataSourceCfg;
import com.github.shawven.calf.oplog.register.domain.InstanceStatus;

import java.util.List;

/**
 * @author wanglaomo
 * @since 2019/6/10
 **/
public interface ReplicationServer {

    void start();

    boolean persistDatasourceConfig(DataSourceCfg config);

    boolean removeDatasourceConfig(String namespace);

    void startTask(DataSourceCfg config);

    void stopTask(String namespace);

    List<DataSourceCfg> getAllConfigs();

    boolean startDatasource(String namespace, String delegatedIp);

    boolean stopDatasource(String namespace);

    List<InstanceStatus> getServiceStatus();
}
