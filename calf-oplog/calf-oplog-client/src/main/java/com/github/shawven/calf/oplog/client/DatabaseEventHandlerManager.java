package com.github.shawven.calf.oplog.client;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author zhenhui
 * @Ddate Created in 2018/18/01/2018/7:20 PM
 * @modified by
 */
public class DatabaseEventHandlerManager {
    private final static Logger log = Logger.getLogger(DatabaseEventHandlerManager.class.toString());
    private Set<Class<? extends DatabaseEventHandler>> handlers = new HashSet<>();

    public final static Map<String, DatabaseEventHandler> HANDLER_MAP = new ConcurrentHashMap<>();
    private String serverUrl;
    private String queueType;
    private String clientId;
    private DataSubscriber dataSubscriber;

    public DatabaseEventHandlerManager(String clientId, DataSubscriber dataSubscriber) {
        this.clientId = clientId;
        this.dataSubscriber = dataSubscriber;
    }

    public DatabaseEventHandlerManager(String serverUrl, String clientId, DataSubscriber dataSubscriber) {
        this.serverUrl = serverUrl;
        this.clientId = clientId;
        this.dataSubscriber = dataSubscriber;
    }

    public DatabaseEventHandlerManager(String serverUrl, String queueType, String clientId, DataSubscriber dataSubscriber) {
        this.serverUrl = serverUrl;
        this.queueType = queueType;
        this.clientId = clientId;
        this.dataSubscriber = dataSubscriber;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public DatabaseEventHandlerManager setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
        return this;
    }

    public String getQueueType() {
        return queueType;
    }

    public DatabaseEventHandlerManager setQueueType(String queueType) {
        this.queueType = queueType;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    public DatabaseEventHandlerManager setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * 自动扫描注册handler
     *
     * @return
     */
    public DatabaseEventHandlerManager autoScanHandler() {
        //初始化handler

        return this;
    }

    /**
     * 注册handler，在使用ioc框架时通过此处注册
     *
     * @param handler
     * @return
     */
    public DatabaseEventHandlerManager registerHandler(DatabaseEventHandler handler) {
        List<String> keys = addHandler(handler.getClazz());
        keys.forEach(k -> HANDLER_MAP.put(k, handler));
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
    public DatabaseEventHandlerManager autoRegisterClient() {
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
        dataSubscriber.subscribe(clientId, HANDLER_MAP::get);
    }

}
