package com.github.shawven.calf.oplog.server.publisher.rabbit;

import com.github.shawven.calf.oplog.base.EventBaseDTO;
import com.rabbitmq.http.client.domain.QueueInfo;

import java.util.List;

/**
 * @author wanglaomo
 * @since 2019/6/6
 **/
public interface RabbitService {

    QueueInfo getQueue(String clientId);

    List<EventBaseDTO> getMessageList(String clientId, long count);
}
