package com.starter.log.core;

/**
 * @author Shoven
 * @date 2019-07-27 19:23
 */
public interface LogRepository {

    boolean isSupport(JoinPointInfo annotation);

    void write(Recordable record);
}
