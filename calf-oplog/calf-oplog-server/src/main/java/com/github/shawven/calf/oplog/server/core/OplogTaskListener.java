package com.github.shawven.calf.oplog.server.core;

import com.github.shawven.calf.oplog.base.DatabaseEvent;
import com.github.shawven.calf.oplog.base.EventBaseDTO;
import com.github.shawven.calf.oplog.server.DocumentUtils;
import com.github.shawven.calf.oplog.server.datasource.ClientDataSource;
import com.github.shawven.calf.oplog.server.datasource.ClientInfo;
import com.github.shawven.calf.oplog.server.datasource.NodeConfig;
import com.github.shawven.calf.oplog.server.datasource.NodeConfigDataSource;
import com.github.shawven.calf.oplog.server.datasource.leaderselector.TaskListener;
import com.github.shawven.calf.oplog.server.publisher.DataPublisherManager;
import io.reactivex.rxjava3.disposables.Disposable;
import org.bson.BsonTimestamp;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.github.shawven.calf.oplog.server.core.OpLogClientFactory.EVENTTYPE_KEY;


/**
 * @author: kl @kailing.pub
 * @date: 2019/8/1
 */
public class OplogTaskListener implements TaskListener {

    Logger logger = LoggerFactory.getLogger(getClass());

    private final NodeConfig nodeConfig;

    private final OpLogClientFactory opLogClientFactory;

    private final ClientDataSource clientDataSource;

    private final NodeConfigDataSource nodeConfigDataSource;

    private final DataPublisherManager dataPublisherManager;

    private OplogClient oplogClient;

    private Disposable disposable;

    private final Map<String, Map<DatabaseEvent, List<ClientInfo>>> clientInfoMap = new ConcurrentHashMap<>();

    public OplogTaskListener(NodeConfig nodeConfig,
                             OpLogClientFactory opLogClientFactory,
                             ClientDataSource clientDataSource,
                             NodeConfigDataSource nodeConfigDataSource,
                             DataPublisherManager dataPublisherManager) {
        this.nodeConfig = nodeConfig;
        this.opLogClientFactory = opLogClientFactory;
        this.clientDataSource = clientDataSource;
        this.nodeConfigDataSource = nodeConfigDataSource;
        this.dataPublisherManager = dataPublisherManager;
    }

    @Override
    public void start() {
        oplogClient = opLogClientFactory.initClient(nodeConfig);

        // 更新Client列表
        updateClientInfoMap(clientDataSource.listConsumerClient(nodeConfig));

        // 监听Client列表变化
        clientDataSource.watcherClientInfo(nodeConfig, this::updateClientInfoMap);

        OpLogEventFormatterFactory formatterFactory = new OpLogEventFormatterFactory(nodeConfig.getNamespace());

        // 启动连接
        try {
            disposable = oplogClient.getOplog().subscribe(document -> {
                opLogClientFactory.eventCount.incrementAndGet();

                // 获取数据格式器
                String eventType = document.getString(EVENTTYPE_KEY);
                OpLogEventFormatter formatter = formatterFactory.getFormatter(eventType);

                // 处理
                handle(document, formatter);

                // 更新状态
                updateOpLogStatus(document);
            });
            nodeConfig.setActive(true);
            nodeConfig.setVersion(nodeConfig.getVersion() + 1);
            nodeConfigDataSource.update(nodeConfig);
        } catch (Exception e) {
            logger.error("[" + nodeConfig.getNamespace() + "] 处理事件异常，{}", e);
        }

    }

    @Override
    public void end() {
        disposable.dispose();
        nodeConfig.setActive(false);
        nodeConfigDataSource.update(nodeConfig);
        opLogClientFactory.closeClient(oplogClient, nodeConfig.getNamespace());
    }

    /**
     * 处理event
     *
     * @param event
     * @param formatterFactory
     */
    private void handle(Document event, OpLogEventFormatter formatter) {
        List<ClientInfo> clientInfos = filterClient(event);
        if (clientInfos.isEmpty()) {
            return;
        }

        EventBaseDTO formatData = formatter.format(event);
        if (formatData == null) {
            logger.debug("uninterested:{}", event);
            return;
        }
        try {
            dataPublisherManager.publish(clientInfos, formatData);
        } catch (Exception e) {
            logger.error("dataPublisherManager.publish error: " + e.getMessage(), e);
        }
    }

    protected List<ClientInfo> filterClient(Document event) {
        String eventType = event.getString(EVENTTYPE_KEY);
        DatabaseEvent databaseEvent = convertDatabaseEvent(eventType);
        if (databaseEvent == null) {
            return Collections.emptyList();
        }

        String database = DocumentUtils.getDataBase(event);
        String table = DocumentUtils.getTable(event);
        String tableKey = database.concat("/").concat(table);

        Map<DatabaseEvent, List<ClientInfo>> eventListMap = clientInfoMap.getOrDefault(tableKey, Collections.emptyMap());
        return eventListMap.getOrDefault(databaseEvent, Collections.emptyList());
    }


    private static DatabaseEvent convertDatabaseEvent(String eventType) {
        DatabaseEvent databaseEvent = null;
        switch (eventType) {
            case "i":
                databaseEvent = DatabaseEvent.WRITE_ROWS;
                break;
            case "u":
                databaseEvent = DatabaseEvent.UPDATE_ROWS;
                break;
            case "d":
                databaseEvent = DatabaseEvent.DELETE_ROWS;
                 break;
            default:
        }
        return databaseEvent;
    }


    /**
     * 更新日志位置
     *
     * @param document
     */
    protected void updateOpLogStatus(Document document) {
        BsonTimestamp ts = (BsonTimestamp) document.get(OpLogClientFactory.TIMESTAMP_KEY);
        clientDataSource.updateNodeStatus(String.valueOf(ts.getTime()), ts.getInc(), nodeConfig);
    }

    private void updateClientInfoMap(Collection<ClientInfo> clientInfos) {
        clientInfos.stream()
                .collect(Collectors.groupingBy(this::getClientInfoMapKey))
                .forEach((mapKey, clientInfoList) -> {
                    Map<DatabaseEvent, List<ClientInfo>> eventListMap = clientInfoList.stream()
                            .collect(Collectors.groupingBy(ClientInfo::getDatabaseEvent));
                    clientInfoMap.put(mapKey,  new ConcurrentHashMap<>(eventListMap));
                });
    }

    private String getClientInfoMapKey(ClientInfo clientInfo) {
        return clientInfo.getDatabaseName().concat("/").concat(clientInfo.getTableName());
    }
}
