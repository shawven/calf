package com.example.kafkapractice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author xw
 * @date 2024/7/19
 */
@Component
public class KafkaPracticeListener {

    private final Logger logger = LoggerFactory.getLogger(KafkaPracticeListener.class);

    @Autowired
    RedissonClient redissonClient;

    ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = Const.TOPIC, concurrency = Const.PARTITION + "")
    public void onMessage(List<ConsumerRecord<String, String>> records, Acknowledgment ack) throws JsonProcessingException {
        for (ConsumerRecord<String, String> record : records) {
            String key = record.key();
            String value = record.value();

            JsonNode jsonNode = objectMapper.readTree(value);
            String otherKey = jsonNode.get("key").asText();

            if (!key.equals(otherKey)) {
                throw new IllegalStateException();
            }

            RLock lock = redissonClient.getLock(Const.TOPIC + ":" + key);

            if (!lock.tryLock()) {
                logger.error("Try lock failed");
                System.exit(-1);
            }

            try {
                logger.info("Process record: " + record.value());
                //
                Uninterruptibles.sleepUninterruptibly(30, TimeUnit.MILLISECONDS);
            } finally {
                lock.unlock();
            }

        }
        ack.acknowledge();
    }

}
