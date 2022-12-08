package com.github.shawven.calf.oplog.server.autoconfig;

import com.github.shawven.calf.oplog.server.KeyPrefixUtil;
import com.github.shawven.calf.oplog.server.OplogServer;
import com.github.shawven.calf.oplog.server.core.DistributorService;
import com.github.shawven.calf.oplog.server.core.MongoDBDistributorServiceImpl;
import com.github.shawven.calf.oplog.server.core.OpLogClientFactory;
import com.github.shawven.calf.oplog.server.datasource.ClientDataSource;
import com.github.shawven.calf.oplog.server.datasource.LeaderSelectorFactory;
import com.github.shawven.calf.oplog.server.datasource.NodeConfigDataSource;
import com.github.shawven.calf.oplog.server.datasource.etcd.EtcdLeaderSelectorFactory;
import com.github.shawven.calf.oplog.server.publisher.DataPublisher;
import com.github.shawven.calf.oplog.server.publisher.DataPublisherManager;
import com.github.shawven.calf.oplog.server.publisher.rabbit.RabbitService;
import com.github.shawven.calf.oplog.server.publisher.rabbit.RabbitServiceImpl;
import com.github.shawven.calf.oplog.server.web.ClientController;
import com.github.shawven.calf.oplog.server.web.ClientServiceImpl;
import com.github.shawven.calf.oplog.server.web.DataSourceController;
import io.etcd.jetcd.Client;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter({DataSourceAutoConfiguration.class, DataPublisherAutoConfiguration.class})
class OplogServerAutoConfiguration {

    private final Logger logger = LoggerFactory.getLogger(OplogServerAutoConfiguration.class);

    @Bean
    public OpLogClientFactory opLogClientFactory(ClientDataSource clientDataSource,
                                          NodeConfigDataSource nodeConfigDataSource,
                                          RedissonClient redissonClient) {
        return new OpLogClientFactory(clientDataSource, nodeConfigDataSource, redissonClient);
    }

    @Bean
    public DistributorService distributorService(OpLogClientFactory opLogClientFactory,
                                                 LeaderSelectorFactory leaderSelectorFactory,
                                                 ClientDataSource clientDataSource,
                                                 NodeConfigDataSource nodeConfigDataSource,
                                                 KeyPrefixUtil keyPrefixUtil,
                                                 DataPublisherManager dataPublisherManager) {
        return new MongoDBDistributorServiceImpl(opLogClientFactory, leaderSelectorFactory, nodeConfigDataSource,
                keyPrefixUtil, clientDataSource, dataPublisherManager);
    }

    @Bean
    public OplogServer oplogServer(Map<String, DistributorService> distributorServiceMap,
                            ClientDataSource clientDataSource,
                            NodeConfigDataSource nodeConfigDataSource) {
        return new OplogServer(distributorServiceMap, clientDataSource, nodeConfigDataSource);
    }

    @Bean
    public DataPublisherManager opLogDataPublisher(Map<String, DataPublisher> dataPublisherMap){
        return new DataPublisherManager(dataPublisherMap);
    }

    @Configuration(proxyBeanMethods = false)
    @Import({ClientController.class, DataSourceController.class, ClientServiceImpl.class, RabbitServiceImpl.class})
    static class webSupportConfiguration {

    }

}
