package com.starter.log.core;

import org.aspectj.lang.JoinPoint;

/**
 * @author Shoven
 * @date 2019-07-26 11:12
 */
public interface LogTaskCreator {

    /**
     * 创建日志任务
     *
     * @param jp 切入点
     * @param value 返回值
     * @param cost 花费时间
     * @return
     */
    LogTask create(JoinPoint jp, Object value, long cost);

    /**
     * 创建日志任务
     *
     * @param jp 切入点
     * @param cause 异常原因
     * @param cost 花费时间
     * @return
     */
    LogTask create(JoinPoint jp, Throwable cause, long cost);
}
