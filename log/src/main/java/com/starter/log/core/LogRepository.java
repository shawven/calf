package com.starter.log.core;

import com.starter.log.annotation.Log;

/**
 * @author Shoven
 * @date 2019-07-27 19:23
 */
public interface LogRepository {

    boolean isSupport(Log annotation);

    void write(Recordable record);
}
