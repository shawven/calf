package com.github.shawven.calf.oplog.server.core;


import com.github.shawven.calf.extension.BinaryLogConfig;
import com.github.shawven.calf.oplog.server.DataPublisher;
import com.mongodb.MongoClient;

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
