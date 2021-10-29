package com.github.shawven.calf.oplog.server.config;


import com.github.shawven.calf.oplog.rabbit.DataPublisherRabbitMQ;
import com.github.shawven.calf.oplog.server.DataPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;

/**
 * @author T-lih
 */
@Configuration
public class BeanConfig {

    private static final Logger log = LoggerFactory.getLogger(BeanConfig.class);



    @Bean("opLogDataPublisher")
    public DataPublisher opLogDataPublisher(){
        return new DataPublisher();
    }



    @Bean
    @ConditionalOnProperty("spring.rabbit.host")
    public DataPublisherRabbitMQ dataPublisherRabbitMQ() {
        DataPublisherRabbitMQ dataPublisher = null;
        try {
            dataPublisher = new DataPublisherRabbitMQ();
        } catch (Exception e) {
            log.error("初始化rabbit队列实现失败", e);
        }
        return dataPublisher;
    }



    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            Environment env = ctx.getEnvironment();
            log.info("server.port=>{}",env.getProperty("server.port"));
            log.info("spring.redisson.address=>{}",env.getProperty("spring.redisson.address"));
            log.info("spring.redisson.database=>{}", env.getProperty("spring.redisson.database"));
            String[] beanDefinitionNames =  ctx.getBeanDefinitionNames();
            Arrays.stream(beanDefinitionNames).sorted().forEach(val ->{
            });
        };
    }

}
