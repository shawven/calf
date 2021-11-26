package com.github.shawven.calf.oplog.client;


import com.github.shawven.calf.oplog.base.EventBaseDTO;

/**
 * @author zhenhui
 * @Ddate Created in 2018/18/01/2018/7:10 PM
 * @modified by
 */
public interface DatabaseEventHandler {

    void handle(EventBaseDTO data);

    Class<? extends DatabaseEventHandler> getClazz();
}
