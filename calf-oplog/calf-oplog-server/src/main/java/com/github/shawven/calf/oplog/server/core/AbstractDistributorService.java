package com.github.shawven.calf.oplog.server.core;

import com.github.shawven.calf.extension.BinaryLogConfig;
import com.github.shawven.calf.extension.ClientDataSource;
import com.github.shawven.calf.extension.ConfigDataSource;
import com.github.shawven.calf.base.BinLogCommand;
import com.github.shawven.calf.base.BinLogCommandType;
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
public abstract class AbstractDistributorService implements DistributorService{
    protected static final Logger logger = LoggerFactory.getLogger(AbstractDistributorService.class);

    protected ScheduledExecutorService scheduledExecutorService;

    @Autowired
    protected ConfigDataSource dataSource;

    @Autowired
    protected ClientDataSource clientDataSource;

    @Override
    public List<BinaryLogConfig> getAllConfigs() {

        return dataSource.getAll();
    }

    @Override
    public boolean persistDatasourceConfig(BinaryLogConfig config) {

        boolean res = dataSource.create(config);
        if(!res) {
            return false;
        }
        return true;
    }

    @Override
    public boolean removeDatasourceConfig(String namespace) {

        BinaryLogConfig removedConfig = dataSource.remove(namespace);
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

        BinLogCommand binLogCommand = new BinLogCommand(namespace, delegatedIp, BinLogCommandType.START_DATASOURCE);
        return clientDataSource.sendBinLogCommand(binLogCommand);
    }

    /**
     * 向etcd发送关闭数据源命令
     *
     * @param namespace
     * @return
     */
    @Override
    public boolean stopDatasource(String namespace) {

        BinLogCommand binLogCommand = new BinLogCommand(namespace, BinLogCommandType.STOP_DATASOURCE);
        return clientDataSource.sendBinLogCommand(binLogCommand);
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

