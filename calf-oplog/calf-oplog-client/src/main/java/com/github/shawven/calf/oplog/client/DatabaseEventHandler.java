package com.github.shawven.calf.oplog.client;


/**
 * @author zhenhui
 * @Ddate Created in 2018/18/01/2018/7:10 PM
 * @modified by
 */
public interface DatabaseEventHandler {

    void handle(String data);

    Class<? extends DatabaseEventHandler> getClazz();
}
