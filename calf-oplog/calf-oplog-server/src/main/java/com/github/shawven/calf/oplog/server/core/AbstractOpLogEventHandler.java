package com.github.shawven.calf.oplog.server.core;

import com.github.shawven.calf.oplog.base.EventBaseDTO;
import com.github.shawven.calf.oplog.server.datasource.ClientInfo;
import com.github.shawven.calf.oplog.server.publisher.DataPublisherManager;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: kl @kailing.pub
 * @date: 2020/1/7
 */
@AllArgsConstructor
public abstract class AbstractOpLogEventHandler {

    private final Logger logger = LoggerFactory.getLogger(AbstractOpLogEventHandler.class);

    protected String namespace;

    protected DataPublisherManager dataPublisherManager;

    protected Map<String, Set<ClientInfo>> clientInfoMap;

    public String getDataBase(Document event){
        String dataBaseTable = event.getString(OpLogClientFactory.DATABASE_KEY);
        return dataBaseTable.split("\\.")[0];
    }

    public String getTable(Document event){
        String dataBaseTable = event.getString(OpLogClientFactory.DATABASE_KEY);
        return dataBaseTable.split("\\.")[1];
    }

    public String getNamespace() {
        return namespace;
    }

    /**
     * 处理event
     *
     * @param event
     */
    public void handle(Document event) {
        Set<ClientInfo> clientInfos = filter(event);
        if (!CollectionUtils.isEmpty(clientInfos)) {
            try {
                dataPublisherManager.publish(clientInfos, formatData(event));
            } catch (Exception e) {
                logger.error("dataPublisherManager.publish error: " + e.getMessage(), e);
            }
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
     * 筛选出关注某事件的应用列表
     * @param event
     * @return
     */
    protected Set<ClientInfo> filter(Document event) {
        String database = getDataBase(event);
        String table = getTable(event);
        String tableKey = database.concat("/").concat(table);
        return clientInfoMap.get(tableKey);
    }
}
