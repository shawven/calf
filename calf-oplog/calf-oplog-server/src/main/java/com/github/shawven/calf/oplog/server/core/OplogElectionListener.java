package com.github.shawven.calf.oplog.server.core;

import com.github.shawven.calf.oplog.base.EventAction;
import com.github.shawven.calf.oplog.base.EventBaseDTO;
import com.github.shawven.calf.oplog.register.domain.ClientInfo;
import com.github.shawven.calf.oplog.register.domain.DataSourceCfg;
import com.github.shawven.calf.oplog.register.election.ElectionListener;
import com.github.shawven.calf.oplog.server.ops.ClientOps;
import com.github.shawven.calf.oplog.server.ops.DataSourceCfgOps;
import com.github.shawven.calf.oplog.server.ops.StatusOps;
import com.github.shawven.calf.oplog.server.publisher.DataPublisherManager;
import com.github.shawven.calf.oplog.server.support.DocumentUtils;
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
public class OplogElectionListener implements ElectionListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DataSourceCfg dataSourceCfg;

    private final OpLogClientFactory opLogClientFactory;

    private final StatusOps statusOps;

    private final ClientOps clientOps;

    private final DataSourceCfgOps dataSourceCfgOps;

    private final DataPublisherManager dataPublisherManager;

    private OplogClient oplogClient;

    private Disposable disposable;

    private final Map<String, Set<EventAction>> watchedEvents = new ConcurrentHashMap<>();

    public OplogElectionListener(DataSourceCfg dataSourceCfg,
                                 OpLogClientFactory opLogClientFactory,
                                 StatusOps statusOps, ClientOps clientOps,
                                 DataSourceCfgOps dataSourceCfgOps,
                                 DataPublisherManager dataPublisherManager) {
        this.dataSourceCfg = dataSourceCfg;
        this.opLogClientFactory = opLogClientFactory;
        this.statusOps = statusOps;
        this.clientOps = clientOps;
        this.dataSourceCfgOps = dataSourceCfgOps;
        this.dataPublisherManager = dataPublisherManager;
    }

    @Override
    public void isLeader() {
        oplogClient = opLogClientFactory.initClient(dataSourceCfg);

        // 更新关注的事件
        updateWatchedEvents(clientOps.listConsumerClient(dataSourceCfg));

        // 监听Client列表变化，更新关注的事件
        clientOps.watcherClientInfo(dataSourceCfg, this::updateWatchedEvents);

        OpLogEventFormatterFactory formatterFactory = new OpLogEventFormatterFactory(dataSourceCfg.getNamespace());

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
            dataSourceCfg.setActive(true);
            dataSourceCfg.setVersion(dataSourceCfg.getVersion() + 1);
            dataSourceCfgOps.update(dataSourceCfg);
        } catch (Exception e) {
            logger.error("[" + dataSourceCfg.getNamespace() + "] 处理事件异常，{}", e);
        }

    }

    @Override
    public void notLeader() {
        if (disposable != null) {
            disposable.dispose();
        }

        dataSourceCfg.setActive(false);
        dataSourceCfgOps.update(dataSourceCfg);
        opLogClientFactory.closeClient(oplogClient, dataSourceCfg.getNamespace());
    }

    /**
     * 处理event
     *
     * @param event
     * @param formatter
     */
    private void handle(Document event, OpLogEventFormatter formatter) {
        if (!filterDocument(event)) {
            return;
        }

        EventBaseDTO formatData = formatter.format(event);
        if (formatData == null) {
            logger.debug("uninterested:{}", event);
            return;
        }

        // 设置目标队列
        formatData.setDestQueue(dataSourceCfg.getDestQueue());

        try {
            dataPublisherManager.publish(formatData);
        } catch (Exception e) {
            logger.error("dataPublisherManager.publish error: " + e.getMessage(), e);
        }
    }

    /**
     * 更新日志位置
     *
     * @param document
     */
    protected void updateOpLogStatus(Document document) {
        BsonTimestamp ts = (BsonTimestamp) document.get(OpLogClientFactory.TIMESTAMP_KEY);
        statusOps.updateDataSourceStatus(String.valueOf(ts.getTime()), ts.getInc(), dataSourceCfg);
    }

    protected boolean filterDocument(Document event) {
        EventAction eventAction = parseEventAction(event.getString(EVENTTYPE_KEY));
        if (eventAction == null) {
            return false;
        }
        String tableKey = DocumentUtils.getDataBase(event).concat("/").concat(DocumentUtils.getTable(event));
        Set<EventAction> eventActions = watchedEvents.getOrDefault(tableKey, Collections.emptySet());
        return eventActions.contains(eventAction);
    }

    private static EventAction parseEventAction(String eventType) {
        EventAction databaseEvent = null;
        switch (eventType) {
            case "i":
                databaseEvent = EventAction.INSERT;
                break;
            case "u":
                databaseEvent = EventAction.UPDATE;
                break;
            case "d":
                databaseEvent = EventAction.DELETE;
                break;
            default:
        }
        return databaseEvent;
    }

    private synchronized void updateWatchedEvents(Collection<ClientInfo> clientInfos) {
        clientInfos.stream()
                .collect(Collectors.groupingBy(this::getEventKey))
                .forEach((key, clients) -> {
                    Set<EventAction> newSet = clients.stream()
                            .map(ClientInfo::getEventAction)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    Set<EventAction> set = watchedEvents.computeIfAbsent(key, (s) -> new HashSet<>());
                    set.addAll(newSet);
                });
    }

    private String getEventKey(ClientInfo clientInfo) {
        return clientInfo.getDatabaseName().concat("/").concat(clientInfo.getTableName());
    }
}
