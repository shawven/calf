package com.github.shawven.calf.oplog.base;


import lombok.Data;

import java.io.Serializable;
import java.util.Objects;
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
    private DatabaseEvent eventType;
    private String database;
    private String table;
    private String namespace;
    private Long timestamp = System.currentTimeMillis();

    public EventBaseDTO() {
    }

    public EventBaseDTO(String namespace, DatabaseEvent eventType, String database, String table) {
        this.namespace = namespace;
        this.eventType = eventType;
        this.database = database;
        this.table = table;
    }

    public EventBaseDTO(EventBaseDTO eventBaseDTO) {
        this(eventBaseDTO.getNamespace(), eventBaseDTO.getEventType(),  eventBaseDTO.getDatabase(),eventBaseDTO.getTable());
    }
}
