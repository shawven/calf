package com.github.shawven.calf.oplog.server.core;


import com.github.shawven.calf.oplog.register.domain.ClientInfo;
import com.github.shawven.calf.oplog.register.domain.DataSourceCfg;
import com.github.shawven.calf.oplog.server.publisher.DataPublisherManager;
import com.mongodb.client.MongoClient;

import java.util.List;

/**
 * @author: kl @kailing.pub
 * @date: 2020/1/7
 */
public class OpLogEventContext {

    private MongoClient mongoClient;

    private DataSourceCfg dataSourceCfg;

    private DataPublisherManager dataPublisherManager;

    private List<ClientInfo> clients;

    public OpLogEventContext(MongoClient mongoClient, DataSourceCfg dataSourceCfg,
                             DataPublisherManager dataPublisherManager, List<ClientInfo> clients) {
        this.mongoClient = mongoClient;
        this.dataSourceCfg = dataSourceCfg;
        this.dataPublisherManager = dataPublisherManager;
        this.clients = clients;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public DataSourceCfg getNodeConfig() {
        return dataSourceCfg;
    }

    public DataPublisherManager getDataPublisherManager() {
        return dataPublisherManager;
    }

    public List<ClientInfo> getClients() {
        return clients;
    }
}
