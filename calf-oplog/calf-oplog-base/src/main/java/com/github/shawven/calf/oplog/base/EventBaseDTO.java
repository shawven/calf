package com.github.shawven.calf.oplog.base;


import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author zhenhui
 * @Ddate Created in 2018/19/01/2018/3:20 PM
 * @modified by
 */
@Data
public class EventBaseDTO implements Serializable {

    private static final long serialVersionUID = -188511150756468947L;

    /**
     * 确保传输的数据唯一
     */
    private String uuid = UUID.randomUUID().toString();

    /**
     * 目标队列
     */
    private String destQueue;

    /**
     * 时间动作
     */
    private EventAction eventAction;

    private String database;

    private String table;

    private String namespace;

    private Long timestamp = System.currentTimeMillis();

    public EventBaseDTO() {
    }

    public EventBaseDTO(String namespace, EventAction eventAction, String database, String table) {
        this.namespace = namespace;
        this.eventAction = eventAction;
        this.database = database;
        this.table = table;
    }

    public String key() {
        return namespace + "_" + database + "_" + table;
    }
}
