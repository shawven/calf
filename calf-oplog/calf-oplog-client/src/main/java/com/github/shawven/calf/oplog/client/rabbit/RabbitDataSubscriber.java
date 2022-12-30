package com.github.shawven.calf.oplog.client.rabbit;

import com.github.shawven.calf.oplog.client.DataSubscriber;
import com.github.shawven.calf.oplog.client.DatabaseEventHandler;
import com.rabbitmq.client.*;
import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.domain.QueueInfo;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.ReceiveAndReplyCallback;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;


/**
 * @auther: chenjh
 * @time: 2018/11/20 10:18
 * @description
 */
public class RabbitDataSubscriber implements DataSubscriber {

    public static final String NOTIFIER = "BIN-LOG-NOTIFIER-";
    public static final String DATA = "BIN-LOG-DATA-";

    private Client rabbitHttpClient;

    private RabbitTemplate rabbitTemplate;

    private RabbitClient rabbitClient;

    private RedissonClient redissonClient;

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public RabbitDataSubscriber(Client rabbitHttpClient,
                                RabbitTemplate rabbitTemplate,
                                RedissonClient redissonClient) throws Exception {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitHttpClient = rabbitHttpClient;
        this.redissonClient = redissonClient;
    }

    @Override
    public void subscribe(String clientId, Function<String, DatabaseEventHandler> handlerFunc) {
        List<QueueInfo> queueList = getQueueInfos();
        ConnectionFactory connectionFactory = rabbitTemplate.getConnectionFactory();

        //处理历史数据
        queueList.stream().filter(queueInfo -> queueInfo.getName().startsWith(DATA + clientId) && !queueInfo.getName().endsWith("-Lock"))
                .forEach(queueInfo -> {
                    RabbitDataHandler dataHandler = new RabbitDataHandler(
                            queueInfo.getName(), clientId, redissonClient, connectionFactory, handlerFunc);
                    EXECUTOR.submit(dataHandler);
                });

        try {

            Channel channel = rabbitTemplate.getConnectionFactory().createConnection().createChannel(false);
                channel.queueDeclare(NOTIFIER + clientId, true, false, true, null);
                Consumer consumer = new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        String msg = new String(body);
                        //每次推送都会执行这个方法，每次开线程，使用线程里面redis锁判断开销太大，先在外面判断一次
                        if (!RabbitDataHandler.DATA_KEY_IN_PROCESS.contains(msg)) {
                            //如果没在处理再进入

                            RabbitDataHandler dataHandler = new RabbitDataHandler(msg, clientId, redissonClient, connectionFactory, handlerFunc);
                            EXECUTOR.submit(dataHandler);
                        }
                    }
                };
            channel.basicConsume(NOTIFIER + clientId, true, consumer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<QueueInfo> getQueueInfos() {
        return rabbitHttpClient.getQueues(this.rabbitClient.getConnectionFactory().getVirtualHost());
    }
}
