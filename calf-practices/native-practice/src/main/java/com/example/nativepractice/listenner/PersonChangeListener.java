package com.example.nativepractice.listenner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.nativepractice.constant.CacheKey;
import com.example.nativepractice.constant.ExchangeName;
import com.example.nativepractice.constant.QueueName;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


/**
 * @author xw
 * @date 2022/10/9
 */
@Component
public class PersonChangeListener {

    private final Logger logger = LoggerFactory.getLogger(PersonChangeListener.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 订阅人员状态变更，清除门户的权限缓存
     *
     * 详见：http://cms.yzjop.com/pages/viewpage.action?pageId=21561939#open2019%E7%A7%BB%E5%8A%A8%E7%AB%AF%E6%8E%A5%E5%8F%A3-5%E3%80%81%E4%BA%BA%E5%91%98%E3%80%81%E8%81%8C%E5%91%98%E5%8F%98%E6%9B%B4mq
     *
     * @param message
     * @throws IOException
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    exchange = @Exchange(
                            value = ExchangeName.USER_NETWORK,
                            type = ExchangeTypes.FANOUT
                    ),
                    value = @Queue(
                            value = QueueName.AUTH_CLEANER_OF_USER_NETWORK,
                            durable = "true"
                    )
            )
    )
    public void rabbitOnMessage(Message message) throws IOException {
        JSONObject jsonObj = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8));

        if ("userNetwork".equals(jsonObj.getString("fromType"))) {
            //   add 入职
            //   del 离职
            //   dept_change 部门变动
            //   jobtitle_change 职位变动
            //   name_change 名字变动
            //   weights_change 权重变动
            if (StringUtils.equalsAny(jsonObj.getString("event_type"), "dept_change", "jobtitle_change")) {
                logger.info("PersonChangeListener:{}", jsonObj.toJSONString());

                String eid = jsonObj.getString("eid");
                redisTemplate.opsForSet().add(CacheKey.Const.AUTH_APP_CLEANER, eid);
            }
        }
    }


    /**
     * 兼职部门变动
     *
     * {
     *   "newData": {},
     *   "oldData": {
     *     "eid": "10109",
     *     "createTime": "2022-11-17 14:07:58",
     *     "orgUserType": "0",
     *     "pid": "604aca60e4b0e941dd68938a",
     *     "id": "2ecceb79-663e-11ed-9523-2298c557103a",
     *     "oid": "604aca60e4b0e941dd68938b",
     *     "type": "1",
     *     "job": "7",
     *     "weights": "2147483647",
     *     "orgId": "61295698-c283-11eb-a60e-82e47cc7294a",
     *     "lastUpdateTime": "2022-11-17 14:07:58"
     *   },
     *   "table": "t_bd_person_casvirorg",
     *   "type": "DELETE"
     * }
     * {
     *   "newData": {
     *     "eid": "10109",
     *     "createTime": "2022-11-17 14:08:40",
     *     "orgUserType": "0",
     *     "pid": "604aca60e4b0e941dd68938a",
     *     "id": "48366cf5-663e-11ed-9523-2298c557103a",
     *     "oid": "604aca60e4b0e941dd68938b",
     *     "type": "1",
     *     "job": "7",
     *     "weights": "2147483647",
     *     "orgId": "61295698-c283-11eb-a60e-82e47cc7294a",
     *     "lastUpdateTime": "2022-11-17 14:08:40"
     *   },
     *   "oldData": {},
     *   "table": "t_bd_person_casvirorg",
     *   "type": "INSERT"
     * }
     *
     * @param body
     */
    @KafkaListener(topics = {"binlog_opensys.t_bd_person_casvirorg"})
    public void kafkaOnMessage(@Payload String body) {
        JSONObject jsonObject = JSON.parseObject(body);

        JSONObject itemData = jsonObject.getJSONObject("oldData");
        if (itemData == null || itemData.isEmpty()) {
            itemData = jsonObject.getJSONObject("newData");
        }

        String eid = null;
        if (itemData != null) {
            eid = itemData.getString("eid");
        }

        if (eid != null) {
            logger.info("PersonCasvirOrgListener:{}", body);
            redisTemplate.opsForSet().add(CacheKey.Const.AUTH_APP_CLEANER, eid);
        }
    }
}
