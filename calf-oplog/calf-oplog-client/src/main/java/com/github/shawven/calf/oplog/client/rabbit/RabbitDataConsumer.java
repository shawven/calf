package com.github.shawven.calf.oplog.client.rabbit;

import com.github.shawven.calf.oplog.base.Const;
import com.github.shawven.calf.oplog.client.DataConsumer;
import com.github.shawven.calf.oplog.client.DataSubscribeHandler;
import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;
import java.util.Map;


/**
 * @auther: chenjh
 * @time: 2018/11/20 10:18
 * @description
 */
public class RabbitDataConsumer implements DataConsumer {

    private final Logger logger = LoggerFactory.getLogger(RabbitDataConsumer.class);

    private RabbitTemplate rabbitTemplate;

    private final Gson gson = new Gson();

    public RabbitDataConsumer(RabbitTemplate rabbitTemplate) throws Exception {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void consume(String clientId, Map<String, DataSubscribeHandler> handlerMap) {
        for (Map.Entry<String, DataSubscribeHandler> entry : handlerMap.entrySet()) {
            try {
                consume(clientId, entry.getKey(), entry.getValue());
            } catch (IOException e) {
                logger.error("subscribe failed :" + e.getMessage(), e);
            }
        }
    }

    private void consume(String clientId, String routingKey, DataSubscribeHandler handler) throws IOException {
        Channel channel = rabbitTemplate.getConnectionFactory().createConnection().createChannel(false);
        try {
            channel.queueDeclare(clientId, true, false, true, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        channel.queueBind(clientId, Const.RABBIT_EVENT_EXCHANGE, routingKey);

        channel.basicConsume(clientId, false, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                try {
                    handler.handle(new String(body));
                    channel.basicAck(envelope.getDeliveryTag(), false);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    try {
                        channel.basicNack(envelope.getDeliveryTag(), false, false);
                    } catch (IOException ex) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

    }
}
