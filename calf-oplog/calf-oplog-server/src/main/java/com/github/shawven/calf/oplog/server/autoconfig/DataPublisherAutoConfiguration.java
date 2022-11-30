package com.github.shawven.calf.oplog.server.autoconfig;

import com.github.shawven.calf.oplog.server.publisher.DataPublisher;
import com.github.shawven.calf.oplog.server.publisher.rabbit.RabbitDataPublisher;
import com.rabbitmq.http.client.Client;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

@Configuration(proxyBeanMethods = false)
class DataPublisherAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(ConnectionFactory.class)
    @ConditionalOnProperty("spring.rabbit.host")
    @EnableConfigurationProperties(RabbitProperties.class)
    static class RabbitConfiguration {

        public static final String NOTIFY_EXCHANGE = "binlog.notify";
        public static final String DATA_EXCHANGE = "binlog.data";

        @Autowired
        private RabbitProperties rabbitProperties;


        @Bean
        @ConditionalOnProperty("spring.rabbit.host")
        public DataPublisher dataPublisher(AmqpAdmin amqpAdmin,
                                           AmqpTemplate amqpTemplate,
                                           DirectExchange notifyExchange,
                                           TopicExchange dataExchange) {
            return new RabbitDataPublisher(amqpAdmin, amqpTemplate, notifyExchange, dataExchange);
        }

        @Bean
        public ConnectionFactory connectionFactory() {
            CachingConnectionFactory factory = new CachingConnectionFactory();
            factory.setAddresses(rabbitProperties.getHost() + ":"  + rabbitProperties.getPort());
            factory.setVirtualHost(rabbitProperties.getVirtualHost());
            factory.setUsername(rabbitProperties.getUsername());
            factory.setPassword(rabbitProperties.getPassword());
            return factory;
        }

        @Bean
        public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
            return new RabbitAdmin(connectionFactory);
        }

        @Bean
        public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
            return new RabbitTemplate(connectionFactory);
        }

        @Bean
        public Client rabbitHttpClient() throws MalformedURLException, URISyntaxException {
            return new Client(rabbitProperties.getApiUrl(), rabbitProperties.getUsername(), rabbitProperties.getPassword());
        }

        @Bean
        public DirectExchange notifyExchange(ConnectionFactory connectionFactory) {
            DirectExchange notifyExchange = new DirectExchange(NOTIFY_EXCHANGE,true,false);
            new RabbitAdmin(connectionFactory).declareExchange(notifyExchange);
            return notifyExchange;
        }

        @Bean
        public TopicExchange dataExchange(ConnectionFactory connectionFactory) {
            TopicExchange dataExchange = new TopicExchange(DATA_EXCHANGE,true,false);
            new RabbitAdmin(connectionFactory).declareExchange(dataExchange);
            return dataExchange;
        }
    }


//    @Configuration(proxyBeanMethods = false)
//    @ConfigurationProperties(prefix = "kafka.zk")
//    @EnableConfigurationProperties(KafkaProperties.class)
//    static class KafkaConfiguration {
//
//        @Autowired
//        private KafkaProperties kafkaProperties;
//
//        @Bean
//        public ZkClient zkClient() {
//            return new ZkClient(kafkaProperties.getServers(),
//                    kafkaProperties.getSessionTimeout(),
//                    kafkaProperties.getConnectionTimeout(),
//                    ZKStringSerializer$.MODULE$);
//        }
//    }
}
