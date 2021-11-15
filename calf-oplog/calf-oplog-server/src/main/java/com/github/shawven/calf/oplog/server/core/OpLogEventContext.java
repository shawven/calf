package com.github.shawven.calf.oplog.server.core;


import com.github.shawven.calf.extension.NodeConfig;
import com.github.shawven.calf.oplog.server.DataPublisher;
import com.mongodb.MongoClient;

/**
 * @author: kl @kailing.pub
 * @date: 2020/1/7
 */
public class OpLogEventContext {

    private MongoClient  mongoClient;


    private NodeConfig nodeConfig;

    private DataPublisher dataPublisher;

    public OpLogEventContext(MongoClient mongoClient, NodeConfig nodeConfig, DataPublisher dataPublisher) {
        this.mongoClient = mongoClient;
        this.nodeConfig = nodeConfig;
        this.dataPublisher = dataPublisher;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public NodeConfig getBinaryLogConfig() {
        return nodeConfig;
    }

    public DataPublisher getDataPublisher() {
        return dataPublisher;
    }

}
