package com.github.shawven.calf.oplog.server.publisher.rabbit;

import com.alibaba.fastjson.JSON;
import com.github.shawven.calf.oplog.base.Const;
import com.github.shawven.calf.oplog.base.EventBaseDTO;
import com.github.shawven.calf.oplog.register.domain.ClientInfo;
import com.github.shawven.calf.oplog.server.publisher.DataPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @auther: chenjh
 * @time: 2018/11/19 14:57
 * @description
 */
public class RabbitDataPublisher implements DataPublisher {

    private static final Logger logger = LoggerFactory.getLogger(RabbitDataPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitDataPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(EventBaseDTO data) {
        String msg = JSON.toJSONString(data);
        try {
            String routingKey = Const.withEventQueue(data.key());

            sendData(routingKey, msg);
            logger.info("推送信息 {}", msg);
        } catch (Exception e) {
            logger.error("推送信息  " + msg + " 失败", e);
        }
    }

    private void sendData(String routingKey, String msg) {
        rabbitTemplate.convertAndSend(Const.RABBIT_EVENT_EXCHANGE, routingKey, msg);
    }


    @Override
    public boolean destroy(ClientInfo clientInfo) {
        rabbitTemplate.destroy();
        return true;
    }

}
