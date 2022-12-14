package com.github.shawven.calf.oplog.server.datasource;

import com.alibaba.fastjson.JSON;
import com.github.shawven.calf.oplog.base.Consts;
import com.github.shawven.calf.oplog.base.ServiceStatus;
import com.github.shawven.calf.oplog.server.KeyPrefixUtil;
import com.github.shawven.calf.oplog.server.mode.Command;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author wanglaomo
 * @since 2019/8/6
 **/
public class ClientDataSourceImpl implements ClientDataSource {


    private static final Logger logger = LoggerFactory.getLogger(ClientDataSourceImpl.class);

    private DataSource dataSource;

    private KeyPrefixUtil keyPrefixUtil;

    private NodeConfigDataSource nodeConfigDataSource;

    public ClientDataSourceImpl(DataSource dataSource, KeyPrefixUtil keyPrefixUtil, NodeConfigDataSource nodeConfigDataSource) {
        this.dataSource = dataSource;
        this.keyPrefixUtil = keyPrefixUtil;
        this.nodeConfigDataSource = nodeConfigDataSource;
    }

    @Override
    public List<ClientInfo> listConsumerClient(NodeConfig nodeConfig) {
        String namespace = nodeConfig.getNamespace();
        String binLogClientSetKey = nodeConfig.getClientSetKey();
        String binLogClientSetStr = getMetaData(namespace, binLogClientSetKey);

        if(!StringUtils.hasText(binLogClientSetStr)) {
            return new ArrayList<>();
        }
        return JSON.parseArray(binLogClientSetStr, ClientInfo.class);
    }

    @Override
    public List<ClientInfo> listConsumerClient(String queryType) {
        return nodeConfigDataSource.getAll().stream()
                .map(config -> getMetaData(config.getNamespace(), config.getClientSetKey()))
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
        NodeConfig config = nodeConfigDataSource.getByNamespace(namespace);

        String metaData = getMetaData(namespace, config.getClientSetKey());

        Set<ClientInfo> clientSet = null;
        if(!StringUtils.hasText(metaData)) {
            clientSet = new HashSet<>();
        } else {
            List<ClientInfo> clientList = JSON.parseArray(metaData, ClientInfo.class);
            clientSet = new HashSet<>(clientList);
        }
        clientSet.add(clientInfo);
        setMetaData(namespace, config.getClientSetKey(), JSON.toJSONString(clientSet));

    }

    @Override
    public void removeConsumerClient(List<ClientInfo> clientInfos) {
        Map<String, List<ClientInfo>> clientMap = clientInfos.stream().collect(Collectors.groupingBy(ClientInfo::getNamespace));
        clientMap.forEach((namespace, clientList) -> {

            NodeConfig config = nodeConfigDataSource.getByNamespace(namespace);
            String metaData = getMetaData(namespace, config.getClientSetKey());
            if(StringUtils.hasText(metaData)) {
                List<ClientInfo> currentList = JSON.parseArray(metaData, ClientInfo.class);
                Set<ClientInfo> clientSet = new HashSet<>(currentList);
                clientList.forEach(clientSet::remove);
                setMetaData(namespace, config.getClientSetKey(), JSON.toJSONString(clientSet));
            }
        });
    }

    @Override
    public boolean sendCommand(Command command) {
        dataSource.set(keyPrefixUtil.withPrefix(Consts.COMMAND), JSON.toJSONString(command));
        return true;
    }

    @Override
    public void updateNodeStatus(String filename, long position, NodeConfig config) {
        long now = System.currentTimeMillis();
        String dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(now), ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_DATE_TIME);

        String namespace = config.getNamespace();
        String statusKey = config.getStatusKey();

        String metaData = getMetaData(namespace, statusKey);
        Map<String, Object> status;
        if(!StringUtils.hasText(metaData)) {
            status = new HashMap<>();
            status.put("namespace", config.getNamespace());
        } else {
            status = JSON.parseObject(metaData).toJavaObject(Map.class);
        }

        if(StringUtils.hasText(filename)) {
            status.put("filename", filename);
        }

        status.put("position", position);
        status.put("timestamp", now);
        status.put("datetime", dateTime);
        setMetaData(namespace, statusKey, JSON.toJSONString(status));
    }

    @Override
    public List<Map<String, Object>> listStatus() {
        return nodeConfigDataSource.getAll().stream()
                .map(config -> {
                    String metaData = getMetaData(config.getNamespace(), config.getStatusKey());
                    if (metaData == null) {
                        return Collections.<String, Object>emptyMap();
                    }
                    // metaData shouldn't be null
                    return (Map<String, Object>)JSON.parseObject(metaData).toJavaObject(Map.class);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getNodeStatus(NodeConfig nodeConfig) {
        String namespace = nodeConfig.getNamespace();
        String statusKey = nodeConfig.getStatusKey();

        String metaData = getMetaData(namespace, statusKey);
        if(!StringUtils.hasText(metaData)) {
            return null;
        }
        return JSON.parseObject(metaData);
    }


    @Override
    public void updateServiceStatus(String serviceKey, ServiceStatus status) {
        dataSource.set(
                keyPrefixUtil.withPrefix(Consts.SERVICE_STATUS_PATH).concat(serviceKey),
                JSON.toJSONString(status),
                20
        );
    }

    /**
     * @return
     */
    @Override
    public List<ServiceStatus> getServiceStatus() {
        List<String> strings = dataSource.list(keyPrefixUtil.withPrefix(Consts.SERVICE_STATUS_PATH));
        return strings.stream()
                .map(str -> JSON.parseObject(str, ServiceStatus.class))
                .collect(Collectors.toList());
    }

    @Override
    public void watcherClientInfo(NodeConfig nodeConfig, Consumer<List<ClientInfo>> consumer) {
        dataSource.watch(getKey(nodeConfig.getNamespace(), nodeConfig.getClientSetKey()), new Emitter<String>() {
            @Override
            public void onNext(@NonNull String value) {
                List<ClientInfo> clientInfos = JSON.parseArray(value, ClientInfo.class);
                consumer.accept(clientInfos);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                logger.error("watcherClientInfos.onError: " + e.getMessage(), e);
            }

            @Override
            public void onComplete() {
                logger.info("watcherClientInfos.onCompleted");
            }
        });
    }

    private String getMetaData(String namespace, String key) {
        return dataSource.get(getKey(namespace, key));
    }

    private void setMetaData(String namespace, String key, String metaData) {
        dataSource.set(getKey(namespace, key), metaData);
    }

    private String getKey(String namespace, String key) {
        return keyPrefixUtil.withPrefix(namespace) + "/" + key;
    }
}
