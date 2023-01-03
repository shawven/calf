package com.github.shawven.calf.oplog.examples.listener;

import com.github.shawven.calf.oplog.client.DataSubscribeRegistry;
import com.github.shawven.calf.oplog.client.rabbit.RabbitDataConsumer;
import com.github.shawven.calf.oplog.examples.handler.ExampleDataEventHandler;
import com.rabbitmq.http.client.Client;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    private Client rabbitHttpClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${databaseEventServerUrl}")
    private String serverUrl;

    @Value("${appName}")
    private String appName;

    @Autowired
    private ExampleDataEventHandler exampleDatabaseEventHandler;

    @PostConstruct
    public void start() throws Exception {
        //初始化订阅的实现
        new DataSubscribeRegistry()
                .setClientId(appName)
                .setServerUrl(serverUrl)
                .setDataConsumer(new RabbitDataConsumer(rabbitTemplate))
                .registerToServer()
                .start();
    }
}
