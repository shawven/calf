package com.starter.log.core;

import com.starter.log.config.LogPointcutConfiguration;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Shoven
 * @date 2019-07-25 17:04
 */
@Aspect
@Component
public class LogAspect extends LogPointcutConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private List<LogRepository> repositories;

    @Autowired
    private LogMetaCreator<LogMeta> logMetaCreator;

    @Autowired
    private LogBuilder<LogMeta> logBuilder;

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
            LogMeta logMeta = cause == null
                    ? createNormalLog(pjp, result, cost)
                    : createExceptionalLog(pjp, cause, cost);
            // 异步执行
            executeTask(newTask(logMeta));
        }
    }

    private LogTask newTask(LogMeta logMeta) {
        return new LogTask(repositories, logBuilder, logMeta);
    }

    private LogMeta createNormalLog(JoinPoint jp, Object value, long cost) {
        try {
            return logMetaCreator.create(jp, value, cost);
        } catch (Exception e) {
            throw new LogException(String.format("创建日志任务失败： %s", e.getMessage()), e);
        }
    }

    private LogMeta createExceptionalLog(JoinPoint jp, Throwable cause, long cost) {
        try {
            return logMetaCreator.create(jp, cause, cost);
        } catch (Exception e) {
            throw new LogException(String.format("创建日志任务失败： %s", e.getMessage()), e);
        }
    }

    private void executeTask(LogTask task) {
        try {
            taskExecutor.execute(task);
        } catch (LogException e) {
            logger.warn("执行日志任务失败：{}", e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
