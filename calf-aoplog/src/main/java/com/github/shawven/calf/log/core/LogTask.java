package com.github.shawven.calf.log.core;

import com.github.shawven.calf.log.emun.LogAttribute;
import com.github.shawven.calf.log.emun.LogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Shoven
 * @date 2019-07-25 18:26
 */
public class LogTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    private final List<LogRepository> repositories;

    private final LogBuilder<LogMeta> logBuilder;

    private final LogMeta logMeta;

    public LogTask(List<LogRepository> repositories,
                   LogBuilder<LogMeta> logBuilder,
                   LogMeta logMeta) {
        this.repositories = repositories;
        this.logBuilder = logBuilder;
        this.logMeta = logMeta;
    }

    @Override
    public void run() {
        try {
            // 提取切入点信息
            JoinPointInfo joinPointInfo = new JoinPointInfo(logMeta.getJoinPoint());
            // 生成日志
            Recordable record = makeRecord(logMeta, joinPointInfo);
            // 写入
            write(record, joinPointInfo);
        } catch (LogException e) {
            logger.warn(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(String.format("日志系统异常: %s", e.getMessage()), e);
        }
    }

    protected Recordable makeRecord(LogMeta logMeta, JoinPointInfo joinPointInfo) {
        Map<LogAttribute, Object> attributes = joinPointInfo.extractAnnotationAttributes();
        Recordable log = logBuilder.build(logMeta, joinPointInfo);
        log.setModule(String.valueOf(attributes.get(LogAttribute.MODULE)));
        log.setDesc(String.valueOf(attributes.get(LogAttribute.VALUE)));
        log.setLogType(LogType.valueOf(String.valueOf(attributes.get(LogAttribute.TYPE))));
        return log;
    }

    public void write(Recordable record, JoinPointInfo joinPointInfo) {
        int repositoriesSize = repositories.size();
        Map<String, Exception> exceptions = new HashMap<>(repositoriesSize);

        for (LogRepository repository : repositories) {
            if (repository.isSupport(joinPointInfo)) {
                try {
                    repository.write(record);
                } catch (Exception e) {
                    exceptions.put(repository.getClass().getSimpleName(), e);
                }
            }
        }

        int exceptionSize = exceptions.size();
        if (exceptionSize == repositoriesSize) {
            throw new RuntimeException(String.format("日志未保存：%s", exceptions.toString()));
        } else if (exceptions.size() > 0) {
            String message;
            if (exceptionSize == 1) {
                Map.Entry<String, Exception> onlyOne = exceptions.entrySet().iterator().next();
                message = String.format("日志[%s]保存失败：%s", onlyOne.getKey(), onlyOne.getValue());
            } else {
                message = String.format("以下日志保存失败：%s", exceptions.toString());
            }
            throw new LogException(message);
        }
    }
}
