package com.github.shawven.calf.oplog.server.datasource.leaderselector;


import java.io.Closeable;

/**
 * @author wanglaomo
 * @since 2019/7/29
 **/
public interface LeaderSelector extends Closeable {

    /**
     * start racing for leadership.
     */
    void start();


    @Override
    void close();
}
