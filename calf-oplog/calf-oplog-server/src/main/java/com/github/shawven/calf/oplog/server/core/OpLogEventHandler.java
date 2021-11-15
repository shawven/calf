package com.github.shawven.calf.oplog.server.core;

import com.github.shawven.calf.base.ClientInfo;
import org.bson.Document;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: kl @kailing.pub
 * @date: 2020/1/7
 */
public abstract class OpLogEventHandler {

    protected OpLogEventContext context;

    protected final Map<String, Set<ClientInfo>> clientInfoMap = new ConcurrentHashMap<>();

    public OpLogEventHandler(OpLogEventContext context) {
        this.context = context;
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
            DataPublisher dataPublisher = context.getDataPublisher();
            dataPublisher.publish(clientInfos, data);
        }
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
