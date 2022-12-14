package com.github.shawven.calf.oplog.server.datasource;

import com.alibaba.fastjson.JSON;
import com.github.shawven.calf.oplog.base.Consts;
import com.github.shawven.calf.oplog.server.KeyPrefixUtil;
import com.github.shawven.calf.oplog.server.mode.Command;
import com.github.shawven.calf.oplog.server.mode.CommandType;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class NodeConfigDataSourceImpl implements NodeConfigDataSource {

    private static final Logger logger = LoggerFactory.getLogger(NodeConfigDataSourceImpl.class);

    private final DataSource dataSource;

    private final KeyPrefixUtil keyPrefixUtil;


    public NodeConfigDataSourceImpl(DataSource dataSource, KeyPrefixUtil keyPrefixUtil) {
        this.dataSource = dataSource;
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
            if(!StringUtils.hasText(config.getNamespace())) {
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
    public boolean create(NodeConfig newConfig) {
        List<NodeConfig> nodeConfigs = getAll();

        boolean exist = nodeConfigs.stream().anyMatch(c -> c.getNamespace().equals(newConfig.getNamespace()));

        if(exist) {
            return false;
        }

        nodeConfigs.add(newConfig);
        saveAll(nodeConfigs);

        return true;
    }

    @Override
    public void update(NodeConfig newConfig) {
        if(Thread.currentThread().isInterrupted()) {
            return;
        }
        List<NodeConfig> configList = getAll();

        AtomicBoolean modifyFlag = new AtomicBoolean(false);
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
            saveAll(configList);
        }
    }

    @Override
    public boolean remove(String namespace) {
        if(!StringUtils.hasText(namespace)) {
            return false;
        }

        List<NodeConfig> nodeConfigs = getAll();
        boolean removed = nodeConfigs.removeIf(config -> config.getNamespace().equals(namespace));

        saveAll(nodeConfigs);

        return removed;
    }

    @Override
    public NodeConfig getByNamespace(String namespace) {
        List<NodeConfig> nodeConfigs = getAll();
        Optional<NodeConfig> optional = nodeConfigs.stream().filter(config -> namespace.equals(config.getNamespace())).findAny();

        return optional.orElse(null);

    }
    @Override
    public List<String> getNamespaceList() {
        return getAll().stream()
                .map(NodeConfig::getNamespace)
                .collect(Collectors.toList());
    }


    @Override
    public List<NodeConfig> getAll() {
        String value = dataSource.get(keyPrefixUtil.withPrefix(Consts.NODE_CONFIG));
        return JSON.parseArray(value, NodeConfig.class);
    }

    private void saveAll(List<NodeConfig> nodeConfigs) {
        dataSource.set(keyPrefixUtil.withPrefix(Consts.NODE_CONFIG), JSON.toJSONString(nodeConfigs));
    }

    @Override
    public void registerServiceWatcher(ServiceWatcher watcher) {
        dataSource.watch(keyPrefixUtil.withPrefix(Consts.COMMAND), new Emitter<String>() {
            @Override
            public void onNext(@NonNull String value) {
                Command command = JSON.parseObject(value, Command.class);
                // 根据不同的命令类型（START/STOP）执行不同的逻辑
                if(CommandType.START_DATASOURCE.equals(command.getType())) {
                    watcher.start(command);
                } else if (CommandType.STOP_DATASOURCE.equals(command.getType())) {
                    watcher.stop(command);
                }
            }

            @Override
            public void onError(@NonNull Throwable throwable) {
                logger.error("watch command error.", throwable);
                CompletableFuture.runAsync(() ->  registerServiceWatcher(watcher));
            }

            @Override
            public void onComplete() {
                logger.info("watch command completed.");
                CompletableFuture.runAsync(() ->  registerServiceWatcher(watcher));
            }
        });
    }
}
