package com.github.shawven.calf.oplog.server.core;

import com.github.shawven.calf.oplog.server.support.NetUtils;
import com.github.shawven.calf.oplog.server.ServerWatcher;
import com.github.shawven.calf.oplog.server.ops.StatusOps;
import com.github.shawven.calf.oplog.register.domain.DataSourceCfg;
import com.github.shawven.calf.oplog.server.ops.ClientOps;
import com.github.shawven.calf.oplog.server.ops.DataSourceCfgOps;
import com.github.shawven.calf.oplog.server.domain.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author xw
 * @date 2021/11/15
 */
public abstract class AbstractReplicationServer implements ReplicationServer {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractReplicationServer.class);

    protected DataSourceCfgOps dataSourceCfgOps;

    protected ClientOps clientOps;

    protected StatusOps statusOps;

    public AbstractReplicationServer(DataSourceCfgOps dataSourceCfgOps,
                                     ClientOps clientOps,
                                     StatusOps statusOps) {
        this.dataSourceCfgOps = dataSourceCfgOps;
        this.clientOps = clientOps;
        this.statusOps = statusOps;
    }


    @Override
    public void start() {
        // 1. 获得初始配置信息
        List<DataSourceCfg> configList = dataSourceCfgOps.getByDataSourceType(dataSourceType());

        // 2. 竞争每个数据源的Leader
        configList.forEach(config -> {
            // 在线程中启动事件监听
            if(config.isActive()) {
                doStart(config);
            }
        });

        // 3. 注册数据源Config 命令Watcher
        dataSourceCfgOps.registerServerWatcher(new ServerWatcherImpl());

        // 4. 服务节点上报
        updateInstanceStatus();
    }

    protected abstract void doStart(DataSourceCfg config);

    protected abstract void updateInstanceStatus();


    private class ServerWatcherImpl implements ServerWatcher {


        @Override
        public void start(Command command) {
            String namespace = command.getNamespace();
            String delegatedIp = command.getDelegatedIp();
            if(!StringUtils.hasText(namespace)) {
                return;
            }

            if(StringUtils.hasText(delegatedIp)) {
                CompletableFuture.runAsync(() -> {
                    DataSourceCfg config = dataSourceCfgOps.getByNamespace(namespace);
                    String localIp = getLocalIp(config.getDataSourceType());
                    if(!delegatedIp.equals(localIp)) {
                        logger.info("Ignore start database command for ip not matching. local: [{}] delegatedId: [{}]", localIp, delegatedIp);
                        try {
                            // 非指定ip延迟等待30s后竞争
                            TimeUnit.SECONDS.sleep(30);
                        } catch (InterruptedException ignored) {

                        }
                    }
                });

            }
            CompletableFuture.runAsync(() -> {

                DataSourceCfg config = dataSourceCfgOps.getByNamespace(namespace);
                doStart(config);
            });

        }

        @Override
        public void stop(Command command) {
            String namespace = command.getNamespace();
            if(!StringUtils.hasText(namespace)) {
                return;
            }
            CompletableFuture.runAsync(() -> {
                AbstractReplicationServer.this.stop(namespace);
                logger.info("[" + namespace + "] 关闭datasource监听成功");
            });

        }

        private String getLocalIp(String dataSourceType){
            return dataSourceType + ":" + NetUtils.getLocalAddress().getHostAddress();
        }
    }
}

