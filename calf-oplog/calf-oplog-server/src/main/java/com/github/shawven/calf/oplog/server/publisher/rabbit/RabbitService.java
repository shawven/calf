package com.github.shawven.calf.oplog.server.publisher.rabbit;

import com.github.shawven.calf.oplog.base.EventBaseDTO;
import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.domain.QueueInfo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Collections;
import java.util.List;

/**
 * @author wanglaomo
 * @since 2019/6/6
 **/
public class RabbitService {

    private RabbitTemplate rabbitTemplate;

    private Client rabbitHttpClient;

    public RabbitService(RabbitTemplate rabbitTemplate, Client rabbitHttpClient) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitHttpClient = rabbitHttpClient;
    }

    public QueueInfo getQueue(String name) {
        String virtualHost = rabbitTemplate.getConnectionFactory().getVirtualHost();
        return rabbitHttpClient.getQueue(virtualHost, name);
    }

    public List<EventBaseDTO> getMessageList(String clientId, long count) {

        return Collections.emptyList();
    }
}
