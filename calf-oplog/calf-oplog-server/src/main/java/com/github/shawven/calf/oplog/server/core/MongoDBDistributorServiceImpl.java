package com.github.shawven.calf.oplog.server.core;


import com.github.shawven.calf.oplog.base.OplogConstants;
import com.github.shawven.calf.oplog.server.datasource.DataSourceException;
import com.github.shawven.calf.oplog.base.ServiceStatus;
import com.github.shawven.calf.oplog.server.datasource.NodeConfig;
import com.github.shawven.calf.oplog.server.datasource.ClientDataSource;
import com.github.shawven.calf.oplog.server.datasource.NodeConfigDataSource;
import com.github.shawven.calf.oplog.server.datasource.leaderselector.LeaderSelector;
import com.github.shawven.calf.oplog.server.mode.Command;
import com.github.shawven.calf.oplog.server.publisher.DataPublisherManager;
import com.github.shawven.calf.oplog.server.KeyPrefixUtil;
import com.github.shawven.calf.oplog.server.NetUtils;
import com.github.shawven.calf.oplog.server.datasource.leaderselector.OplogLeaderSelectorListener;
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
public class MongoDBDistributorServiceImpl extends AbstractDistributorService {

    public static final String TYPE = "MongoDB";

    private OpLogClientFactory opLogClientFactory;

    private ClientDataSource clientDataSource;

    private NodeConfigDataSource nodeConfigDataSource;

    private KeyPrefixUtil keyPrefixUtil;


    @Qualifier("opLogDataPublisher")
    private DataPublisherManager dataPublisherManager;

    private ExecutorService executorService;

    private ScheduledExecutorService scheduledExecutorService;

    private Map<String, LeaderSelector> leaderSelectorMap = new ConcurrentHashMap<>();

    public MongoDBDistributorServiceImpl(OpLogClientFactory opLogClientFactory,
                                         ClientDataSource clientDataSource,
                                         NodeConfigDataSource nodeConfigDataSource,
                                         KeyPrefixUtil keyPrefixUtil,
                                         DataPublisherManager dataPublisherManager) {
        this.opLogClientFactory = opLogClientFactory;
        this.clientDataSource = clientDataSource;
        this.nodeConfigDataSource = nodeConfigDataSource;
        this.keyPrefixUtil = keyPrefixUtil;
        this.dataPublisherManager = dataPublisherManager;
    }

    @Override
    public void startDistribute() {
        // 1. 从etcd获得初始配置信息
        List<NodeConfig> configList = nodeConfigDataSource.init(TYPE);

        // 2. 竞争每个数据源的Leader
        executorService = Executors.newCachedThreadPool();
        configList.forEach(config -> {
            // 在线程中启动事件监听
            if(config.isActive()) {
                startTask(config);
            }
        });

        // 3. 注册数据源Config 命令Watcher
        nodeConfigDataSource.registerWatcher(new ServiceSwitcher() {
            @Override
            public void start(Command command) {
                String namespace = command.getNamespace();
                String delegatedIp = command.getDelegatedIp();
                if(StringUtils.isEmpty(namespace)) {
                    return;
                }

                if(!StringUtils.isEmpty(delegatedIp)) {
                    NodeConfig config = nodeConfigDataSource.getByNamespace(namespace);
                    String localIp = getLocalIp(config.getDataSourceType());
                    if(!delegatedIp.equals(localIp)) {
                        logger.info("Ignore start database command for ip not matching. local: [{}] delegatedId: [{}]", localIp, delegatedIp);
                        try {
                            // 非指定ip延迟等待30s后竞争
                            TimeUnit.SECONDS.sleep(30);
                        } catch (InterruptedException ignored) {

                        }
                    }
                }
                NodeConfig config = nodeConfigDataSource.getByNamespace(namespace);
                MongoDBDistributorServiceImpl.this.startTask(config);
            }

            @Override
            public void stop(Command command) {
                String namespace = command.getNamespace();
                if(StringUtils.isEmpty(namespace)) {
                    return;
                }
                MongoDBDistributorServiceImpl.this.stopTask(namespace);
                logger.info("[" + namespace + "] 关闭datasource监听成功");
            }
        });

        // 4. 服务节点上报
        updateServiceStatus(TYPE);
    }

    public static String getLocalIp(String dataSourceType){
        return dataSourceType + ":" + NetUtils.getLocalAddress().getHostAddress();
    }

    @Override
    public void startTask(NodeConfig config) {
        executorService.submit(() -> {
            String namespace = config.getNamespace();
            String identification = NetUtils.getLocalAddress().getHostAddress();
            String identificationPath = keyPrefixUtil.withPrefix(OplogConstants.LEADER_IDENTIFICATION_PATH);
            OplogLeaderSelectorListener listener = new OplogLeaderSelectorListener(opLogClientFactory, config, nodeConfigDataSource);
            LeaderSelector leaderSelector = new LeaderSelector(etcdClient, config.getNamespace(), 20L, identification, identificationPath, listener);
            leaderSelectorMap.put(namespace, leaderSelector);
            leaderSelector.start();
        });
    }

    @Override
    public void stopTask(String namespace) {
        LeaderSelector leaderSelector = leaderSelectorMap.get(namespace);
        if(leaderSelector != null) {
            leaderSelector.close();
        }
    }


    protected void updateServiceStatus(String dataSourceType) {
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        try {
            long leaseId = clientDataSource.getLease("Update Service Status", 20);
            scheduledExecutorService.scheduleWithFixedDelay(() -> {

                ServiceStatus serviceStatus = new ServiceStatus();
                String localIp = dataSourceType + ":" + NetUtils.getLocalAddress().getHostAddress();
                serviceStatus.setIp(localIp);
//                serviceStatus.setActiveNamespaces(opLogClientFactory.getActiveNameSpaces());
                serviceStatus.setTotalEventCount(opLogClientFactory.getEventCount());
                serviceStatus.setLatelyEventCount(opLogClientFactory.eventCountSinceLastTime());
                serviceStatus.setTotalPublishCount(dataPublisherManager.getPublishCount());
                serviceStatus.setLatelyPublishCount(dataPublisherManager.publishCountSinceLastTime());
                serviceStatus.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

                try {
                    clientDataSource.updateServiceStatus(localIp, serviceStatus, leaseId);
                    logger.info("Update Service Status: [{}]", serviceStatus.toString());
                } catch (Exception e) {
                    throw new DataSourceException("Update Service Status Error!", e);
                }
            },10, 20, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new DataSourceException("Update Service Status Error!", e);
        }
    }
}
