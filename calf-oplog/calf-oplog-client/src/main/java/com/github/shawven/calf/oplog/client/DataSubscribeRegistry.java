package com.github.shawven.calf.oplog.client;


import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.context.SmartLifecycle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * @author xw
 * @date 2023-01-03
 */
@Setter
@Accessors(chain = true)
public class DataSubscribeRegistry implements SmartLifecycle {

    private final static Logger log = Logger.getLogger(DataSubscribeRegistry.class.toString());

    private final static Map<String, DataSubscribeHandler> HANDLER_MAP = new ConcurrentHashMap<>();

    private String clientId;

    private String serverUrl;

    private DataConsumer dataConsumer;

    private volatile boolean running;

    public DataSubscribeRegistry registerHandler(DataSubscribeHandler handler) {
        HANDLER_MAP.put(handler.key(), handler);
        return this;
    }

    /**
     * 注册到服务器
     *
     * @return
     */
    public DataSubscribeRegistry registerToServer() {
        try {
            if (serverUrl != null && !HANDLER_MAP.isEmpty()) {

            }
        } catch (Exception e) {
            log.severe("注册项目出现未知异常，跳过注册");
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    /**
     * 执行监听
     */
    public void start() {
        if (isRunning()) {
            return;
        }
        running = true;
        dataConsumer.startConsumers(clientId, HANDLER_MAP);
    }

    @Override
    public void stop() {
        running = false;
        dataConsumer.stopConsumers(clientId);
    }

    @Override
    public boolean isRunning() {
        return running;
    }

}
