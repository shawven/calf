package com.github.shawven.calf.oplog.server.dao;

import com.alibaba.fastjson.JSON;
import com.github.shawven.calf.oplog.base.Const;
import com.github.shawven.calf.oplog.register.Emitter;
import com.github.shawven.calf.oplog.register.domain.ClientInfo;
import com.github.shawven.calf.oplog.register.domain.DataSourceCfg;
import com.github.shawven.calf.oplog.server.KeyPrefixUtil;
import com.github.shawven.calf.oplog.register.Repository;
import com.github.shawven.calf.oplog.server.domain.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author wanglaomo
 * @since 2019/8/6
 **/
public class ClientDAOImpl implements ClientDAO {

    private static final Logger logger = LoggerFactory.getLogger(ClientDAOImpl.class);

    private final Repository repository;

    private final KeyPrefixUtil keyPrefixUtil;

    private final DataSourceCfgDAO dataSourceCfgDAO;

    public ClientDAOImpl(Repository repository, KeyPrefixUtil keyPrefixUtil, DataSourceCfgDAO dataSourceCfgDAO) {
        this.repository = repository;
        this.keyPrefixUtil = keyPrefixUtil;
        this.dataSourceCfgDAO = dataSourceCfgDAO;
    }

    @Override
    public List<ClientInfo> listConsumerClient(DataSourceCfg dataSourceCfg) {
        String namespace = dataSourceCfg.getNamespace();
        String binLogClientSetKey = dataSourceCfg.getClientSetKey();
        String binLogClientSetStr = repository.get(joinKey(namespace, binLogClientSetKey));

        if(!StringUtils.hasText(binLogClientSetStr)) {
            return new ArrayList<>();
        }
        return JSON.parseArray(binLogClientSetStr, ClientInfo.class);
    }

    @Override
    public List<ClientInfo> listConsumerClient(String queryType) {
        return dataSourceCfgDAO.getAll().stream()
                .map(config -> repository.get(joinKey(config.getNamespace(), config.getClientSetKey())))
                .map(str -> {
                    List<ClientInfo> clientInfos = JSON.parseArray(str, ClientInfo.class);
                    return clientInfos == null ? new ArrayList<ClientInfo>() : clientInfos;
                })
                .flatMap(List::stream)
                .filter(clientInfo -> queryType == null || queryType.equals(clientInfo.getQueueType()))
                .sorted(Comparator.comparing(ClientInfo::getNamespace))
                .collect(Collectors.toList());
    }

    @Override
    public List<ClientInfo> listConsumerClientsByKey(String clientInfoKey) {
        List<ClientInfo> clientInfos = listConsumerClient((String) null);
        if(clientInfos == null || clientInfos.isEmpty()) {
            return clientInfos;
        }

        return clientInfos
                .stream()
                .filter(clientInfo -> clientInfo.getKey().equals(clientInfoKey))
                .collect(Collectors.toList());
    }

    @Override
    public void addConsumerClient(ClientInfo clientInfo) {
        String namespace = clientInfo.getNamespace();
        DataSourceCfg config = dataSourceCfgDAO.getByNamespace(namespace);
        if (config == null) {
            throw new RuntimeException("not exist namespace: " + namespace);
        }
        String metaData = repository.get(joinKey(namespace, config.getClientSetKey()));

        Set<ClientInfo> clientSet = null;
        if(!StringUtils.hasText(metaData)) {
            clientSet = new HashSet<>();
        } else {
            List<ClientInfo> clientList = JSON.parseArray(metaData, ClientInfo.class);
            clientSet = new HashSet<>(clientList);
        }
        clientSet.add(clientInfo);
        repository.set(joinKey(namespace, config.getClientSetKey()), JSON.toJSONString(clientSet));
    }

    @Override
    public void removeConsumerClient(List<ClientInfo> clientInfos) {
        Map<String, List<ClientInfo>> clientMap = clientInfos.stream().collect(Collectors.groupingBy(ClientInfo::getNamespace));
        clientMap.forEach((namespace, clientList) -> {

            DataSourceCfg config = dataSourceCfgDAO.getByNamespace(namespace);
            String metaData = repository.get(joinKey(namespace, config.getClientSetKey()));
            if(StringUtils.hasText(metaData)) {
                List<ClientInfo> currentList = JSON.parseArray(metaData, ClientInfo.class);
                Set<ClientInfo> clientSet = new HashSet<>(currentList);
                clientList.forEach(clientSet::remove);
                repository.set(joinKey(namespace, config.getClientSetKey()), JSON.toJSONString(clientSet));
            }
        });
    }

    @Override
    public boolean sendCommand(Command command) {
        repository.set(keyPrefixUtil.withPrefix(Const.COMMAND), JSON.toJSONString(command));
        return true;
    }


    @Override
    public void watcherClientInfo(DataSourceCfg dataSourceCfg, Consumer<List<ClientInfo>> consumer) {
        repository.watch(joinKey(dataSourceCfg.getNamespace(), dataSourceCfg.getClientSetKey()), new Emitter<String>() {
            @Override
            public void onNext(String value) {
                List<ClientInfo> clientInfos = JSON.parseArray(value, ClientInfo.class);
                consumer.accept(clientInfos);
            }

            @Override
            public void onError(Throwable e) {
                logger.error("watcherClientInfos.onError: " + e.getMessage(), e);
            }

            @Override
            public void onComplete() {
                logger.info("watcherClientInfos.onCompleted");
            }
        });
    }

    private String joinKey(String namespace, String key) {
        return keyPrefixUtil.withPrefix(namespace) + "/" + key;
    }
}
