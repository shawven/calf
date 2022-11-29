package com.example.nativepractice.listenner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.nativepractice.constant.ExchangeName;
import com.example.nativepractice.constant.QueueName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author xw
 * @date 2022/11/17
 */
@Component
public class CloudAuthChangeListener {

    private final Logger logger = LoggerFactory.getLogger(PersonChangeListener.class);

    @RabbitListener(
            bindings = @QueueBinding(
                    exchange = @Exchange(
                            value = ExchangeName.OPEN_LOG,
                            type = ExchangeTypes.FANOUT
                    ),
                    value = @Queue(
                            value = QueueName.AUTH_CLEANER_OF_AUTH_CENTER,
                            durable = "true"
                    )
            )
    )
    public void rabbitOnMessage(Message message) throws IOException {
        JSONObject jsonObj = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8));

        if ("AUTH_CENTER_MANAGE".equalsIgnoreCase(jsonObj.getString("eventType"))) {

            logger.info("ChangeListener:{}", jsonObj.toJSONString());
        }
    }
}
