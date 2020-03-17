package com.starter.log.core;

/**
 * @author Shoven
 * @date 2019-07-30 10:17
 */
public interface LogTask extends Runnable {

    void write(Recordable record, JoinPointInfo annotation);
}
