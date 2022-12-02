package com.github.shawven.calf.oplog.server.core;

import com.github.shawven.calf.oplog.base.EventBaseDTO;
import com.github.shawven.calf.oplog.server.datasource.ClientInfo;
import com.github.shawven.calf.oplog.server.publisher.DataPublisherManager;
import org.bson.Document;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author: kl @kailing.pub
 * @date: 2020/1/7
 */
public abstract class OpLogEventHandler {

    protected OpLogEventContext context;

    protected final Map<String, Set<ClientInfo>> clientInfoMap = new ConcurrentHashMap<>();

    public OpLogEventHandler(OpLogEventContext context) {
        this.context = context;
        updateClientBatch(context.getClients());
    }

    public String getDataBase(Document event){
        String dataBaseTable = event.getString(OpLogClientFactory.DATABASE_KEY);
        return dataBaseTable.split("\\.")[0];
    }

    public String getTable(Document event){
        String dataBaseTable = event.getString(OpLogClientFactory.DATABASE_KEY);
        return dataBaseTable.split("\\.")[1];
    }

    /**
     * 处理event
     *
     * @param event
     */
    public void handle(Document event) {
        Set<ClientInfo> clientInfos = filter(event);
        if (!CollectionUtils.isEmpty(clientInfos)) {
            publish(formatData(event), clientInfos);
            updateOpLogStatus(event);
        }
    }

    /**
     * 格式化参数格式
     *
     * @param event
     * @return 格式化后的string
     */
    protected EventBaseDTO formatData(Document event) {
        return null;
    }

    /**
     * 发布信息
     *
     * @param data
     */
    protected void publish(EventBaseDTO data, Set<ClientInfo> clientInfos) {
        if (data != null) {
            DataPublisherManager dataPublisherManager = context.getDataPublisherManager();
            dataPublisherManager.publish(clientInfos, data);
        }
    }

    /**
     * 添加客户端关注的表
     *
     * @param client
     */
    public void addClient(ClientInfo client) {
        String key = getClientInfoMapKey(client);
        Set<ClientInfo> set = clientInfoMap.computeIfAbsent(key, k -> new HashSet<>());
        set.add(client);
    }

    /**
     * 删除客户端关注的表
     *
     * @param clientInfo
     */
    public void deleteClient(ClientInfo clientInfo){
        String key = getClientInfoMapKey(clientInfo);
        Set<ClientInfo> clientInfos = clientInfoMap.get(key);
        clientInfos.remove(clientInfo);
        clientInfoMap.put(key, clientInfos);
    }

    public void updateClientBatch(List<ClientInfo> clients) {
        clients.stream()
                .collect(Collectors.groupingBy(this::getClientInfoMapKey))
                .forEach((mapKey, clientInfoList) -> clientInfoMap.put(mapKey, new HashSet<>(clientInfoList)));
    }

    private String getClientInfoMapKey(ClientInfo clientInfo) {
        return clientInfo.getDatabaseName().concat("/").concat(clientInfo.getTableName());
    }

    /**
     * 更新日志位置
     *
     * @param document
     */
    protected void updateOpLogStatus(Document document) {

    }


    /**
     * 筛选出关注某事件的应用列表
     * @param event
     * @return
     */
    protected Set<ClientInfo> filter(Document event) {
        return null;
    }
}
