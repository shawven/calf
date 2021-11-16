package com.github.shawven.calf.oplog.server.datasource.leaderselector;


import java.io.Closeable;

/**
 * @author wanglaomo
 * @since 2019/7/29
 **/
public interface LeaderSelector extends Closeable {


    /**
     * get current leader.
     *
     * @return null if there is no current leader.
     */
    String getLeader() throws Exception;

    /**
     * start racing for leadership.
     */
    void start();


    @Override
    void close();
}
