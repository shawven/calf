package com.github.shawven.calf.oplog.server.publisher.rabbit;

import com.github.shawven.calf.base.EventBaseDTO;
import com.rabbitmq.http.client.domain.QueueInfo;

import java.util.List;

/**
 * @author wanglaomo
 * @since 2019/6/5
 **/
public class RabbitService {

    private String vHost;

    public RabbitService(String vHost) {
        this.vHost = vHost;
    }

    public QueueInfo getQueue(String clientId) {

        return null;
    }


    public List<EventBaseDTO> getMessageList(String clientId, long count) {

        return null;
    }
}
