package com.github.shawven.calf.extension;


import java.io.Closeable;
import java.util.concurrent.ExecutionException;

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
    String getLeader();

    /**
     * start racing for leadership.
     */
    void start();


    @Override
    void close();
}
