package com.github.shawven.calf.oplog.server.autoconfig;

import com.github.shawven.calf.oplog.register.ElectionFactory;
import com.github.shawven.calf.oplog.register.Repository;
import com.github.shawven.calf.oplog.server.support.KeyUtils;
import com.github.shawven.calf.oplog.server.ServerRunner;
import com.github.shawven.calf.oplog.server.core.ReplicationServer;
import com.github.shawven.calf.oplog.server.core.MongoReplicationServerImpl;
import com.github.shawven.calf.oplog.server.core.OpLogClientFactory;
import com.github.shawven.calf.oplog.server.ops.*;
import com.github.shawven.calf.oplog.server.publisher.DataPublisher;
import com.github.shawven.calf.oplog.server.publisher.DataPublisherManager;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.*;

import java.util.List;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter({DataPublisherAutoConfiguration.class})
class OplogServerAutoConfiguration implements InitializingBean {

    @Value("${calf-oplog.root}") String root;

    @Override
    public void afterPropertiesSet() throws Exception {
        KeyUtils.setRoot(root);
    }

    @Bean
    @ConditionalOnMissingBean(ClientOps.class)
    public ClientOps clientDAOImpl(Repository repository, DataSourceCfgOps dataSourceCfgOps) {
        return new ClientOpsImpl(repository, dataSourceCfgOps);
    }

    @Bean
    @ConditionalOnMissingBean(StatusOps.class)
    public StatusOps statusDAOImpl(Repository repository, DataSourceCfgOps dataSourceCfgOps) {
        return new StatusOpsImpl(repository, dataSourceCfgOps);
    }

    @Bean
    @ConditionalOnMissingBean(DataSourceCfgOps.class)
    public DataSourceCfgOps dataSourceCfgDAOImpl(Repository repository) {
        return new DataSourceCfgOpsImpl(repository);
    }

    @Bean
    public OpLogClientFactory opLogClientFactory(StatusOps statusOps,
                                                 DataSourceCfgOps dataSourceCfgOps,
                                                 RedissonClient redissonClient) {
        return new OpLogClientFactory(statusOps, dataSourceCfgOps, redissonClient);
    }

    @Bean
    public ReplicationServer mongoReplicationServerImpl(OpLogClientFactory opLogClientFactory,
                                                        ElectionFactory electionFactory,
                                                        ClientOps clientOps,
                                                        StatusOps statusOps,
                                                        DataSourceCfgOps dataSourceCfgOps,
                                                        DataPublisherManager dataPublisherManager) {
        return new MongoReplicationServerImpl(opLogClientFactory, electionFactory, dataSourceCfgOps,
                clientOps, statusOps, dataPublisherManager);
    }

    @Bean
    public ServerRunner oplogServer(List<ReplicationServer> serverList) {
        return new ServerRunner(serverList);
    }

    @Bean
    public DataPublisherManager opLogDataPublisher(Map<String, DataPublisher> dataPublisherMap){
        return new DataPublisherManager(dataPublisherMap);
    }

    @Configuration(proxyBeanMethods = false)
    @ComponentScan(basePackages = "com.github.shawven.calf.oplog.server.web")
    static class webSupportConfiguration {

    }

}
