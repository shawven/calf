package com.github.shawven.calf.oplog.server.autoconfig;

import com.github.shawven.calf.oplog.register.ElectionFactory;
import com.github.shawven.calf.oplog.register.Repository;
import com.github.shawven.calf.oplog.server.KeyPrefixUtil;
import com.github.shawven.calf.oplog.server.OplogServer;
import com.github.shawven.calf.oplog.server.core.ReplicationServer;
import com.github.shawven.calf.oplog.server.core.MongoReplicationServerImpl;
import com.github.shawven.calf.oplog.server.core.OpLogClientFactory;
import com.github.shawven.calf.oplog.server.dao.*;
import com.github.shawven.calf.oplog.server.publisher.DataPublisher;
import com.github.shawven.calf.oplog.server.publisher.DataPublisherManager;
import com.github.shawven.calf.oplog.server.publisher.rabbit.RabbitServiceImpl;
import com.github.shawven.calf.oplog.server.web.ClientController;
import com.github.shawven.calf.oplog.server.web.ClientServiceImpl;
import com.github.shawven.calf.oplog.server.web.DataSourceController;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Map;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter({DataPublisherAutoConfiguration.class})
class OplogServerAutoConfiguration {

    @Bean
    public KeyPrefixUtil keyPrefixUtil(@Value("${calf-oplog.root}") String root) {
        return new KeyPrefixUtil(root);
    }

    @Bean
    @ConditionalOnMissingBean(ClientDAO.class)
    public ClientDAO clientDAOImpl(Repository repository, KeyPrefixUtil keyPrefixUtil,
                                   DataSourceCfgDAO dataSourceCfgDAO) {
        return new ClientDAOImpl(repository, keyPrefixUtil, dataSourceCfgDAO);
    }

    @Bean
    @ConditionalOnMissingBean(StatusDAO.class)
    public StatusDAO statusDAOImpl(Repository repository, KeyPrefixUtil keyPrefixUtil,
                                   DataSourceCfgDAO dataSourceCfgDAO) {
        return new StatusDAOImpl(repository, keyPrefixUtil, dataSourceCfgDAO);
    }

    @Bean
    @ConditionalOnMissingBean(DataSourceCfgDAO.class)
    public DataSourceCfgDAO dataSourceCfgDAOImpl(Repository repository, KeyPrefixUtil keyPrefixUtil) {
        return new DataSourceCfgDAOImpl(repository, keyPrefixUtil);
    }

    @Bean
    public OpLogClientFactory opLogClientFactory(StatusDAO statusDAO,
                                                 DataSourceCfgDAO dataSourceCfgDAO,
                                                 RedissonClient redissonClient) {
        return new OpLogClientFactory(statusDAO, dataSourceCfgDAO, redissonClient);
    }

    @Bean
    public ReplicationServer mongoReplicationServerImpl(OpLogClientFactory opLogClientFactory,
                                                        ElectionFactory electionFactory,
                                                        ClientDAO clientDAO,
                                                        StatusDAO statusDAO,
                                                        DataSourceCfgDAO dataSourceCfgDAO,
                                                        KeyPrefixUtil keyPrefixUtil,
                                                        DataPublisherManager dataPublisherManager) {
        return new MongoReplicationServerImpl(opLogClientFactory, electionFactory, dataSourceCfgDAO,
                clientDAO, statusDAO, keyPrefixUtil, dataPublisherManager);
    }

    @Bean
    public OplogServer oplogServer(Map<String, ReplicationServer> distributorServiceMap) {
        return new OplogServer(distributorServiceMap);
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
