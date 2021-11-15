package com.github.shawven.calf.oplog.server.core;


import com.github.shawven.calf.base.Constants;
import com.github.shawven.calf.base.DataSourceException;
import com.github.shawven.calf.base.ServiceStatus;
import com.github.shawven.calf.extension.NodeConfig;
import com.github.shawven.calf.extension.ClientDataSource;
import com.github.shawven.calf.extension.NodeConfigDataSource;
import com.github.shawven.calf.extension.LeaderSelector;
import com.github.shawven.calf.oplog.server.DataPublisher;
import com.github.shawven.calf.oplog.server.KeyPrefixUtil;
import com.github.shawven.calf.oplog.server.NetUtils;
import com.github.shawven.calf.oplog.server.core.leaderselector.OplogLeaderSelectorListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author: kl @kailing.pub
 * @date: 2020/1/7
 */
@Service
public class MongoDBDistributorServiceImpl extends AbstractDistributorService {

    public static final String TYPE = "MongoDB";

    @Autowired
    private OpLogClientFactory opLogClientFactory;

    @Autowired
    private ClientDataSource clientDataSource;
    @Autowired
    private NodeConfigDataSource nodeConfigDataSource;
    @Autowired
    private KeyPrefixUtil keyPrefixUtil;


    @Qualifier("opLogDataPublisher")
    @Autowired
    private DataPublisher dataPublisher;

    private ExecutorService executorService;

    private ScheduledExecutorService scheduledExecutorService;

    private Map<String, LeaderSelector> leaderSelectorMap = new ConcurrentHashMap<>();

    public MongoDBDistributorServiceImpl() {
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
                submitBinLogDistributeTask(config);
            }
        });

        // 3. 注册数据源Config 命令Watcher
        nodeConfigDataSource.registerConfigCommandWatcher();

        // 4. 服务节点上报
        updateServiceStatus(TYPE);
    }

    @Override
    public void submitBinLogDistributeTask(NodeConfig config) {
        executorService.submit(() -> binLogDistributeTask(config));
    }


    private void binLogDistributeTask(NodeConfig nodeConfig) {
        String namespace = nodeConfig.getNamespace();
        String identification = NetUtils.getLocalAddress().getHostAddress();
        String identificationPath = keyPrefixUtil.withPrefix(Constants.LEADER_IDENTIFICATION_PATH);
        OplogLeaderSelectorListener listener = new OplogLeaderSelectorListener(opLogClientFactory, nodeConfig, nodeConfigDataSource);
        LeaderSelector leaderSelector = null;
//        LeaderSelector leaderSelector = new LeaderSelector(etcdClient, binaryLogConfig.getNamespace(), 20L, identification, identificationPath, listener);
        leaderSelectorMap.put(namespace, leaderSelector);
        leaderSelector.start();
    }

    @Override
    public void stopBinLogDistributeTask(String namespace) {
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
                serviceStatus.setTotalPublishCount(dataPublisher.getPublishCount());
                serviceStatus.setLatelyPublishCount(dataPublisher.publishCountSinceLastTime());
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
