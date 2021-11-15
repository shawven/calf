package com.github.shawven.calf.oplog.server.rabbit;

import com.github.shawven.calf.base.EventBaseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @auther: chenjh
 * @time: 2018/11/19 14:57
 * @description
 */
public class DataPublisherRabbitMQ {

    private static final Logger log = LoggerFactory.getLogger(DataPublisherRabbitMQ.class);

    public static final String NOTIFIER = "BIN-LOG-NOTIFIER-";

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private DirectExchange notifyExchange;

    @Autowired
    private TopicExchange dataExchange;

    public void doPublish(String clientId, String dataKey, EventBaseDTO data) {
        try {
            sendData(dataKey, data);
            log.info("推送信息,{}", data);
            String notifier = NOTIFIER.concat(clientId);
            sendNoftify(notifier, dataKey);
        } catch (Exception e) {
            log.error("推送信息, " + data + " 失败", e);
        }
    }

    private void sendData(String queueName, Object msg) {
        Queue queue = new Queue(queueName, true, false, true);
        amqpAdmin.declareQueue(queue);
        Binding binding = BindingBuilder.bind(queue).to(dataExchange).with(queueName);
        amqpAdmin.declareBinding(binding);
        amqpTemplate.convertAndSend(dataExchange.getName(),queueName, msg);
    }

    private void sendNoftify(String queueName, Object msg) {
        Queue queue = new Queue(queueName, true, false, true);
        amqpAdmin.declareQueue(queue);
        Binding binding = BindingBuilder.bind(queue).to(notifyExchange).withQueueName();
        amqpAdmin.declareBinding(binding);
        amqpTemplate.convertAndSend(notifyExchange.getName(),queueName, msg);
    }

    public boolean deleteTopic(String topicName) {
        Queue queue = new Queue(topicName, true, false, true);
        amqpAdmin.declareQueue(queue);
        Binding binding = BindingBuilder.bind(queue).to(dataExchange).with(topicName);
        amqpAdmin.declareBinding(binding);
        return amqpAdmin.deleteQueue(topicName);
    }

}
