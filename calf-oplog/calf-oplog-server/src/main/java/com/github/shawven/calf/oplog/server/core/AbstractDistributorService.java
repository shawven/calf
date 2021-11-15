package com.github.shawven.calf.oplog.server.core;

import com.github.shawven.calf.oplog.server.datasource.NodeConfig;
import com.github.shawven.calf.oplog.server.datasource.ClientDataSource;
import com.github.shawven.calf.oplog.server.datasource.NodeConfigDataSource;
import com.github.shawven.calf.oplog.server.mode.Command;
import com.github.shawven.calf.oplog.server.mode.CommandType;
import com.github.shawven.calf.base.ServiceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author xw
 * @date 2021/11/15
 */
public abstract class AbstractDistributorService implements DistributorService {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractDistributorService.class);

    protected ScheduledExecutorService scheduledExecutorService;

    @Autowired
    protected NodeConfigDataSource dataSource;

    @Autowired
    protected ClientDataSource clientDataSource;

    @Override
    public List<NodeConfig> getAllConfigs() {

        return dataSource.getAll();
    }

    @Override
    public boolean persistDatasourceConfig(NodeConfig config) {

        boolean res = dataSource.create(config);
        if(!res) {
            return false;
        }
        return true;
    }

    @Override
    public boolean removeDatasourceConfig(String namespace) {

        NodeConfig removedConfig = dataSource.remove(namespace);
        if(removedConfig == null) {
            return false;
        }
        return true;
    }

    /**
     * 向etcd发送开启数据源命令
     *
     *
     * @param namespace
     * @param delegatedIp
     * @return
     */
    @Override
    public boolean startDatasource(String namespace, String delegatedIp) {

        Command command = new Command(namespace, delegatedIp, CommandType.START_DATASOURCE);
        return clientDataSource.sendBinLogCommand(command);
    }

    /**
     * 向etcd发送关闭数据源命令
     *
     * @param namespace
     * @return
     */
    @Override
    public boolean stopDatasource(String namespace) {

        Command command = new Command(namespace, CommandType.STOP_DATASOURCE);
        return clientDataSource.sendBinLogCommand(command);
    }

    /**
     * 获得能够执行任务的主机ip
     * @return
     */
    @Override
    public List<ServiceStatus> getServiceStatus() {
        return clientDataSource.getServiceStatus();
    }

}

