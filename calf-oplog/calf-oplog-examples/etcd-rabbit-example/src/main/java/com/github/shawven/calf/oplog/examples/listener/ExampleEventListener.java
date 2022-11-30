package com.github.shawven.calf.oplog.examples.listener;

import com.github.shawven.calf.oplog.client.DataSubscriber;
import com.github.shawven.calf.oplog.client.DatabaseEventHandlerManager;
import com.github.shawven.calf.oplog.client.rabbit.RabbitDataSubscriber;
import com.github.shawven.calf.oplog.examples.handler.ExampleDataEventHadler;
import com.rabbitmq.http.client.Client;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @auther: chenjh
 * @time: 2018/10/16 11:38
 * @description
 */
@Component
public class ExampleEventListener {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private Client rabbitHttpClient;

    @Autowired
    private RedissonClient redissonClient;

    @Value("${databaseEventServerUrl}")
    private String serverUrl;

    @Value("${appName}")
    private String appName;

    @Autowired
    private ExampleDataEventHadler exampleDatabaseEventHandler;

    @PostConstruct
    public void start() throws Exception {
        //初始化订阅的实现
        DataSubscriber dataSubscriber = new RabbitDataSubscriber(connectionFactory, rabbitHttpClient, redissonClient);
        new DatabaseEventHandlerManager(appName, dataSubscriber)
                //在binlog中注册handler
                .registerHandler(exampleDatabaseEventHandler)
                .setQueueType("rabbit")
                .setServerUrl(serverUrl).autoRegisterClient().start();
    }
}
