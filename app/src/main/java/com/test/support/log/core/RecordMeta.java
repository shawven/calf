package com.test.support.log.core;

/**
 * @author Shoven
 * @date 2019-07-25 17:16
 */
public interface RecordMeta {

    JoinPointInfo getJoinPointInfo();

    Throwable getCause();
}

