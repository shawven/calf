package com.github.shawven.calf.oplog.server.datasource.etcd;

import com.alibaba.fastjson.JSON;
import com.github.shawven.calf.oplog.server.mode.Command;
import com.github.shawven.calf.oplog.server.mode.CommandType;
import com.github.shawven.calf.base.Constants;
import com.github.shawven.calf.oplog.server.datasource.NodeConfigDataSource;
import com.github.shawven.calf.oplog.server.datasource.DataSourceException;
import com.github.shawven.calf.oplog.server.datasource.NodeConfig;
import com.github.shawven.calf.oplog.server.KeyPrefixUtil;
import com.github.shawven.calf.oplog.server.NetUtils;
import com.github.shawven.calf.oplog.server.core.DistributorService;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author wanglaomo
 * @since 2019/6/4
 **/
public class EtcdNodeConfigDataSource implements NodeConfigDataSource, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(EtcdNodeConfigDataSource.class);

    private final Client etcdClient;

    private final KeyPrefixUtil keyPrefixUtil;

    private ApplicationContext applicationContext;

    public EtcdNodeConfigDataSource(Client etcdClient, KeyPrefixUtil keyPrefixUtil) {
        this.etcdClient = etcdClient;
        this.keyPrefixUtil = keyPrefixUtil;
    }

    @Override
    public List<NodeConfig> init(String dataSourceType) {
        List<NodeConfig> nodeConfigs = getAll();

        if(nodeConfigs.isEmpty()) {
            logger.warn("There is no available binlog config!");
            return nodeConfigs;
        }
        Set<String> namespaces = new HashSet<>();
        List<NodeConfig> filterDataSource = new ArrayList<>();
        nodeConfigs.forEach(config -> {
            if(StringUtils.isEmpty(config.getNamespace())) {
                throw new IllegalArgumentException("You need to config namespace!");
            }
            if(!namespaces.add(config.getNamespace())) {
                throw new IllegalArgumentException("Duplicated namespace!");
            }
            if(config.getDataSourceType().equals(dataSourceType)){
                filterDataSource.add(config);
            }
        });
       return filterDataSource;
    }

    @Override
    public List<NodeConfig> getAll() {
        KV kvClient = etcdClient.getKVClient();
        List<NodeConfig> NodeConfigs = new ArrayList<>();
        try {
            GetResponse configRes = kvClient.get(ByteSequence.from(keyPrefixUtil.withPrefix(Constants.DEFAULT_BINLOG_CONFIG_KEY), StandardCharsets.UTF_8)).get();

            if(configRes == null || configRes.getCount() == 0) {
                return NodeConfigs;
            }
            // not range query
            String configListStr = configRes.getKvs().get(0).getValue().toString(StandardCharsets.UTF_8);
            NodeConfigs = JSON.parseArray(configListStr, NodeConfig.class);
        } catch (InterruptedException | ExecutionException e) {
            throw new DataSourceException("Failed to connect to etcd server.", e);
        }
        return NodeConfigs;
    }

    @Override
    public boolean create(NodeConfig newConfig) {
        List<NodeConfig> nodeConfigs = getAll();

        boolean exist = nodeConfigs.stream().anyMatch(c -> c.getNamespace().equals(newConfig.getNamespace()));

        if(exist) {
            return false;
        }

        nodeConfigs.add(newConfig);
        persistConfig(nodeConfigs);

        return true;
    }

    @Override
    public void update(NodeConfig newConfig) {
        if(Thread.currentThread().isInterrupted()) {
            return;
        }

        AtomicBoolean modifyFlag = new AtomicBoolean(false);
        List<NodeConfig> configList = getAll();
        configList = configList.stream().map((c) -> {
            if(newConfig.getNamespace().equalsIgnoreCase(c.getNamespace())) {

                // 版本号小于集群中版本号则忽略
                if(newConfig.getVersion() < c.getVersion()) {
                    logger.warn("Ignore BinLogConfig[{}] Modify case local version [{}] < current version [{}]", newConfig.getNamespace(), newConfig.getVersion(), c.getVersion());
                    return c;
                } else {
                    modifyFlag.set(true);

                    return newConfig;
                }
            }
            return c;
        }).collect(Collectors.toList());

        if(modifyFlag.get()) {
            persistConfig(configList);
        }
    }

    @Override
    public NodeConfig remove(String namespace) {
        if(StringUtils.isEmpty(namespace)) {
            return null;
        }

        NodeConfig removedConfig = null;

        List<NodeConfig> NodeConfigs = getAll();
        Iterator<NodeConfig> iterator = NodeConfigs.iterator();
        while (iterator.hasNext()){
            NodeConfig config = iterator.next();
            if(config.getNamespace().equals(namespace)) {
                removedConfig = config;
                iterator.remove();
                break;
            }
        }

        persistConfig(NodeConfigs);

        return removedConfig;
    }

    @Override
    public NodeConfig getByNamespace(String namespace) {
        List<NodeConfig> nodeConfigs = getAll();
        Optional<NodeConfig> optional = nodeConfigs.stream().filter(config -> namespace.equals(config.getNamespace())).findAny();

        if (optional.isPresent()) {
            return optional.get();
        }

        return null;
    }


    @Override
    public List<String> getNamespaceList() {
        return getAll().stream()
                .map(NodeConfig::getNamespace)
                .collect(Collectors.toList());
    }

    /**
     * 真正开启数据源的逻辑
     *
     * @param namespace
     * @param delegatedIp
     * @return
     */
    @Override
    public void start(String namespace, String delegatedIp) {
        if(StringUtils.isEmpty(namespace)) {
            return;
        }

        if(!StringUtils.isEmpty(delegatedIp)) {
            NodeConfig config = this.getByNamespace(namespace);
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
        NodeConfig config = getByNamespace(namespace);
        getDistributorService(config).submitBinLogDistributeTask(config);
    }

    /**
     * 真正关闭数据源的逻辑
     *
     * @param namespace
     * @return
     */
    @Override
    public void stop(String namespace) {
        if(StringUtils.isEmpty(namespace)) {
            return;
        }
        NodeConfig config = getByNamespace(namespace);
        getDistributorService(config).stopBinLogDistributeTask(namespace);
        logger.info("[" + namespace + "] 关闭datasource监听成功");
    }

    @Override
    public void registerWatcher() {
        Watch watchClient = etcdClient.getWatchClient();
        watchClient.watch(
                ByteSequence.from(keyPrefixUtil.withPrefix(Constants.DEFAULT_BINLOG_CONFIG_COMMAND_KEY), StandardCharsets.UTF_8),
                WatchOption.newBuilder().withPrevKV(true).withNoDelete(true).build(),
                new Watch.Listener() {

                    @Override
                    public void onNext(WatchResponse response) {

                        List<WatchEvent> eventList = response.getEvents();
                        for(WatchEvent event: eventList) {

                            if (WatchEvent.EventType.PUT.equals(event.getEventType())) {
                                Command command = JSON.parseObject(event.getKeyValue().getValue().toString(StandardCharsets.UTF_8), Command.class);

                                // 根据不同的命令类型（START/STOP）执行不同的逻辑
                                if(CommandType.START_DATASOURCE.equals(command.getType())) {
                                    start(command.getNamespace(), command.getDelegatedIp());
                                } else if (CommandType.STOP_DATASOURCE.equals(command.getType())) {
                                    stop(command.getNamespace());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        logger.error("Watch binlog config command error.", throwable);
                        new Thread(() -> registerWatcher()).start();
                    }

                    @Override
                    public void onCompleted() {
                        logger.info("Watch binlog config command completed.");
                        new Thread(() -> registerWatcher()).start();
                    }
                }
        );
    }


    private void persistConfig(List<NodeConfig> NodeConfigs) {
        KV kvClient = etcdClient.getKVClient();
        try {
            kvClient.put(
                    ByteSequence.from(keyPrefixUtil.withPrefix(Constants.DEFAULT_BINLOG_CONFIG_KEY), StandardCharsets.UTF_8),
                    ByteSequence.from(JSON.toJSONString(NodeConfigs), StandardCharsets.UTF_8)
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new DataSourceException("Failed to connect to etcd server.", e);
        }
    }

    private DistributorService getDistributorService(NodeConfig config) {
        return applicationContext.getBean(config.getDataSourceType(), DistributorService.class);
    }

    public static String getLocalIp(String dataSourceType){
        return dataSourceType + ":" + NetUtils.getLocalAddress().getHostAddress();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
