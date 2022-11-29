package com.github.shawven.calf.oplog.client.rabbit;

import com.github.shawven.calf.oplog.client.DataSubscriber;
import com.github.shawven.calf.oplog.client.DatabaseEventHandler;
import com.rabbitmq.client.*;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

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

    public Client rabbitHttpClient;

    public String vhost;

    private RabbitClient rabbitClient;

    private RedissonClient redissonClient;

    private static final ExecutorService executors = Executors.newFixedThreadPool(5);

    public RabbitDataSubscriber(ConnectionFactory connectionFactory,
                                Client rabbitHttpClient,
                                RedissonClient redissonClient) throws Exception {
        this.rabbitClient = RabbitClient.getInstance(connectionFactory);
        this.rabbitHttpClient = rabbitHttpClient;
        this.vhost = this.rabbitClient.getConnectionFactory().getVirtualHost();
        this.redissonClient = redissonClient;
    }

    @Override
    public void subscribe(String clientId, Function<String, DatabaseEventHandler> handlerFunc) {
        List<QueueInfo> queueList = rabbitHttpClient.getQueues(vhost);
        ConnectionFactory connectionFactory = rabbitClient.getConnectionFactory();
        //处理历史数据
        queueList.stream().filter(queueInfo -> queueInfo.getName().startsWith(DATA + clientId) && !queueInfo.getName().endsWith("-Lock"))
                .forEach(queueInfo -> {
                    RabbitDataHandler dataHandler = new RabbitDataHandler(queueInfo.getName(), clientId,
                            redissonClient, connectionFactory, handlerFunc);
                    executors.submit(dataHandler);
                });
        try {
            Channel channel = connectionFactory.createConnection().createChannel(false);
                channel.queueDeclare(NOTIFIER + clientId, true, false, true, null);
                Consumer consumer = new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        String msg = new String(body);
                        //每次推送都会执行这个方法，每次开线程，使用线程里面redis锁判断开销太大，先在外面判断一次
                        if (!RabbitDataHandler.DATA_KEY_IN_PROCESS.contains(msg)) {
                            //如果没在处理再进入

                            RabbitDataHandler dataHandler = new RabbitDataHandler(msg, clientId, redissonClient, connectionFactory, handlerFunc);
                            executors.submit(dataHandler);
                        }
                    }
                };
            channel.basicConsume(NOTIFIER + clientId, true, consumer);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
