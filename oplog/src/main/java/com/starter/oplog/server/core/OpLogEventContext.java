package com.starter.oplog.server.core;


import com.mongodb.MongoClient;
import com.starter.oplog.server.DataPublisher;
import com.starter.oplog.server.config.BinaryLogConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: kl @kailing.pub
 * @date: 2020/1/7
 */
public class OpLogEventContext {

    private MongoClient  mongoClient;


    private BinaryLogConfig binaryLogConfig;

    private DataPublisher dataPublisher;

    public OpLogEventContext(MongoClient mongoClient,  BinaryLogConfig binaryLogConfig, DataPublisher dataPublisher) {
        this.mongoClient = mongoClient;
        this.binaryLogConfig = binaryLogConfig;
        this.dataPublisher = dataPublisher;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public BinaryLogConfig getBinaryLogConfig() {
        return binaryLogConfig;
    }

    public DataPublisher getDataPublisher() {
        return dataPublisher;
    }

}
