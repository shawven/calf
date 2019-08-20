package com.test.support.log.core;

import com.test.support.log.annotation.Log;

/**
 * @author Shoven
 * @date 2019-07-27 19:23
 */
public interface LogRepository {

    boolean isSupport(Log annotation);

    void write(Recordable record);
}
