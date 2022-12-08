package com.github.shawven.calf.oplog.server.core;


import com.github.shawven.calf.oplog.server.datasource.NodeConfig;
import com.github.shawven.calf.oplog.server.datasource.ClientDataSource;
import com.github.shawven.calf.oplog.server.datasource.NodeConfigDataSource;
import com.github.shawven.calf.oplog.server.publisher.DataPublisherManager;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.BsonTimestamp;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: kl @kailing.pub
 * @date: 2020/1/7
 */
@Component
public class OpLogClientFactory {

    private static final Logger logger = LoggerFactory.getLogger(OpLogClientFactory.class);

    protected final RedissonClient redissonClient;

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
                              RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        this.nodeConfigDataSource = nodeConfigDataSource;
        this.clientDataSource = clientDataSource;
    }

    public OplogClient initClient(NodeConfig nodeConfig) {
        MongoClient mongoClient = MongoClients.create(nodeConfig.getDataSourceUrl());

        OplogClient client = new OplogClient(mongoClient);

        // 配置当前位置
        configOpLogStatus(client, nodeConfig);

        return client;
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

    public boolean closeClient(OplogClient oplogClient, String namespace) {
        try {
            // remove active namespace
            oplogClient.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
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
            logger.error(e.getMessage(), e);
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
