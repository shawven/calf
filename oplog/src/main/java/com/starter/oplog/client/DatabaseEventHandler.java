package com.starter.oplog.client;


import com.starter.oplog.model.dto.EventBaseDTO;

/**
 * @author zhenhui
 * @Ddate Created in 2018/18/01/2018/7:10 PM
 * @modified by
 */
public interface DatabaseEventHandler {
    void handle(EventBaseDTO data);
    Class getClazz();
}
