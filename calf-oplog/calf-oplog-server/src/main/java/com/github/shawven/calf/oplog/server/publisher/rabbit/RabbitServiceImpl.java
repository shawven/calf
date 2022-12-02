package com.github.shawven.calf.oplog.server.publisher.rabbit;

import com.github.shawven.calf.oplog.base.EventBaseDTO;
import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.domain.QueueInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author wanglaomo
 * @since 2019/6/5
 **/
@Service
@ConditionalOnProperty("spring.rabbit.host")
public class RabbitServiceImpl implements RabbitService {

    @Value("${spring.rabbit.virtualHost}")
    private String vHost;

    @Autowired
    Client rabbitHttpClient;

    @Override
    public QueueInfo getQueue(String clientId) {

        return rabbitHttpClient.getQueue(vHost, clientId);
    }

    @Override
    public List<EventBaseDTO> getMessageList(String clientId, long count) {

        return Collections.emptyList();
    }
}
