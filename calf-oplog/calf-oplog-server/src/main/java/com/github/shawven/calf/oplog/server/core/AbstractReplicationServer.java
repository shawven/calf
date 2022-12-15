package com.github.shawven.calf.oplog.server.core;

import com.github.shawven.calf.oplog.server.dao.StatusDAO;
import com.github.shawven.calf.oplog.register.domain.DataSourceCfg;
import com.github.shawven.calf.oplog.server.dao.ClientDAO;
import com.github.shawven.calf.oplog.server.dao.DataSourceCfgDAO;
import com.github.shawven.calf.oplog.server.domain.Command;
import com.github.shawven.calf.oplog.server.domain.CommandType;
import com.github.shawven.calf.oplog.register.domain.InstanceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author xw
 * @date 2021/11/15
 */
public abstract class AbstractReplicationServer implements ReplicationServer {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractReplicationServer.class);

    protected DataSourceCfgDAO dataSourceCfgDAO;

    protected ClientDAO clientDAO;

    protected StatusDAO statusDAO;

    public AbstractReplicationServer(DataSourceCfgDAO dataSourceCfgDAO, ClientDAO clientDAO, StatusDAO statusDAO) {
        this.dataSourceCfgDAO = dataSourceCfgDAO;
        this.clientDAO = clientDAO;
        this.statusDAO = statusDAO;
    }

    @Override
    public List<DataSourceCfg> getAllConfigs() {
        return dataSourceCfgDAO.getAll();
    }

    @Override
    public boolean persistDatasourceConfig(DataSourceCfg config) {
        return dataSourceCfgDAO.create(config);
    }

    @Override
    public boolean removeDatasourceConfig(String namespace) {
        return dataSourceCfgDAO.remove(namespace);
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
        return clientDAO.sendCommand(command);
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
        return clientDAO.sendCommand(command);
    }

    /**
     * 获得能够执行任务的主机ip
     * @return
     */
    @Override
    public List<InstanceStatus> getServiceStatus() {
        return statusDAO.getInstanceStatus();
    }

}

