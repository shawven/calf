package com.github.shawven.calf.oplog.client;


import com.github.shawven.calf.oplog.base.EventAction;

/**
 * @author zhenhui
 * @Ddate Created in 2018/18/01/2018/7:10 PM
 * @modified by
 */
public interface DataSubscribeHandler {

    String key();

    EventAction[] actions();

    void handle(String data);
}
