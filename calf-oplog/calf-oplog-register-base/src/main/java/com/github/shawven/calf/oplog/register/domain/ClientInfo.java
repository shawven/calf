package com.github.shawven.calf.oplog.register.domain;


import com.github.shawven.calf.oplog.base.DatabaseEvent;
import com.github.shawven.calf.oplog.base.LockLevel;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author zhenhui
 * @date Created in 2018/17/01/2018/4:02 PM
 * @modified by
 */
@Data
public class ClientInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String QUEUE_TYPE_REDIS = "redis";
    public static final String QUEUE_TYPE_RABBIT = "rabbit";
    public static final String QUEUE_TYPE_KAFKA = "kafka";

    /**
     * 客户端编号
     */
    private String clientId;

    /**
     * 队列实现方式 默认为redis
     */
    private String queueType = QUEUE_TYPE_REDIS;

    /**
     * 关注的数据库的标识
     */
    private String namespace = "default";

    /**
     * 关注的数据库名
     */
    private String databaseName;

    /**
     * 关注的表名
     */
    private String tableName;

    /**
     * 关注的表的事件
     */
    private DatabaseEvent databaseEvent;

    /**
     * 数据锁定级别
     */
    private LockLevel lockLevel;

    /**
     * 锁级别为列的时候，使用指定列名
     */
    private String columnName;

    /**
     * 拼接key，避免频繁拼接
     */
    private String key;

    public ClientInfo() {

    }

    public ClientInfo(String clientId, String queueType, String namespace, String databaseName, String tableName, DatabaseEvent databaseEvent, LockLevel lockLevel, String columnName) {
        this.clientId = clientId;
        this.queueType = queueType;
        this.namespace = namespace == null ? "default" : namespace;
        this.databaseName = databaseName;
        this.tableName = tableName;
        this.databaseEvent = databaseEvent;
        this.lockLevel = lockLevel;
        this.columnName = columnName;

        String multiDataSourceSupport = "default".equals(this.namespace) ? "" : ("-" + namespace);

        switch (lockLevel) {
            case TABLE:
                key = clientId + multiDataSourceSupport + "-" + lockLevel + "-" + databaseName + "-" + tableName;
                break;
            case COLUMN:
                key = clientId + multiDataSourceSupport + "-" + lockLevel + "-" + databaseName + "-" + tableName + "-";
                break;
            case NONE:
            default:
                key = clientId + multiDataSourceSupport + "-" + lockLevel + "-" + databaseName;
        }
    }
}
