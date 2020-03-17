package com.starter.log.core;

import com.starter.log.config.LogPointcutConfiguration;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author Shoven
 * @date 2019-07-25 17:04
 */
@Aspect
@Component
public class LogLogAspect extends LogPointcutConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(LogLogAspect.class);

    @Autowired
    @Qualifier("taskExecutor")
    private ThreadPoolTaskExecutor executor;

    @Autowired
    private LogTaskCreator logTaskCreator;

    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Object result = null;
        Throwable cause = null;
        long startTime = System.currentTimeMillis();

        try {
            // 执行代理方法保存返回结果值
            result = pjp.proceed();
            return result;
        } catch (Throwable e) {
            cause = e;
            throw e;
        } finally {
            // 记录耗时
            long cost = System.currentTimeMillis() - startTime;
            // 创建日志任务
            LogTask logTask = cause == null
                    ? createNormalLog(pjp, result, cost)
                    : createExceptionalLog(pjp, cause, cost);
            // 异步执行
            executeTask(logTask);
        }
    }

    private LogTask createNormalLog(JoinPoint jp, Object value, long cost) {
        try {
            return logTaskCreator.create(jp, value, cost);
        } catch (Exception e) {
            throw new LogException(String.format("创建日志任务失败： %s", e.getMessage()), e);
        }
    }

    private LogTask createExceptionalLog(JoinPoint jp, Throwable cause, long cost) {
        try {
            return logTaskCreator.create(jp, cause, cost);
        } catch (Exception e) {
            throw new LogException(String.format("创建日志任务失败： %s", e.getMessage()), e);
        }
    }

    private void executeTask(LogTask task) {
        try {
            executor.execute(task);
        } catch (LogException e) {
            logger.warn("执行日志任务失败：{}", e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
