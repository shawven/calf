package com.github.shawven.calf.oplog.server.core;


import com.github.shawven.calf.oplog.server.datasource.ClientInfo;
import com.github.shawven.calf.oplog.server.datasource.NodeConfig;
import com.github.shawven.calf.oplog.server.datasource.ClientDataSource;
import com.github.shawven.calf.oplog.server.datasource.NodeConfigDataSource;
import com.github.shawven.calf.oplog.server.publisher.DataPublisherManager;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.internal.MongoClientImpl;
import org.bson.BsonTimestamp;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: kl @kailing.pub
 * @date: 2020/1/7
 */
@Component
public class OpLogClientFactory {

    private static final Logger log = LoggerFactory.getLogger(OpLogClientFactory.class);

    protected final RedissonClient redissonClient;

    private final DataPublisherManager dataPublisherManager;

    private final NodeConfigDataSource nodeConfigDataSource;
    private final ClientDataSource clientDataSource;


    public volatile AtomicLong eventCount = new AtomicLong(0);

    /**
     * 事件类型的key，值可为i(插入)、u(更新)、d(删除)
     */
    public static final String EVENTTYPE_KEY = "op";
    /**
     * 数据库、集合名称
     */
    public static final String DATABASE_KEY = "ns";
    /**
     * 消费时间戳
     */
    public static final String TIMESTAMP_KEY = "ts";
    /**
     * 内容
     */
    public static final String CONTEXT_KEY = "o";
    /**
     * 更新事件更新的条件
     */
    public static final String UPDATE_WHERE_KEY = "o2";
    /**
     * 更新事件，更新的内容
     */
    public static final String UPDATE_CONTEXT_KEY = "$set";


    private long lastEventCount = 0;

    public OpLogClientFactory(ClientDataSource clientDataSource,
                              NodeConfigDataSource nodeConfigDataSource,
                              RedissonClient redissonClient,
                              DataPublisherManager dataPublisherManager) {
        this.redissonClient = redissonClient;
        this.dataPublisherManager = dataPublisherManager;
        this.nodeConfigDataSource = nodeConfigDataSource;
        this.clientDataSource = clientDataSource;
    }

    public OplogClient initClient(NodeConfig nodeConfig) {

        String namespace = nodeConfig.getNamespace();

        MongoClient mongoClient = this.getMongoClient(nodeConfig);
        OpLogEventContext context = createEventContext(nodeConfig, mongoClient);

        OpLogEventHandlerFactory opLogEventHandlerFactory = new OpLogEventHandlerFactory(context);

        OplogClient client = new OplogClient(mongoClient, opLogEventHandlerFactory);

        // 配置当前位置
        configOpLogStatus(client, nodeConfig);
        // 启动Client列表数据监听
        registerMetaDataWatcher(nodeConfig, opLogEventHandlerFactory);
        return client;
    }

    private OpLogEventContext createEventContext(NodeConfig nodeConfig, MongoClient mongoClient) {
        List<ClientInfo> clients = clientDataSource.listBinLogConsumerClient(nodeConfig);
        return new OpLogEventContext(mongoClient, nodeConfig, dataPublisherManager, clients);
    }

    /**
     * 配置当前binlog位置
     *
     * @param oplogClient
     * @param nodeConfig
     */
    private void configOpLogStatus(OplogClient oplogClient, NodeConfig nodeConfig) {
        Map<String, Object> binLogStatus = clientDataSource.getBinaryLogStatus(nodeConfig);
        if (binLogStatus != null) {
            int seconds = Integer.parseInt(String.valueOf(binLogStatus.get("binlogFilename")));
            int inc = Integer.parseInt((String.valueOf(binLogStatus.get("binlogPosition"))));
            oplogClient.setTs(new BsonTimestamp(seconds, inc));
        }
    }

    private MongoClient getMongoClient(NodeConfig nodeConfig) {
        return MongoClients.create(nodeConfig.getDataSourceUrl());
    }

    private void registerMetaDataWatcher(NodeConfig nodeConfig, OpLogEventHandlerFactory opLogEventHandlerFactory) {
    }



    public boolean closeClient(OplogClient oplogClient, String namespace) {
        try {
            // remove active namespace
            oplogClient.close();
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        NodeConfig nodeConfig = nodeConfigDataSource.getByNamespace(namespace);
        try {
            while (nodeConfig.isActive()) {
                TimeUnit.SECONDS.sleep(1);
                nodeConfig = nodeConfigDataSource.getByNamespace(namespace);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
            return false;
        }

        return true;
    }


    public long getEventCount() {
        return eventCount.get();
    }

    public long eventCountSinceLastTime() {

        long total = eventCount.get();
        long res = total - lastEventCount;

        lastEventCount = total;

        return res;
    }
}
