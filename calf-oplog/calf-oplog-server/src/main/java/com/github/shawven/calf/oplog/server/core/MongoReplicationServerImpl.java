package com.github.shawven.calf.oplog.server.core;


import com.github.shawven.calf.oplog.base.Const;
import com.github.shawven.calf.oplog.register.ElectionFactory;
import com.github.shawven.calf.oplog.server.ServerWatcher;
import com.github.shawven.calf.oplog.server.dao.ClientDAO;
import com.github.shawven.calf.oplog.server.dao.DataSourceCfgDAO;
import com.github.shawven.calf.oplog.server.dao.StatusDAO;
import com.github.shawven.calf.oplog.register.domain.DataSourceCfg;
import com.github.shawven.calf.oplog.register.domain.InstanceStatus;
import com.github.shawven.calf.oplog.register.election.Election;
import com.github.shawven.calf.oplog.server.domain.Command;
import com.github.shawven.calf.oplog.server.publisher.DataPublisherManager;
import com.github.shawven.calf.oplog.server.KeyPrefixUtil;
import com.github.shawven.calf.oplog.server.NetUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author: kl @kailing.pub
 * @date: 2020/1/7
 */
public class MongoReplicationServerImpl extends AbstractReplicationServer {

    public static final String TYPE = "MongoDB";

    private OpLogClientFactory opLogClientFactory;

    private ClientDAO clientDAO;

    private DataSourceCfgDAO dataSourceCfgDAO;

    private KeyPrefixUtil keyPrefixUtil;


    @Qualifier("opLogDataPublisher")
    private DataPublisherManager dataPublisherManager;

    private ExecutorService executorService;

    private ScheduledExecutorService scheduledExecutorService;

    private ElectionFactory electionFactory;

    private Map<String, Election> electionMap = new ConcurrentHashMap<>();

    public MongoReplicationServerImpl(OpLogClientFactory opLogClientFactory, ElectionFactory electionFactory,
                                      DataSourceCfgDAO dataSourceCfgDAO, ClientDAO clientDAO, StatusDAO statusDAO,
                                      KeyPrefixUtil keyPrefixUtil, DataPublisherManager dataPublisherManager) {
        super(dataSourceCfgDAO, clientDAO, statusDAO);
        this.opLogClientFactory = opLogClientFactory;
        this.electionFactory = electionFactory;
        this.dataSourceCfgDAO = dataSourceCfgDAO;
        this.keyPrefixUtil = keyPrefixUtil;
        this.clientDAO = clientDAO;
        this.dataPublisherManager = dataPublisherManager;
    }

    @Override
    public void start() {
        // 1. 获得初始配置信息
        List<DataSourceCfg> configList = dataSourceCfgDAO.init(TYPE);

        // 2. 竞争每个数据源的Leader
        executorService = Executors.newCachedThreadPool();
        configList.forEach(config -> {
            // 在线程中启动事件监听
            if(config.isActive()) {
                startTask(config);
            }
        });

        // 3. 注册数据源Config 命令Watcher
        dataSourceCfgDAO.registerServerWatcher(serverWatcher);

        // 4. 服务节点上报
        updateInstanceStatus();
    }

    public static String getLocalIp(String dataSourceType){
        return dataSourceType + ":" + NetUtils.getLocalAddress().getHostAddress();
    }

    @Override
    public void startTask(DataSourceCfg dataSourceCfg) {
        executorService.submit(() -> {
            String path = keyPrefixUtil.withPrefix(Const.LEADER_PATH);
            String namespace = dataSourceCfg.getNamespace();
            String uniqueId = NetUtils.getLocalAddress().getHostAddress();

            OplogTaskListener listener = new OplogTaskListener(dataSourceCfg, opLogClientFactory,
                    clientDAO, statusDAO, dataSourceCfgDAO, dataPublisherManager);

            Election election = electionFactory.getElection(path, namespace, uniqueId, 20L, listener);

            electionMap.put(namespace, election);
            election.start();
        });
    }

    @Override
    public void stopTask(String namespace) {
        Election election = electionMap.get(namespace);
        if(election != null) {
            election.close();
        }
    }

    protected void updateInstanceStatus() {
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            InstanceStatus instanceStatus = new InstanceStatus();
            String localIp = TYPE + ":" + NetUtils.getLocalAddress().getHostAddress();
            instanceStatus.setIp(localIp);
//                serviceStatus.setActiveNamespaces(opLogClientFactory.getActiveNameSpaces());
            instanceStatus.setTotalEventCount(opLogClientFactory.getEventCount());
            instanceStatus.setLatelyEventCount(opLogClientFactory.eventCountSinceLastTime());
            instanceStatus.setTotalPublishCount(dataPublisherManager.getPublishCount());
            instanceStatus.setLatelyPublishCount(dataPublisherManager.publishCountSinceLastTime());
            instanceStatus.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            statusDAO.updateInstanceStatus(localIp, instanceStatus);
            logger.info("updateInstanceStatus: [{}]", instanceStatus);
        },10, 20, TimeUnit.SECONDS);
    }

    ServerWatcher serverWatcher = new ServerWatcher() {
        @Override
        public void start(Command command) {
            String namespace = command.getNamespace();
            String delegatedIp = command.getDelegatedIp();
            if(!StringUtils.hasText(namespace)) {
                return;
            }

            if(StringUtils.hasText(delegatedIp)) {
                CompletableFuture.runAsync(() -> {
                    DataSourceCfg config = dataSourceCfgDAO.getByNamespace(namespace);
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

                DataSourceCfg config = dataSourceCfgDAO.getByNamespace(namespace);
                MongoReplicationServerImpl.this.startTask(config);
            });

        }

        @Override
        public void stop(Command command) {
            String namespace = command.getNamespace();
            if(!StringUtils.hasText(namespace)) {
                return;
            }
            CompletableFuture.runAsync(() -> {
                MongoReplicationServerImpl.this.stopTask(namespace);
                logger.info("[" + namespace + "] 关闭datasource监听成功");
            });

        }
    };
}
