package com.github.shawven.calf.oplog.server.core;


import com.github.shawven.calf.oplog.server.datasource.ClientInfo;
import com.github.shawven.calf.oplog.server.datasource.NodeConfig;
import com.github.shawven.calf.oplog.server.publisher.DataPublisherManager;
import com.mongodb.client.MongoClient;

import java.util.List;

/**
 * @author: kl @kailing.pub
 * @date: 2020/1/7
 */
public class OpLogEventContext {

    private MongoClient mongoClient;

    private NodeConfig nodeConfig;

    private DataPublisherManager dataPublisherManager;

    private List<ClientInfo> clients;

    public OpLogEventContext(MongoClient mongoClient, NodeConfig nodeConfig,
                             DataPublisherManager dataPublisherManager, List<ClientInfo> clients) {
        this.mongoClient = mongoClient;
        this.nodeConfig = nodeConfig;
        this.dataPublisherManager = dataPublisherManager;
        this.clients = clients;
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

    public List<ClientInfo> getClients() {
        return clients;
    }
}
