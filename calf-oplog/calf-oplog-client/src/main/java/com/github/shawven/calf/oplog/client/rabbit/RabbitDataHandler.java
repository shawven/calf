package com.github.shawven.calf.oplog.client.rabbit;


import com.github.shawven.calf.oplog.base.Const;
import com.github.shawven.calf.oplog.base.EventBaseDTO;
import com.github.shawven.calf.oplog.client.DatabaseEventHandler;
import com.github.shawven.calf.oplog.client.EventBaseErrorDTO;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @auther: chenjh
 * @time: 2018/11/20 13:28
 * @description
 */
public class RabbitDataHandler implements Runnable {
    private final static Logger log = Logger.getLogger(RabbitDataHandler.class.toString());

    private final String dataKey;
    private final Function<String, DatabaseEventHandler> handlerFunction;
    private final RedissonClient redissonClient;
    private final String errMapKey;
    private final String dataKeyLock;
    private final ConnectionFactory connectionFactory;
    private Channel channel;
    /**
     * 正在处理的队列
     */
    public static Set<String> DATA_KEY_IN_PROCESS = new ConcurrentHashMap<String, String>().keySet("");

    public RabbitDataHandler(String dataKey,
                             String clientId,
                             RedissonClient redissonClient,
                             ConnectionFactory connectionFactory,
                             Function<String, DatabaseEventHandler> handlerFunction) {
        this.dataKey = dataKey;
        this.redissonClient = redissonClient;
        this.connectionFactory = connectionFactory;
        this.errMapKey = Const.REDIS_PREFIX.concat("BIN-LOG-ERR-MAP-").concat(clientId);
        this.dataKeyLock = dataKey.concat("-Lock");
        this.handlerFunction = handlerFunction;
    }

    @Override
    public void run() {
        try {
            Channel channel = connectionFactory.createConnection().createChannel(false) ;
            channel.queueDeclare(dataKey, true, false, true, null);
            this.channel = channel;
        } catch (IOException e) {
            e.printStackTrace();
            log.severe("接收处理数据失败：" + e.toString());
        }

        doRunWithLock();
    }


    private void doRunWithLock() {
        RLock rLock = redissonClient.getLock(dataKeyLock);
        EventBaseDTO dto;
        boolean lockRes = false;
        try {
            // 尝试加锁，最多等待50ms(防止过多线程等待)，上锁以后6个小时自动解锁(防止redis队列太长，当前拿到锁的线程处理时间过长)
            lockRes = rLock.tryLock(50, 6 * 3600 * 1000, TimeUnit.MILLISECONDS);
            if (!lockRes) {
                return;
            }
            DATA_KEY_IN_PROCESS.add(dataKey);
            GetResponse getResponse;
            while ((getResponse = channel.basicGet(dataKey, false)) != null) {
                //反序列化对象
                ByteArrayInputStream bais = new ByteArrayInputStream(getResponse.getBody());
                ObjectInputStream ois = new ObjectInputStream(bais);
                dto = (EventBaseDTO) ois.readObject();
                boolean handleRes = doHandleWithLock(dto, 0);
                //处理完毕，把数据从队列摘除
                if (handleRes) {
                    channel.basicAck(getResponse.getEnvelope().getDeliveryTag(), false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.severe("接收处理数据失败：" + e.toString());
        } finally {
            //forceUnlock是可以释放别的线程拿到的锁的，需要判断是否是当前线程持有的锁
            if (lockRes) {
                rLock.forceUnlock();
                rLock.unlock();
                DATA_KEY_IN_PROCESS.remove(dataKey);
            }
        }
    }

    /**
     * handle及处理异常，出现异常未成功处理返回false，处理成功返回true
     *
     * @param dto
     * @param retryTimes
     */
    private boolean doHandleWithLock(final EventBaseDTO dto, Integer retryTimes) {
        try {
            handle(dto);
            //如果之前有异常，恢复正常，那就处理
            if (retryTimes != 0) {
                RMap<String, EventBaseErrorDTO> errMap = redissonClient.getMap(errMapKey, new JsonJacksonCodec());
                errMap.remove(dto.getUuid());
            }
            return true;
        } catch (Exception e) {
            if (retryTimes >= 5) {
                log.log(Level.SEVERE, "全部重试失败，请及时处理！", e);
                return true;
            }
            log.log(Level.INFO, "第" + ++retryTimes + "次重试");
            RMap<String, EventBaseErrorDTO> errMap = redissonClient.getMap(errMapKey, new JsonJacksonCodec());
            errMap.put(dto.getUuid(), new EventBaseErrorDTO(dto, e, dataKey));
            try {
                // 重试间隔,单位毫秒
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e1) {
                log.severe(e1.toString());
            }
            return doHandleWithLock(dto, retryTimes);
        }
    }

    public void handle(EventBaseDTO dto) {
        String key = dto.getNamespace() + dto.getDatabase() + dto.getTable() + dto.getEventType();;
        DatabaseEventHandler eventHandler = handlerFunction.apply(key);
        if (eventHandler != null) {
            eventHandler.handle(dto);
        } else {
            log.warning("no " + key + " handler");
        }
    }
}
