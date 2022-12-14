package com.github.shawven.calf.oplog.server.autoconfig;

import com.github.shawven.calf.oplog.server.KeyPrefixUtil;
import com.github.shawven.calf.oplog.server.datasource.ClientDataSource;
import com.github.shawven.calf.oplog.server.datasource.DataSource;
import com.github.shawven.calf.oplog.server.datasource.LeaderSelectorFactory;
import com.github.shawven.calf.oplog.server.datasource.NodeConfigDataSource;
import com.github.shawven.calf.oplog.server.datasource.etcd.EtcdDataSource;
import com.github.shawven.calf.oplog.server.datasource.etcd.EtcdLeaderSelectorFactory;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.support.Util;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class DataSourceAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(Client.class)
    @EnableConfigurationProperties(EtcdProperties.class)
    static class EtcdConfiguration {
        @Bean
        public Client etcdClient(EtcdProperties etcdProperties) {
            return Client
                    .builder()
                    .endpoints(Util.toURIs(etcdProperties.getEndpoints()))
                    //.authority(authority)
                    //.user(ByteSequence.from(username, StandardCharsets.UTF_8))
                    //.password(ByteSequence.from(password, StandardCharsets.UTF_8))
                    .build();
        }

        @Bean
        @ConditionalOnMissingBean(LeaderSelectorFactory.class)
        public LeaderSelectorFactory leaderSelectorFactory(Client client) {
            return new EtcdLeaderSelectorFactory(client);
        }

        @Bean
        public KeyPrefixUtil keyPrefixUtil(EtcdProperties etcdProperties) {
            return new KeyPrefixUtil(etcdProperties.getRoot());
        }

        @Bean
        public DataSource etcdDataSource(Client client) {
            return new EtcdDataSource(client);
        }
    }
}
