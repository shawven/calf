package com.starter.log.config;

import org.aspectj.lang.annotation.Pointcut;

/**
 * @author Shoven
 * @date 2019-07-05 18:21
 */
public class LogPointcutConfiguration {

    @Pointcut("@annotation(com.starter.log.annotation.Log)")
    public void methodsExistLog(){}

    @Pointcut("@target(com.starter.log.annotation.Log)")
    public void classExistLog(){}

    @Pointcut("@annotation(com.starter.log.annotation.InsertLog)")
    public void methodsExistInsertLog(){}

    @Pointcut("@annotation(com.starter.log.annotation.UpdateLog)")
    public void methodExistUpdateLog(){}

    @Pointcut("@annotation(com.starter.log.annotation.DeleteLog)")
    public void methodsExistDeleteLog(){}

    @Pointcut("(methodsExistInsertLog() || methodExistUpdateLog() || methodsExistDeleteLog()))")
    public void methodExistSubLog(){}

    @Pointcut("(methodsExistLog() || (classExistLog() && methodExistSubLog()))")
    public void logPointcut(){}

}
