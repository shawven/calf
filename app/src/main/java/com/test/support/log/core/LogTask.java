package com.test.support.log.core;

import com.test.support.log.annotation.Log;

/**
 * @author Shoven
 * @date 2019-07-30 10:17
 */
public interface LogTask extends Runnable {

    void write(Recordable record, Log annotation);
}
