package com.github.shawven.calf.oplog.server.core;


import com.github.shawven.calf.oplog.server.datasource.NodeConfig;
import com.github.shawven.calf.oplog.server.publisher.DataPublisherManager;
import com.mongodb.MongoClient;

/**
 * @author: kl @kailing.pub
 * @date: 2020/1/7
 */
public class OpLogEventContext {

    private MongoClient  mongoClient;


    private NodeConfig nodeConfig;

    private DataPublisherManager dataPublisherManager;

    public OpLogEventContext(MongoClient mongoClient, NodeConfig nodeConfig, DataPublisherManager dataPublisherManager) {
        this.mongoClient = mongoClient;
        this.nodeConfig = nodeConfig;
        this.dataPublisherManager = dataPublisherManager;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public NodeConfig getNodeConfig() {
        return nodeConfig;
    }

    public DataPublisherManager getDataPublisherManager() {
        return dataPublisherManager;
    }
}
