package com.github.shawven.calf.oplog.server.core;


import com.github.shawven.calf.oplog.server.dao.StatusDAO;
import com.github.shawven.calf.oplog.register.domain.DataSourceCfg;
import com.github.shawven.calf.oplog.server.dao.DataSourceCfgDAO;
import com.github.shawven.calf.oplog.register.domain.DataSourceStatus;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.BsonTimestamp;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: kl @kailing.pub
 * @date: 2020/1/7
 */
@Component
public class OpLogClientFactory {

    private static final Logger logger = LoggerFactory.getLogger(OpLogClientFactory.class);

    private final StatusDAO statusDAO;

    private final DataSourceCfgDAO dataSourceCfgDAO;

    protected final RedissonClient redissonClient;

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

    public OpLogClientFactory(StatusDAO statusDAO,
                              DataSourceCfgDAO dataSourceCfgDAO,
                              RedissonClient redissonClient) {
        this.statusDAO = statusDAO;
        this.dataSourceCfgDAO = dataSourceCfgDAO;
        this.redissonClient = redissonClient;
    }

    public OplogClient initClient(DataSourceCfg dataSourceCfg) {
        MongoClient mongoClient = MongoClients.create(dataSourceCfg.getDataSourceUrl());

        OplogClient client = new OplogClient(mongoClient);

        // 配置当前位置
        configOpLogStatus(client, dataSourceCfg);

        return client;
    }

    /**
     * 配置当前binlog位置
     *
     * @param oplogClient
     * @param dataSourceCfg
     */
    private void configOpLogStatus(OplogClient oplogClient, DataSourceCfg dataSourceCfg) {
        DataSourceStatus dataSourceStatus = statusDAO.getDataSourceStatus(dataSourceCfg);
        if (dataSourceStatus != null) {
            int seconds = Integer.parseInt(String.valueOf(dataSourceStatus.getFilename()));
            int inc = Integer.parseInt((String.valueOf(dataSourceStatus.getPosition())));
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
        DataSourceCfg dataSourceCfg = dataSourceCfgDAO.getByNamespace(namespace);
        try {
            while (dataSourceCfg.isActive()) {
                TimeUnit.SECONDS.sleep(1);
                dataSourceCfg = dataSourceCfgDAO.getByNamespace(namespace);
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
