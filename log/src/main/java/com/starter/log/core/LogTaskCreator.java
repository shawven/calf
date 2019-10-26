package com.starter.log.core;

import org.aspectj.lang.JoinPoint;

/**
 * @author Shoven
 * @date 2019-07-26 11:12
 */
public interface LogTaskCreator {

    LogTask create(JoinPoint jp, Object value, Throwable cause, long cost);
}
