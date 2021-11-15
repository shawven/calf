package com.github.shawven.calf.oplog.server.rabbit;

import com.github.shawven.calf.base.EventBaseDTO;
import com.rabbitmq.http.client.domain.QueueInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wanglaomo
 * @since 2019/6/5
 **/
@Service
@ConditionalOnProperty("spring.rabbit.host")
public class RabbitMQService {

    @Value("${spring.rabbit.virtualHost}")
    private String vHost;


    public QueueInfo getQueue(String clientId) {

        return null;
    }


    public List<EventBaseDTO> getMessageList(String clientId, long count) {

        return null;
    }
}
