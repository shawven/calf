package com.github.shawven.calf.oplog.register.election;


import java.io.Closeable;

/**
 * @author wanglaomo
 * @since 2019/7/29
 **/
public interface Election extends Closeable {

    /**
     * start racing for leadership.
     */
    void start();


    @Override
    void close();
}
