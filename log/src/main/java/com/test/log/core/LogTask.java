package com.test.log.core;

import com.test.log.annotation.Log;

/**
 * @author Shoven
 * @date 2019-07-30 10:17
 */
public interface LogTask extends Runnable {

    void write(Recordable record, Log annotation);
}
