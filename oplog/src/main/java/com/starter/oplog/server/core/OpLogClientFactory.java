package com.starter.oplog.server.core;


import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.starter.oplog.server.DataPublisher;
import com.starter.oplog.server.config.BinaryLogConfig;
import org.bson.BsonTimestamp;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: kl @kailing.pub
 * @date: 2020/1/7
 */
@Component
public class OpLogClientFactory {

    private static final Logger log = LoggerFactory.getLogger(OpLogClientFactory.class);

    protected final RedissonClient redissonClient;

    private final DataPublisher dataPublisher;

    private final BinaryLogConfigContainer binaryLogConfigContainer;


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

    public OpLogClientFactory(RedissonClient redissonClient,
                              @Qualifier("opLogDataPublisher") DataPublisher dataPublisher,
                              BinaryLogConfigContainer binaryLogConfigContainer) {
        this.redissonClient = redissonClient;
        this.dataPublisher = dataPublisher;
        this.binaryLogConfigContainer = binaryLogConfigContainer;
    }

    public OplogClient initClient(BinaryLogConfig binaryLogConfig) {

        String namespace = binaryLogConfig.getNamespace();

        MongoClient mongoClient = this.getMongoClient(binaryLogConfig);
        OpLogEventContext context = new OpLogEventContext(mongoClient, binaryLogConfig, dataPublisher);

        OpLogEventHandlerFactory opLogEventHandlerFactory = new OpLogEventHandlerFactory(context);

        OplogClient client = new OplogClient(mongoClient, opLogEventHandlerFactory);
        // 配置当前位置
        configOpLogStatus(client, binaryLogConfig);
        // 启动Client列表数据监听
        registerMetaDataWatcher(binaryLogConfig, opLogEventHandlerFactory);
        return client;
    }

    /**
     * 配置当前binlog位置
     *
     * @param oplogClient
     * @param binaryLogConfig
     */
    private void configOpLogStatus(OplogClient oplogClient, BinaryLogConfig binaryLogConfig) {
        JSONObject binLogStatus = etcdService.getBinaryLogStatus(binaryLogConfig);
        if (binLogStatus != null) {
            int seconds = binLogStatus.getIntValue("binlogFilename");
            int inc = binLogStatus.getIntValue("binlogPosition");
            oplogClient.setTs(new BsonTimestamp(seconds, inc));
        }
    }

    private MongoClient getMongoClient(BinaryLogConfig binaryLogConfig) {
        return new MongoClient(new MongoClientURI(binaryLogConfig.getDataSourceUrl()));
    }

    /**
     * 注册Client列表更新监听
     *
     * @param binaryLogConfig
     * @param opLogEventHandlerFactory
     */
    private void registerMetaDataWatcher(BinaryLogConfig binaryLogConfig, OpLogEventHandlerFactory opLogEventHandlerFactory) {

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
