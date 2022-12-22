package com.github.shawven.calf.oplog.server.dao;

import com.alibaba.fastjson.JSON;
import com.github.shawven.calf.oplog.base.Const;
import com.github.shawven.calf.oplog.register.Emitter;
import com.github.shawven.calf.oplog.register.domain.DataSourceCfg;
import com.github.shawven.calf.oplog.server.KeyPrefixUtil;
import com.github.shawven.calf.oplog.server.ServerWatcher;
import com.github.shawven.calf.oplog.register.Repository;
import com.github.shawven.calf.oplog.server.domain.Command;
import com.github.shawven.calf.oplog.server.domain.CommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class DataSourceCfgDAOImpl implements DataSourceCfgDAO {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceCfgDAOImpl.class);

    private final Repository repository;

    private final KeyPrefixUtil keyPrefixUtil;


    public DataSourceCfgDAOImpl(Repository repository, KeyPrefixUtil keyPrefixUtil) {
        this.repository = repository;
        this.keyPrefixUtil = keyPrefixUtil;
    }

    @Override
    public List<DataSourceCfg> init(String type) {
        List<DataSourceCfg> dataSourceCfgs = getAll();

        if(dataSourceCfgs.isEmpty()) {
            logger.warn("There is no available datasource Configs!");
            return dataSourceCfgs;
        }
        Set<String> namespaces = new HashSet<>();
        List<DataSourceCfg> filterDataSource = new ArrayList<>();
        dataSourceCfgs.forEach(config -> {
            if(!StringUtils.hasText(config.getNamespace())) {
                throw new IllegalArgumentException("You need to config namespace!");
            }
            if(!namespaces.add(config.getNamespace())) {
                throw new IllegalArgumentException("Duplicated namespace!");
            }
            if(config.getDataSourceType().equals(type)){
                filterDataSource.add(config);
            }
        });
        return filterDataSource;
    }

    @Override
    public boolean create(DataSourceCfg newConfig) {
        List<DataSourceCfg> dataSourceCfgs = getAll();

        boolean exist = dataSourceCfgs.stream().anyMatch(c -> c.getNamespace().equals(newConfig.getNamespace()));
        if(exist) {
            return false;
        }
        if (dataSourceCfgs.isEmpty()) {
            dataSourceCfgs = Collections.singletonList(newConfig);
        } else {
            dataSourceCfgs.add(newConfig);
        }

        saveAll(dataSourceCfgs);
        return true;
    }

    @Override
    public void update(DataSourceCfg newConfig) {
        if(Thread.currentThread().isInterrupted()) {
            return;
        }
        List<DataSourceCfg> configList = getAll();

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

        List<DataSourceCfg> dataSourceCfgs = getAll();
        boolean removed = dataSourceCfgs.removeIf(config -> config.getNamespace().equals(namespace));

        saveAll(dataSourceCfgs);

        return removed;
    }

    @Override
    public DataSourceCfg getByNamespace(String namespace) {
        List<DataSourceCfg> dataSourceCfgs = getAll();
        Optional<DataSourceCfg> optional = dataSourceCfgs.stream().filter(config -> namespace.equals(config.getNamespace())).findAny();

        return optional.orElse(null);

    }
    @Override
    public List<String> getNamespaceList() {
        return getAll().stream()
                .map(DataSourceCfg::getNamespace)
                .collect(Collectors.toList());
    }


    @Override
    public List<DataSourceCfg> getAll() {
        String value = repository.get(keyPrefixUtil.withPrefix(Const.NODE_CONFIG));
        if (value == null) {
            return Collections.emptyList();
        }
        return JSON.parseArray(value, DataSourceCfg.class);
    }

    private void saveAll(List<DataSourceCfg> dataSourceCfgs) {
        repository.set(keyPrefixUtil.withPrefix(Const.NODE_CONFIG), JSON.toJSONString(dataSourceCfgs));
    }

    @Override
    public void registerServerWatcher(ServerWatcher watcher) {
        repository.watch(keyPrefixUtil.withPrefix(Const.COMMAND), new Emitter<String>() {
            @Override
            public void onNext(String value) {
                Command command = JSON.parseObject(value, Command.class);
                // 根据不同的命令类型（START/STOP）执行不同的逻辑
                if(CommandType.START_DATASOURCE.equals(command.getType())) {
                    watcher.start(command);
                } else if (CommandType.STOP_DATASOURCE.equals(command.getType())) {
                    watcher.stop(command);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error("watch command error.", throwable);
                CompletableFuture.runAsync(() ->  registerServerWatcher(watcher));
            }

            @Override
            public void onComplete() {
                logger.info("watch command completed.");
                CompletableFuture.runAsync(() ->  registerServerWatcher(watcher));
            }
        });
    }
}
