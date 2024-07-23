package com.example.kafkapractice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author xw
 * @date 2024/7/19
 */
@Component
public class KafkaPracticeProducer {

    private final Logger logger = LoggerFactory.getLogger(KafkaPracticeProducer.class);

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(cron = "*/10 * * * * *")
    public void produceMessage() {
        for (int i = 0; i < 10000; i++) {
            UUID uuid = UUID.randomUUID();
            int key = uuid.hashCode() % Const.PARTITION;

            ObjectNode node = objectMapper.createObjectNode();
            node.put("key", key);
            node.put("index", i);
            node.put("body", uuid.toString());

            kafkaTemplate.send(Const.TOPIC, String.valueOf(key), node.toString());
        }
    }
}
