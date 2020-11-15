package com.starter.oplog.client;


import com.starter.oplog.model.ClientInfo;
import com.starter.oplog.model.dto.EventBaseDTO;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author zhenhui
 * @Ddate Created in 2018/18/01/2018/7:20 PM
 * @modified by
 */
public class BinLogDistributorClient {
    private final static Logger log = Logger.getLogger(BinLogDistributorClient.class.toString());
    private Set<Class<? extends DatabaseEventHandler>> handlers = new HashSet<>();

    public final static Map<String, DatabaseEventHandler> HANDLER_MAP = new ConcurrentHashMap<>();
    private String serverUrl;
    private String queueType = ClientInfo.QUEUE_TYPE_REDIS;
    private String clientId;
    private DataSubscriber dataSubscriber;

    public BinLogDistributorClient(String clientId, DataSubscriber dataSubscriber) {
        this.clientId = clientId;
        this.dataSubscriber = dataSubscriber;
    }

    public BinLogDistributorClient(String serverUrl, String clientId, DataSubscriber dataSubscriber) {
        this.serverUrl = serverUrl;
        this.clientId = clientId;
        this.dataSubscriber = dataSubscriber;
    }

    public BinLogDistributorClient(String serverUrl, String queueType, String clientId, DataSubscriber dataSubscriber) {
        this.serverUrl = serverUrl;
        this.queueType = queueType;
        this.clientId = clientId;
        this.dataSubscriber = dataSubscriber;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public BinLogDistributorClient setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
        return this;
    }

    public String getQueueType() {
        return queueType;
    }

    public BinLogDistributorClient setQueueType(String queueType) {
        this.queueType = queueType;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    public BinLogDistributorClient setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * 自动扫描注册handler
     *
     * @return
     */
    public BinLogDistributorClient autoScanHandler() {
        //初始化handler

        return this;
    }

    /**
     * 注册handler，在使用ioc框架时通过此处注册
     *
     * @param handler
     * @return
     */
    public BinLogDistributorClient registerHandler(DatabaseEventHandler handler) {
        List<String> keys = addHandler(handler.getClazz());
        keys.stream().forEach(k -> HANDLER_MAP.put(k, handler));
        return this;
    }

    /**
     * 如果添加了注释HandleDatabaseEvent，添加到handler set，并返回关注的key，否则忽视
     *
     * @param clazz
     * @return
     */
    private List<String> addHandler(Class<? extends DatabaseEventHandler> clazz) {
        HandleDatabaseEvent ann = clazz.getAnnotation(HandleDatabaseEvent.class);
        if (ann != null) {
            handlers.add(clazz);
            return Arrays.stream(ann.events()).map(e -> ann.namespace() + ann.database() + ann.table() + e).collect(Collectors.toList());
        }
        return new ArrayList<>(0);
    }

    /**
     * 注册client到服务器
     *
     * @return
     */
    public BinLogDistributorClient autoRegisterClient() {
        try {
            if (serverUrl != null && handlers.size() > 0) {

            }
        } catch (Exception e) {
            log.severe("注册项目出现未知异常，跳过注册");
            e.printStackTrace();
        }
        return this;
    }


    /**
     * 执行监听
     */
    public void start() {
        dataSubscriber.subscribe(clientId, this);
    }

    /**
     * 处理信息
     *
     * @param dto
     */
    public void handle(EventBaseDTO dto) {

        String key = dto.getNamespace() + dto.getDatabase() + dto.getTable() + dto.getEventType();
        DatabaseEventHandler eventHandler = HANDLER_MAP.get(key);
        if (eventHandler != null) {
            eventHandler.handle(dto);
        } else {
            log.warning("no " + key + " handler");
        }
    }

}
