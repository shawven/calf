package com.github.shawven.calf.oplog.server.autoconfig;

import com.github.shawven.calf.oplog.server.datasource.ClientDataSource;
import com.github.shawven.calf.oplog.server.publisher.DataPublisher;
import com.github.shawven.calf.oplog.server.datasource.NodeConfigDataSource;
import com.github.shawven.calf.oplog.server.publisher.DataPublisherManager;
import com.github.shawven.calf.oplog.server.KeyPrefixUtil;
import com.github.shawven.calf.oplog.server.OplogServer;
import com.github.shawven.calf.oplog.server.core.DistributorService;
import com.github.shawven.calf.oplog.server.core.MongoDBDistributorServiceImpl;
import com.github.shawven.calf.oplog.server.core.OpLogClientFactory;
import com.github.shawven.calf.oplog.server.datasource.etcd.EtcdClientDataSource;
import com.github.shawven.calf.oplog.server.datasource.etcd.EtcdNodeConfigDataSource;
import com.github.shawven.calf.oplog.server.publisher.rabbit.RabbitConfig;
import com.github.shawven.calf.oplog.server.publisher.rabbit.RabbitDataPublisher;
import com.github.shawven.calf.oplog.server.publisher.rabbit.RabbitService;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Util;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
@Import(RedissonConfig.class)
class OplogServerAutoConfiguration {

    private final Logger logger = LoggerFactory.getLogger(OplogServerAutoConfiguration.class);

    @Bean
    public OpLogClientFactory opLogClientFactory(ClientDataSource clientDataSource,
                                          NodeConfigDataSource nodeConfigDataSource,
                                          RedissonClient redissonClient,
                                          DataPublisherManager dataPublisherManager) {
        return new OpLogClientFactory(clientDataSource, nodeConfigDataSource, redissonClient, dataPublisherManager);
    }

    @Bean
    public DistributorService distributorService(OpLogClientFactory opLogClientFactory,
                                          ClientDataSource clientDataSource,
                                          NodeConfigDataSource nodeConfigDataSource,
                                          KeyPrefixUtil keyPrefixUtil,
                                          DataPublisherManager dataPublisherManager) {
        return new MongoDBDistributorServiceImpl(opLogClientFactory, clientDataSource, nodeConfigDataSource,
                keyPrefixUtil, dataPublisherManager);
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


    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            Environment env = ctx.getEnvironment();
            logger.info("server.port=>{}",env.getProperty("server.port"));
            logger.info("spring.redisson.address=>{}",env.getProperty("spring.redisson.address"));
            logger.info("spring.redisson.database=>{}", env.getProperty("spring.redisson.database"));
            String[] beanDefinitionNames =  ctx.getBeanDefinitionNames();
            Arrays.stream(beanDefinitionNames).sorted().forEach(val ->{
            });
        };
    }

    @Configuration(proxyBeanMethods = false)
    static class DataSourceConfiguration {


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
            public KeyPrefixUtil keyPrefixUtil(EtcdProperties etcdProperties) {
                return new KeyPrefixUtil(etcdProperties.getRoot());
            }

            @Bean
            @ConditionalOnMissingBean(NodeConfigDataSource.class)
            public NodeConfigDataSource nodeConfigDataSource(Client client, KeyPrefixUtil keyPrefixUtil) {
                return new EtcdNodeConfigDataSource(client, keyPrefixUtil);
            }

            @Bean
            @ConditionalOnMissingBean(ClientDataSource.class)
            public ClientDataSource clientDataSource(Client client, KeyPrefixUtil keyPrefixUtil,
                                                     NodeConfigDataSource nodeConfigDataSource) {
                return new EtcdClientDataSource(client, keyPrefixUtil, nodeConfigDataSource);
            }

        }
    }

    @Configuration(proxyBeanMethods = false)
    static class DataPublisherConfiguration {

        @Configuration(proxyBeanMethods = false)
        @ConditionalOnClass(ConnectionFactory.class)
        @Import(RabbitConfig.class)
        static class RabbitConfiguration {

            @Value("${spring.rabbit.virtualHost}")
            private String vHost;

            @Bean
            public RabbitService rabbitService() {
                return new RabbitService(vHost);
            }

            @Bean
            @ConditionalOnProperty("spring.rabbit.host")
            public DataPublisher dataPublisher(AmqpAdmin amqpAdmin,
                                               AmqpTemplate amqpTemplate,
                                               DirectExchange notifyExchange,
                                               TopicExchange dataExchange) {
                return new RabbitDataPublisher(amqpAdmin, amqpTemplate, notifyExchange, dataExchange);
            }
        }
    }
}
